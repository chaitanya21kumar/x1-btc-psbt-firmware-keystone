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

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.electrum.SignedTxFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import static com.x1-btc-psbt-firmware.cold.ui.modal.ExportPsbtDialog.showExportPsbtDialog;
import static com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet.PSBT_MULTISIG_SIGN_ID;

public class PsbtSignedTxFragment extends SignedTxFragment {

    private boolean isMultisig;

    @Override
    protected void displaySignResult(TxEntity txEntity) {
        isMultisig = txEntity.getSignId().equals(PSBT_MULTISIG_SIGN_ID);
        if (isMultisig) {
            boolean signed = isSigned(txEntity);
            mBinding.txDetail.scanHint.setText(signed ? getString(R.string.broadcast_multisig_tx_hint)
                    : getString(R.string.export_multisig_tx_hint));
            //show bc32 animated qr code
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setVisibility(View.VISIBLE);

            byte[] psbtBytes = Base64.decode(txEntity.getSignedHex());
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setData(new CryptoPSBT(psbtBytes).toUR().toString());
            mBinding.txDetail.dynamicQrcodeLayout.hint.setVisibility(View.GONE);

            mBinding.txDetail.qrcodeLayout.qrcode.setVisibility(View.GONE);
            mBinding.txDetail.broadcastGuide.setVisibility(View.GONE);
            mBinding.txDetail.export.setVisibility(View.GONE);

            mBinding.txDetail.info.setVisibility(View.INVISIBLE);
            mBinding.txDetail.exportToSdcardHint.setVisibility(View.VISIBLE);
            mBinding.txDetail.exportToSdcardHint.setText(R.string.generic_qrcode_hint);
            mBinding.txDetail.exportToSdcardHint.setOnClickListener(v -> showExportDialog());

        } else if (watchWallet.supportPsbt() && watchWallet.supportBc32QrCode()) {
            if (watchWallet == WatchWallet.BLUE) {
                mBinding.txDetail.info.setOnClickListener(v -> showBlueWalletInfo(
                        R.string.blue_wallet_broadcast_guide,
                        R.string.blue_wallet_broadcast_guide1));
            } else if (watchWallet == WatchWallet.BTCPAY) {
                mBinding.txDetail.info.setOnClickListener(v -> showBlueWalletInfo(
                        R.string.btc_pay_broadcast_guide,
                        R.string.btc_pay_broadcast_guide_content));
            }
            mBinding.txDetail.scanHint.setText(mActivity.getString(R.string.use_wallet_to_broadcast,
                    watchWallet.getWalletName(mActivity)));
            if (watchWallet.supportBc32QrCode()) {
                if (watchWallet.supportUR2()) {
                    byte[] psbtBytes = Base64.decode(txEntity.getSignedHex());
                    mBinding.txDetail.dynamicQrcodeLayout.qrcode.setData(new CryptoPSBT(psbtBytes).toUR().toString());
                } else {
                    mBinding.txDetail.dynamicQrcodeLayout.qrcode
                            .setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
                }
            }

            mBinding.txDetail.exportToSdcardHint.setVisibility(View.GONE);
            mBinding.txDetail.qrcodeLayout.qrcode.setVisibility(View.GONE);
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setVisibility(View.VISIBLE);
            mBinding.txDetail.broadcastGuide.setVisibility(View.GONE);
            if (!watchWallet.supportSdcard()) {
                mBinding.txDetail.export.setVisibility(View.GONE);
            }
            if (watchWallet == WatchWallet.GENERIC || watchWallet == WatchWallet.SPARROW) {
                mBinding.txDetail.dynamicQrcodeLayout.hint.setVisibility(View.GONE);
                mBinding.txDetail.info.setVisibility(View.INVISIBLE);
                mBinding.txDetail.exportToSdcardHint.setVisibility(View.VISIBLE);
                mBinding.txDetail.exportToSdcardHint.setText(R.string.generic_qrcode_hint);
                mBinding.txDetail.exportToSdcardHint.setOnClickListener(v -> showExportDialog());
                mBinding.txDetail.export.setVisibility(View.GONE);

            }

        } else if (watchWallet == WatchWallet.WASABI) {
            mBinding.txDetail.qr.setVisibility(View.GONE);
            mBinding.txDetail.broadcastGuide.setGravity(Gravity.START);
            mBinding.txDetail.broadcastGuide.setText(getBroadcastGuideText());
        }
    }

    private int getBroadcastGuideText() {
        if (watchWallet == WatchWallet.WASABI) {
            return R.string.wasabi_broadcast_guide;
        } else if (watchWallet == WatchWallet.BTCPAY) {
            return R.string.btcpay_broadcast_guide;
        }
        return 0;
    }

    private boolean isSigned(TxEntity txEntity) {
        String signStatus = txEntity.getSignStatus();
        String[] splits = signStatus.split("-");
        int sigNumber = Integer.parseInt(splits[0]);
        int reqSigNumber = Integer.parseInt(splits[1]);
        return sigNumber >= reqSigNumber;
    }

    private void showBlueWalletInfo(int title, int content) {
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

    @Override
    protected void showExportDialog() {
        Runnable runnable;
        if (!isMultisig) {
            runnable = () -> popBackStack(R.id.assetFragment, false);
        } else {
            runnable = () -> popBackStack(R.id.legacyMultisigFragment, false);
        }
        showExportPsbtDialog(mActivity, txEntity, runnable);
    }
}
