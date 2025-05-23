/*
 *
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
 *
 */

package com.x1-btc-psbt-firmware.cold.ui.fragment.multisigs.legacy;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportMultisigWalletToWatchWalletBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportSdcardModalBinding;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.update.utils.Storage;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showExportResult;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.writeToSdcard;

public class ExportMultiSigWalletToWatchWallet extends MultiSigBaseFragment<ExportMultisigWalletToWatchWalletBinding> {

    private JSONObject caravanWalletJson;
    private String walletName;
    @Override
    protected int setView() {
        return R.layout.export_multisig_wallet_to_watch_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        multiSigViewModel.exportWalletToCaravan(data.getString("wallet_fingerprint")).observe(this, jsonObject -> {
            caravanWalletJson = jsonObject;
            walletName = caravanWalletJson.optString("name");
            mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(jsonObject.toString().getBytes()));
        });

        mBinding.exportToSdcard.setOnClickListener(v -> exportToSdcard());
        mBinding.info.setOnClickListener(v -> showCaravanImportGuide(mActivity));
        if (data.getBoolean("setup", false)) {
            mBinding.done.setOnClickListener(v -> popBackStack(R.id.caravanMultisigFragment, false));
        } else {
            mBinding.done.setOnClickListener(v -> navigateUp());
        }
    }

    private void showCaravanImportGuide(AppCompatActivity activity) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(activity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.caravan_import_guide_title);
        binding.subTitle.setText(R.string.caravan_import_guide);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    private void exportToSdcard() {
        Storage storage = Storage.createByEnvironment();
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog modalDialog = ModalDialog.newInstance();
            ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.export_sdcard_modal, null, false);
            binding.title.setText("导出钱包文件");
            binding.fileName.setText(walletName + ".json");
            binding.actionHint.setVisibility(View.GONE);
            binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
            binding.confirm.setOnClickListener(vv -> {
                modalDialog.dismiss();
                try {
                    boolean result = writeToSdcard(storage, caravanWalletJson.toString(2), walletName + ".json");
                    showExportResult(mActivity, null, result, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            modalDialog.setBinding(binding);
            modalDialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }
}
