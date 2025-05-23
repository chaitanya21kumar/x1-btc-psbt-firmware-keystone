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

package com.x1-btc-psbt-firmware.cold.ui.fragment.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.android.internal.app.LocalePicker;
import com.x1-btc-psbt-firmware.coinlib.Util;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable;
import com.x1-btc-psbt-firmware.cold.callables.GetMessageCallable;
import com.x1-btc-psbt-firmware.cold.callables.ResetCallable;
import com.x1-btc-psbt-firmware.cold.config.FeatureFlags;
import com.x1-btc-psbt-firmware.cold.databinding.FactoryResetModalBinding;
import com.x1-btc-psbt-firmware.cold.fingerprint.FingerprintKit;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.Constants;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.ui.modal.ProgressModalDialog;
import com.x1-btc-psbt-firmware.cold.ui.preference.SimplePreference;
import com.x1-btc-psbt-firmware.cold.ui.preference.SwitchPreference;
import com.x1-btc-psbt-firmware.cold.ui.views.AuthenticateModal;
import com.x1-btc-psbt-firmware.cold.ui.views.UpdatingHelper;
import com.x1-btc-psbt-firmware.cold.update.data.UpdateManifest;
import com.x1-btc-psbt-firmware.cold.util.DataCleaner;
import com.x1-btc-psbt-firmware.cold.viewmodel.UpdatingViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet;

import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

import static android.content.Context.FINGERPRINT_SERVICE;
import static com.x1-btc-psbt-firmware.cold.Utilities.IS_SETUP_VAULT;
import static com.x1-btc-psbt-firmware.cold.Utilities.SHARED_PREFERENCES_KEY;
import static com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable.READ;
import static com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable.TYPE_PASSPHRASE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.LicenseFragment.KEY_TITLE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.LicenseFragment.KEY_URL;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.SIGNATURE;

public class MainPreferenceFragment extends PreferenceFragmentCompat {

    private static final String SETTING_PATTERN_UNLOCK = "setting_pattern_unlock";
    private static final String SETTING_CHANGE_PWD = "setting_change_pwd";
    private static final String SETTING_SYSTEM = "setting_system";
    private static final String SETTING_WHITE_LIST = "setting_white_list";
    private static final String SETTING_MNEMONIC_ENTRY = "setting_mnemonic_entry";
    private static final String SETTING_SAFETY_INSTRUCTION = "setting_safety_instruction";
    private static final String SETTING_LICENSE = "setting_license";
    private static final String SETTING_PRIVACY_POLICY = "setting_privacy_policy";
    private static final String SETTING_FACTORY_RESET = "setting_factory_reset";
    private static final String SETTING_VERSION = "setting_version";
    private static final String SETTING_FINGERPRINT = "setting_fingerprint";
    private static final String SETTING_PASSPHRASE = "setting_passphrase";
    public static final String SETTING_CHOOSE_WATCH_WALLET = "setting_choose_watch_only_wallet";
    public static final String SETTING_ADDRESS_FORMAT = "setting_address_format";
    public static final String SETTING_TESTNET = "setting_testnet";
    public static final String SETTING_CHECK_MNEMONIC = "setting_check_mnemonic";
    public static final String SETTING_MULTI_SIG_MODE = "setting_multi_sig_mode";

    private SwitchPreference switchPreference;
    private SimplePreference versionPreference;
    private SimplePreference testNetPreference;

    private BadgeView patternBadgeView;
    private BadgeView fingerprintBadgeView;

    private SimplePreference fingerprintPreference;

    private UpdateManifest manifest;

    private UpdatingViewModel viewModel;

