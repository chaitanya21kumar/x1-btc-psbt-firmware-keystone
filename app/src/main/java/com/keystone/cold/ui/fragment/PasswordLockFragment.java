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

package com.x1-btc-psbt-firmware.cold.ui.fragment;

import static com.x1-btc-psbt-firmware.cold.Utilities.IS_SETUP_VAULT;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.IS_FORCE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_TITLE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.View;

import androidx.databinding.ObservableField;

import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.callables.VerifyPasswordCallable;
import com.x1-btc-psbt-firmware.cold.databinding.PasswordUnlockBinding;
import com.x1-btc-psbt-firmware.cold.fingerprint.FingerprintKit;
import com.x1-btc-psbt-firmware.cold.setting.VibratorHelper;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setting.MainPreferenceFragment;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment;
import com.x1-btc-psbt-firmware.cold.util.HashUtil;
import com.x1-btc-psbt-firmware.cold.util.Keyboard;

import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

public class PasswordLockFragment extends BaseFragment<PasswordUnlockBinding> {

    public static final int MAX_PWD_RETRY_TIMES = 5;

    private int retryTimes;
    private final ObservableField<String> password = new ObservableField<>("");

    public static final String TAG = "PasswordLockFragment";

    @Override
    protected int setView() {
        return R.layout.password_unlock;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        boolean isForce = data.getBoolean(IS_FORCE);
        if (isForce) {
            mBinding.toolbar.setNavigationIcon(new ColorDrawable(Color.TRANSPARENT));
        } else {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        }
        boolean hasVault = Utilities.hasVaultCreated(mActivity);
        retryTimes = Utilities.getPasswordRetryTimes(mActivity);
        mBinding.passwordInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        mBinding.setPassword(password);
        mBinding.unlock.setOnClickListener(v -> {
            Handler handler = new Handler();
            mBinding.progress.setVisibility(View.VISIBLE);
            mBinding.unlock.setVisibility(View.GONE);
            AppExecutors.getInstance().diskIO().execute(() -> {
                String passwordHash = getPasswordHash(password.get());
                boolean verified = verifyPasswordHash(passwordHash);
                if (verified) {
                    Utilities.setPasswordRetryTimes(mActivity, 0);
                    Utilities.setPatternRetryTimes(mActivity, 0);
                    FingerprintKit.verifyPassword(mActivity);
                    mActivity.finish();
                } else {
                    handler.post(() -> {
                        password.set("");
                        mBinding.unlock.setVisibility(View.VISIBLE);
                        mBinding.progress.setVisibility(View.GONE);
                        mBinding.passwordInput.clearComposingText();
                        mBinding.hint.setVisibility(View.VISIBLE);
                        retryTimes++;
                        mBinding.hint.setText(getHintText());
                        Utilities.setPasswordRetryTimes(mActivity, retryTimes);
                        VibratorHelper.vibrate(mActivity);
                        if (retryTimes >= MAX_PWD_RETRY_TIMES) {
                            Keyboard.hide(mActivity, mBinding.passwordInput);
                            new Handler().postDelayed(() -> MainPreferenceFragment.reset(mActivity), 1000);
                        }
                    });
                }
            });
        });

        Bundle bundle = new Bundle();

        mBinding.forget.setOnClickListener(v -> {
            if (hasVault) {
                bundle.putString(KEY_TITLE, getString(R.string.verify_mnemonic));
                bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
                navigate(R.id.action_resetpassword_verifyMnemonic, bundle);
            } else {
                bundle.putBoolean(IS_SETUP_VAULT, true);
                navigate(R.id.action_to_setPasswordFragment, bundle);
            }
        });
        Keyboard.show(mActivity, mBinding.passwordInput);
    }

    private String getHintText() {
        String hint;
        if (retryTimes < MAX_PWD_RETRY_TIMES) {
            hint = getString(R.string.password_error_hint, MAX_PWD_RETRY_TIMES - retryTimes);
        } else {
            hint = getString(R.string.reset_hint);
        }
        return hint;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getPasswordHash(String input) {
        byte[] passwordHash;
        if (input == null) {
            return "";
        } else {
            passwordHash = HashUtil.twiceSha256(input);
            if (passwordHash == null) {
                return "";
            }
        }
        return Hex.toHexString(passwordHash);
    }

    public static boolean verifyPasswordHash(String passwordHash) {
        if (passwordHash.equals("")) return false;
        return new VerifyPasswordCallable(passwordHash).call();
    }
}
