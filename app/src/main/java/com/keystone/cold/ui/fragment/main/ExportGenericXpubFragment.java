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

package com.x1-btc-psbt-firmware.cold.ui.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.coinlib.ExtendPubkeyFormat;
import com.x1-btc-psbt-firmware.coinlib.utils.Account;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportSdcardModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportXpubGenericBinding;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.update.utils.Storage;
import com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoAccount;

import org.json.JSONObject;

import static com.x1-btc-psbt-firmware.coinlib.ExtendPubkeyFormat.convertExtendPubkey;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showExportResult;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.writeToSdcard;


public class ExportGenericXpubFragment extends BaseFragment<ExportXpubGenericBinding> {

    private JSONObject xpubInfo;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.export_xpub_generic;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        CryptoAccount cryptoAccount = GlobalViewModel.generateCryptoAccount(mActivity);
        UR ur = cryptoAccount.toUR();
        mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
        mBinding.qrcodeLayout.qrcode.setData(ur.toString());
        mBinding.done.setOnClickListener(v -> handleButtonClick());
        mBinding.skip.setOnClickListener(v -> popBackStack(R.id.assetFragment, false));
        mBinding.exportToSdcard.setOnClickListener(v -> exportToSdcard());
        refreshScanHint();
    }

    private void refreshScanHint() {
        if (watchWallet == WatchWallet.BTCPAY) {
            mBinding.skip.setVisibility(View.GONE);
            mBinding.scanHint.setText(R.string.scan_xpub_qrcode_with_btc_pay);
            mBinding.info.setVisibility(View.VISIBLE);
            mBinding.info.setOnClickListener(v ->
                    showExportGuide(getString(R.string.export_xpub_guide_text1_btcpay), getString(R.string.export_xpub_guide_text2_btcpay)));
        } else if (watchWallet == WatchWallet.SPARROW) {
            mBinding.skip.setVisibility(View.GONE);
            mBinding.scanHint.setText(R.string.scan_xpub_qrcode_with_sparrow);
            mBinding.info.setVisibility(View.VISIBLE);
            mBinding.info.setOnClickListener(v ->
                    showExportGuide(getString(R.string.export_xpub_guide_text1_sparrow), getString(R.string.export_xpub_guide_text2_sparrow)));
        }
    }

    private void showExportGuide(String title, String content) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(title);
        binding.subTitle.setText(content);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void handleButtonClick() {
        if (mActivity instanceof SetupVaultActivity) {
            startActivity(new Intent(mActivity, MainActivity.class));
            mActivity.finish();
        } else {
            popBackStack(R.id.assetFragment, false);
        }
    }

    private void exportToSdcard() {
        Storage storage = Storage.createByEnvironment();
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog modalDialog = ModalDialog.newInstance();
            ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.export_sdcard_modal, null, false);
            binding.title.setText(R.string.export_xpub_text_file);
            binding.fileName.setText(getFileName());
            binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
            binding.confirm.setOnClickListener(vv -> {
                modalDialog.dismiss();
                boolean result = writeToSdcard(storage, GlobalViewModel.getOutputDescriptionString(mActivity), getFileName());
                showExportResult(mActivity, null, result);
            });
            modalDialog.setBinding(binding);
            modalDialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private String getFileName() {
        Account account = GlobalViewModel.getAccount(mActivity);
        switch (account) {
            case P2WPKH:
            case P2WPKH_TESTNET:
                return "p2wpkh-pubkey.txt";
            case P2SH_P2WPKH:
            case P2SH_P2WPKH_TESTNET:
                return "p2sh-p2wpkh-pubkey.txt";
            case P2PKH:
            case P2PKH_TESTNET:
                return "p2pkh-pubkey.txt";
        }
        return "p2sh-p2wpkh-pubkey.txt";
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String convertExtpub(String expub, Account account) {
        return convertExtendPubkey(expub, ExtendPubkeyFormat.valueOf(account.getXpubPrefix()));
    }
}
