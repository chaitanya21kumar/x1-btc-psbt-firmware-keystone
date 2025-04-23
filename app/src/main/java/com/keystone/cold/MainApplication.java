/*
 * Copyright (c) 2021 X1-BTC-PSBT-Firmware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file LICENSE.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.x1-btc-psbt-firmware.cold;

import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_MULTI_SIG_MODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.x1-btc-psbt-firmware.coinlib.v8.ScriptLoader;
import com.x1-btc-psbt-firmware.cold.callables.RestartSeCallable;
import com.x1-btc-psbt-firmware.cold.db.AppDatabase;
import com.x1-btc-psbt-firmware.cold.encryption.EncryptionCoreProvider;
import com.x1-btc-psbt-firmware.cold.logging.FileLogger;
import com.x1-btc-psbt-firmware.cold.sdcard.OnSdcardStatusChange;
import com.x1-btc-psbt-firmware.cold.sdcard.SdCardStatusMonitor;
import com.x1-btc-psbt-firmware.cold.sdcard.SdcardFormatHelper;
import com.x1-btc-psbt-firmware.cold.service.AttackCheckingService;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;
import com.x1-btc-psbt-firmware.cold.ui.UnlockActivity;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;

import java.lang.ref.SoftReference;

public class MainApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static MainApplication sApplication;
    private AppExecutors mAppExecutors;
    private boolean shouldLock;
    private boolean showFormatSdcard = false;

    public MainApplication() {
        sApplication = this;
    }

    @NonNull
    public static MainApplication getApplication() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        compatibleMultisigModeSp();
        migrateVaultCreateFlag();
        mAppExecutors = AppExecutors.getInstance();
        EncryptionCoreProvider.getInstance().initialize(this);
        mAppExecutors.diskIO().execute(() -> {
            FileLogger.init(this);
            FileLogger.purgeLogs(this);
        });
        initBackgroundCallBack();
        ScriptLoader.init(this);

        registerReceiver(mScreeOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        shouldLock = Utilities.hasPasswordSet(this);

        startAttackCheckingService();
        RestartSe();
        registerSdcardStatusMonitor();
    }

    private void compatibleMultisigModeSp() {
        SharedPreferences prefs = Utilities.getPrefs(sApplication);
        String mutlisigMode = prefs.getString(SETTING_MULTI_SIG_MODE, MultiSigMode.CASA.getModeId());
        switch (mutlisigMode) {
            case "0":
                prefs.edit().putString(SETTING_MULTI_SIG_MODE, MultiSigMode.LEGACY.getModeId()).apply();
                break;
            case "1":
                prefs.edit().putString(SETTING_MULTI_SIG_MODE, MultiSigMode.CASA.getModeId()).apply();
                break;
            default:
                break;
        }
    }

    private void migrateVaultCreateFlag() {
        if (Utilities.hasVaultCreated(sApplication) && Utilities.getVaultCreateStep(sApplication) == SetupVaultViewModel.VAULT_CREATE_STEP_WELCOME) {
            Utilities.setVaultCreateStep(sApplication, SetupVaultViewModel.VAULT_CREATE_STEP_DONE);
        }
        if (Utilities.hasVaultCreated(sApplication) && !Utilities.hasPasswordSet(sApplication)) {
            Utilities.markPasswordSet(sApplication);
        }
    }

    private void registerSdcardStatusMonitor() {
        sdcardFormatHelper = new SdcardFormatHelper();
        SdCardStatusMonitor.getInstance(this).register(new OnSdcardStatusChange() {
            @Override
            public String id() {
                return "application";
            }

            @Override
            public void onInsert() {
                boolean needFormatSdcard = sdcardFormatHelper.needFormatSdcard();
                if (needFormatSdcard) {
                    AppCompatActivity activity = topActivity.get();
                    if (activity instanceof MainActivity || activity instanceof SetupVaultActivity) {
                        sdcardFormatHelper.showFormatModal(activity);
                        showFormatSdcard = false;
                    } else {
                        showFormatSdcard = true;
                    }
                } else {
                    showFormatSdcard = false;
                }
            }

            @Override
            public void onRemove() {
                showFormatSdcard = false;
            }
        });
        showFormatSdcard = sdcardFormatHelper.needFormatSdcard();
    }

    private void RestartSe() {
        if (Utilities.hasVaultCreated(this)) {
            mAppExecutors.diskIO().execute(() -> {
                boolean success = new RestartSeCallable().call();
                if (success) {
                    getRepository().deleteHiddenVaultData();
                    Utilities.setCurrentBelongTo(this, "main");
                }
            });
        }
    }

    private void startAttackCheckingService() {
        Intent intent = new Intent(this, AttackCheckingService.class);
        startService(intent);
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(this, getDatabase());
    }

    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if ((activity instanceof MainActivity || activity instanceof SetupVaultActivity) && savedInstanceState == null && shouldLock) {
                    Intent intent = new Intent(activity, UnlockActivity.class);
                    activity.startActivity(intent);
                    shouldLock = false;
                }

                if (activity instanceof MainActivity && savedInstanceState != null) {
                    showFormatSdcard = false;
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                topActivity = new SoftReference<>((AppCompatActivity) activity);
                if ((activity instanceof MainActivity || activity instanceof SetupVaultActivity) && showFormatSdcard) {
                    sdcardFormatHelper.showFormatModal(topActivity.get());
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    private SoftReference<AppCompatActivity> topActivity;
    private SdcardFormatHelper sdcardFormatHelper;
    private final BroadcastReceiver mScreeOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Activity activity = topActivity.get();
                if (!(activity instanceof UnlockActivity)
                        && Utilities.hasPasswordSet(activity)
                        && !Utilities.isAttackDetected(activity)) {
                    startActivity(new Intent(activity, UnlockActivity.class));
                } else if ((activity instanceof UnlockActivity) && !Utilities.hasPasswordSet(activity)) {
                    activity.finish();
                }
            }
        }
    };
}
