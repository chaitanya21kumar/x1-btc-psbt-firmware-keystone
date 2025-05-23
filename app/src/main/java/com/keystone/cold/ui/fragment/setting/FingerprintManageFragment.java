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

import android.graphics.Color;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.navigation.fragment.NavHostFragment;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.FingerprintManageBinding;
import com.x1-btc-psbt-firmware.cold.databinding.InputModalBinding;
import com.x1-btc-psbt-firmware.cold.fingerprint.FingerprintKit;
import com.x1-btc-psbt-firmware.cold.fingerprint.RemovalListener;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;

import java.util.Objects;

import static com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable.OFF;
import static com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable.TYPE_PASSPHRASE;
import static com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.x1-btc-psbt-firmware.cold.callables.FingerprintPolicyCallable.WRITE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;

public class FingerprintManageFragment extends BaseFragment<FingerprintManageBinding> {

    private Fingerprint fingerprint;
    private FingerprintKit fpKit;
    private final ObservableField<String> input = new ObservableField<>();
    private String fingerprintName;
    private ModalDialog dialog;
    private String password;

    @Override
    protected int setView() {
        return R.layout.fingerprint_manage;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());

        fpKit = new FingerprintKit(mActivity);
        fingerprint = data.getParcelable("xfp");
        fingerprintName = fingerprint.getName().toString();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbarTitle.setText(fingerprint.getName());
        mBinding.rename.title.setText(R.string.rename_fingerprint);
        mBinding.rename.item.setOnClickListener(v -> rename());

        mBinding.remove.title.setText(R.string.delete_fingerprint);
        mBinding.remove.title.setTextColor(Color.parseColor("#F0264E"));
        mBinding.remove.item.setOnClickListener(v -> remove());
        input.set(fingerprint.getName().toString());
        password = data.getString(PASSWORD);
    }

    private void remove() {
        dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        binding.title.setText(R.string.delete_fingerprint);
        binding.subTitle.setText(getString(R.string.confirm_delete) + fingerprintName);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setBackgroundColor(Color.parseColor("#DF1E44"));
        binding.confirm.setText(R.string.confirm_delete);
        binding.confirm.setOnClickListener(v -> {
            fpKit.removeFingerprint(fingerprint, new RemovalListener() {
                @Override
                public void onSuccess() {
                    if (!fpKit.hasEnrolledFingerprint()) {
                        Utilities.setFingerprintUnlockEnable(mActivity, false);
                        new FingerprintPolicyCallable(password, WRITE, TYPE_PASSPHRASE, OFF).call();
                        new FingerprintPolicyCallable(password, WRITE, TYPE_SIGN_TX, OFF).call();
                        NavHostFragment.findNavController(FingerprintManageFragment.this)
                                .popBackStack(R.id.settingFragment, false);
                    } else {
                        navigateUp();
                    }

                }

                @Override
                public void onError(int errMsgId, String errString) {

                }
            });
            dialog.dismiss();
            mBinding.toolbarTitle.setText(input.get());
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void rename() {
        dialog = new ModalDialog();
        InputModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.input_modal, null, false);
        binding.title.setText(R.string.fingerprint_rename_subtitle);
        binding.setInput(input);
        binding.inputBox.setSelectAllOnFocus(true);
        binding.inputBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        binding.inputBox.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        binding.inputBox.setTransformationMethod(null);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setOnClickListener(v -> {
            fpKit.renameFingerprint(fingerprint, input.get());
            dialog.dismiss();
            mBinding.toolbarTitle.setText(input.get());
            fingerprintName = input.get();
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