    protected AppCompatActivity mActivity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.main_preference);
        SimplePreference whiteListPreference = findPreference(SETTING_WHITE_LIST);
        if (!FeatureFlags.ENABLE_WHITE_LIST && whiteListPreference != null) {
            getPreferenceScreen().removePreference(whiteListPreference);
        }
        viewModel = ViewModelProviders.of(mActivity).get(UpdatingViewModel.class);
        subscribeUpdateChecking();
        fingerprintPreference = findPreference(SETTING_FINGERPRINT);
        if (!FingerprintKit.isHardwareDetected(mActivity)) {
            getPreferenceScreen().removePreference(fingerprintPreference);
        }
        testNetPreference = findPreference(SETTING_TESTNET);
        if (testNetPreference != null) {
            testNetPreference.setRemindText(Utilities.isMainNet(mActivity) ?
                    getString(R.string.mainnet) : getString(R.string.testnet));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        versionPreference = findPreference(SETTING_VERSION);
        if (versionPreference != null) {
            versionPreference.setTitle(getString(R.string.version) + " " + getAppVersion(mActivity));
        }

        switchPreference = findPreference(SETTING_PATTERN_UNLOCK);
        if (switchPreference != null) {
            switchPreference.setChecked(Utilities.isPatternUnlock(mActivity));
        }

        SimplePreference chooseWalletPreference = findPreference(SETTING_CHOOSE_WATCH_WALLET);
        if (chooseWalletPreference != null) {
            chooseWalletPreference.setRemindText(WatchWallet.getWatchWallet(mActivity)
                    .getWalletName(mActivity));
        }
        if (testNetPreference != null) {
            testNetPreference.setRemindText(Utilities.isMainNet(mActivity) ?
                    getString(R.string.mainnet) : getString(R.string.testnet));
        }
        Looper.getMainLooper().getQueue().addIdleHandler(() -> {

            if (!Utilities.hasUserClickPatternLock(mActivity)) {
                patternBadgeView = BadgeFactory.create(mActivity)
                        .setWidthAndHeight(10, 10)
                        .setBadgeBackground(Color.RED)
                        .setBadgeGravity(Gravity.END | Gravity.TOP)
                        .setShape(BadgeView.SHAPE_CIRCLE)
                        .setSpace(10, 0)
                        .bind(switchPreference.getViewHolder().findViewById(android.R.id.title));
            }

            if (fingerprintPreference != null && FingerprintKit.isHardwareDetected(mActivity)
                    && !Utilities.hasUserClickFingerprint(mActivity)) {
                fingerprintBadgeView = BadgeFactory.create(mActivity)
                        .setWidthAndHeight(10, 10)
                        .setBadgeBackground(Color.RED)
                        .setBadgeGravity(Gravity.END | Gravity.TOP)
                        .setShape(BadgeView.SHAPE_CIRCLE)
                        .setSpace(10, 0)
                        .bind(fingerprintPreference.getViewHolder().findViewById(android.R.id.title));
            }
            return false;
        });
    }

    private void subscribeUpdateChecking() {
        viewModel.getUpdateManifest().observe(this, updateManifest -> {
            manifest = updateManifest;
            updateVersionPreference();
        });
    }

    private void update() {
        new UpdatingHelper(mActivity,true).onUpdatingDetect(manifest);
    }

    public static void restartApplication(AppCompatActivity activity) {
        final Intent intent = ((Context) activity).getPackageManager().getLaunchIntentForPackage(((Context) activity).getPackageName());
        Objects.requireNonNull(intent).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void updateVersionPreference() {
        versionPreference = findPreference(SETTING_VERSION);
        if (versionPreference != null) {
            versionPreference.setRemindText(manifest != null ? getString(R.string.updatable) : "");
        }
    }

    private String getAppVersion(Context context) {
        String version = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
            if (version != null && version.length() > 2) {
                version = version.substring(2);
            }
            version = "B-" + version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case SETTING_CHANGE_PWD:
                handleChangePassword();
                break;
            case SETTING_PATTERN_UNLOCK:
                if (patternBadgeView != null) {
                    Utilities.setUserClickPatternLock(mActivity);
                    patternBadgeView.unbind();
                    patternBadgeView = null;
                    ((MainActivity) mActivity).updateBadge();
                }
                handleSwitchPatternUnlock();
                break;
            case SETTING_FINGERPRINT:
                if (fingerprintBadgeView != null) {
                    Utilities.setUserClickFingerprint(mActivity);
                    fingerprintBadgeView.unbind();
                    fingerprintBadgeView = null;
                    ((MainActivity) mActivity).updateBadge();
                }
                handleFingerprintSetting();
                break;
            case SETTING_SYSTEM:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_to_systemSettingFragment);
                break;
            case SETTING_WHITE_LIST:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_settingFragment_to_whiteListFragment);
                break;
            case SETTING_MNEMONIC_ENTRY:
                handleCreateVault();
                break;
            case SETTING_PASSPHRASE:
                handlePassphrase();
                break;
            case SETTING_SAFETY_INSTRUCTION:
                navigateToLicense("safety_instruction.html", preference.getTitle().toString());
                break;
            case SETTING_LICENSE:
                navigateToLicense("license.html", preference.getTitle().toString());
                break;
            case SETTING_PRIVACY_POLICY:
                navigateToLicense("privacy_policy.html", preference.getTitle().toString());
                break;
            case SETTING_FACTORY_RESET:
                handleFactoryReset();
                break;
            case SETTING_VERSION:
                if (manifest == null) {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_to_versionFragment);
                } else {
                    update();
                }
                break;
            case SETTING_CHOOSE_WATCH_WALLET:
                Bundle data = new Bundle();
                data.putInt(Constants.KEY_TITLE, R.string.choose_watch_only_wallet);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_to_chooseWatchOnly, data);

                break;
            case SETTING_TESTNET:
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.KEY_TITLE, R.string.select_network);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_to_switchNetwork, bundle);
                break;
            case SETTING_CHECK_MNEMONIC:
                AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "",
                        token -> {
                            Bundle bundle1 = new Bundle();
                            bundle1.putString(ACTION, PreImportFragment.ACTION_CHECK);
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_to_preImportFragment, bundle1);
                        }, forgetPassword);
                break;

            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_to_preImportFragment, bundle);
    };

    private void handlePassphrase() {
        boolean fingerprint = new FingerprintPolicyCallable(READ, TYPE_PASSPHRASE).call();
        AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "", fingerprint,
                token -> {
                    NavController navController = Navigation.findNavController(requireView());
                    if (token.password != null) {
                        navController.navigate(R.id.action_settingFragment_to_passphraseFragment,
                                Bundle.forPair(PASSWORD, token.password));
                    } else if (token.signature != null) {
                        String message = new GetMessageCallable().call();
                        String signatureRS = null;
                        if (!TextUtils.isEmpty(message)) {
                            try {
                                token.signature.update(Hex.decode(message));
                                byte[] signature = token.signature.sign();
                                byte[] rs = Util.decodeRSFromDER(signature);
                                signatureRS = Hex.toHexString(rs);
                            } catch (SignatureException e) {
                                e.printStackTrace();
                            }
                        }
                        if (signatureRS != null) {
                            navController.navigate(R.id.action_settingFragment_to_passphraseFragment,
                                    Bundle.forPair(SIGNATURE, signatureRS));
                        }
                    }
                }, forgetPassword);
    }

    private void handleFingerprintSetting() {
        AuthenticateModal.show(mActivity, getString(R.string.password_modal_title),
                "",
                password -> {
                    int navId;
                    FingerprintKit fpKit = new FingerprintKit(mActivity);
                    if (fpKit.hasEnrolledFingerprint()) {
                        navId = R.id.action_to_fingerprintSettingFragment;
                    } else {
                        navId = R.id.action_to_fingerprintGuideFragment;
                    }

                    Navigation.findNavController(requireView())
                            .navigate(navId, Bundle.forPair(PASSWORD, password.password));
                },
                forgetPassword
        );
    }

    private void handleCreateVault() {
        AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "",
                password -> {
                    Intent intent = new Intent(mActivity, SetupVaultActivity.class);
                    intent.putExtra(IS_SETUP_VAULT, false);
                    intent.putExtra(PASSWORD,password.password);
                    startActivity(intent);
                }, forgetPassword);
    }

    private void handleFactoryReset() {
        ModalDialog dialog = ModalDialog.newInstance();
        FactoryResetModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.factory_reset_modal,null,false);
        dialog.setBinding(binding);
        binding.close.setOnClickListener(v->dialog.dismiss());
        binding.confirm.setOnClickListener(v-> {
            dialog.dismiss();
            AuthenticateModal.show(mActivity, getString(R.string.password_modal_title),
                    "", token -> reset(mActivity), forgetPassword);
        });
        dialog.show(mActivity.getSupportFragmentManager(),"");
    }

    public static void reset(AppCompatActivity activity) {
        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(activity.getSupportFragmentManager()), "");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SystemPreferenceFragment.resetSetting(activity);
                new ResetCallable().call();
                DataCleaner.cleanApplicationData(activity);
                removeAllFingerprint(activity);
                LocalePicker.updateLocale(Locale.ENGLISH);
            } catch (Exception ignored) {
            } finally {
                DataCleaner.cleanApplicationData(activity);
                restartApplication(activity);
            }
        });
    }

    public static void removeAllFingerprint(AppCompatActivity activity) {
        FingerprintManager fm = (FingerprintManager) activity.getSystemService(FINGERPRINT_SERVICE);
        if (fm.isHardwareDetected() && fm.hasEnrolledFingerprints()) {
            FingerprintKit fpKit = new FingerprintKit(activity);
            if (FingerprintKit.isHardwareDetected(activity)) {
                List<Fingerprint> fingerprints = fpKit.getEnrolledFingerprints();
                fingerprints.forEach(f -> {
                    fpKit.removeFingerprint(f, null);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void handleChangePassword() {
        AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "", password -> {
            Bundle data = new Bundle();
            data.putString(PASSWORD, password.password);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_to_change_password, data);
        }, forgetPassword);
    }

    private void handleSwitchPatternUnlock() {
        if (switchPreference.isChecked()) {
            switchPreference.setChecked(false);
            Utilities.clearPatternUnlock(mActivity);
        } else {
            AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "", token ->
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_to_setPatternUnlock), forgetPassword);
        }
    }

    private void navigateToLicense(String url, String title) {
        Bundle data = new Bundle();
        data.putString(KEY_URL, url);
        data.putString(KEY_TITLE, title);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_to_licenseFragment, data);
    }
}
