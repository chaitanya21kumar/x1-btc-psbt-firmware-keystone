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

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.BroadcastPsbtTxFragmentBinding;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.CasaSignature;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.CoinListViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;
import com.sparrowwallet.hummingbird.registry.CryptoPSBT;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import static com.x1-btc-psbt-firmware.cold.ui.modal.ExportPsbtDialog.showExportPsbtDialog;
import static com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet.BTCPAY;
import static com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet.SPARROW;
import static com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet.getWatchWallet;

public class PsbtBroadcastTxFragment extends BaseFragment<BroadcastPsbtTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    public static final String KEY_MULTISIG_MODE = "multisig_mode";
    private TxEntity txEntity;
    private CasaSignature casaSignature;
    private boolean isMultisig;
    private boolean signed;
    private MultiSigMode multiSigMode;

    @Override
    protected int setView() {
        return R.layout.broadcast_psbt_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = requireArguments();
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        String mode = data.getString(KEY_MULTISIG_MODE);
        boolean isCasaMultisig = false;
        boolean isLegacyMultisig = false;
        if (mode != null) {
            isMultisig = true;
            multiSigMode = MultiSigMode.valueOf(mode);
            if (multiSigMode.equals(MultiSigMode.LEGACY) || multiSigMode.equals(MultiSigMode.CARAVAN)) {
                isLegacyMultisig = true;
            } else {
                isCasaMultisig = true;
            }
        }

        if (!isMultisig || isLegacyMultisig) {
            viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
                this.txEntity = txEntity;
                mBinding.setCoinCode(txEntity.getCoinCode());
                WatchWallet wallet = getWatchWallet(mActivity);
                if (isMultisig) {
                    byte[] psbtBytes = Base64.decode(txEntity.getSignedHex());
                    mBinding.qrcodeLayout.qrcode.setData(new CryptoPSBT(psbtBytes).toUR().toString());
                } else {
                    switch (wallet) {
                        case GENERIC:
                        case SPARROW:
                        case BTCPAY:
                        case BLUE: {
                            byte[] psbtBytes = Base64.decode(txEntity.getSignedHex());
                            mBinding.qrcodeLayout.qrcode.setData(new CryptoPSBT(psbtBytes).toUR().toString());
                            break;
                        }
                        default:
                            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
                    }
                }
                updateUI();
            });
        } else if (isCasaMultisig) {
            viewModel.loadCasaSignature(data.getString(KEY_TXID)).observe(this, casaSignature -> {
                this.casaSignature = casaSignature;
                mBinding.setCoinCode("BTC");
                byte[] psbtBytes = Base64.decode(casaSignature.getSignedHex());
                mBinding.qrcodeLayout.qrcode.setData(new CryptoPSBT(psbtBytes).toUR().toString());
                updateUI();
            });
        }

    }

    private void updateUI() {
        View.OnClickListener goHome;
        if (isMultisig) {
            if (multiSigMode.equals(MultiSigMode.LEGACY)) {
                goHome = v -> popBackStack(R.id.legacyMultisigFragment, false);
                mBinding.signStatus.setText(getString(R.string.sign_status) + ":" + getSignStatus(txEntity));
                mBinding.exportToSdcard.setOnClickListener(v ->
                        showExportPsbtDialog(mActivity, txEntity, null));
            } else if (multiSigMode.equals(MultiSigMode.CARAVAN)) {
                goHome = v -> popBackStack(R.id.caravanMultisigFragment, false);
                mBinding.signStatus.setText(getString(R.string.sign_status) + ":" + getSignStatus(txEntity));
                mBinding.exportToSdcard.setOnClickListener(v ->
                        showExportPsbtDialog(mActivity, txEntity, null));
            } else {
                goHome = v -> popBackStack(R.id.casaMultisigFragment, false);
                mBinding.signStatus.setText(getString(R.string.sign_status) + ":" + getSignStatus(casaSignature));
                mBinding.exportToSdcard.setOnClickListener(v ->
                        showExportPsbtDialog(mActivity, casaSignature, null));
            }
            mBinding.toolbarTitle.setText(getString(R.string.export_tx));
            mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
            mBinding.exportToSdcard.setVisibility(View.VISIBLE);
            if (signed) {
                mBinding.scanHint.setText(R.string.broadcast_multisig_tx_hint);
            } else {
                mBinding.scanHint.setText(R.string.export_multisig_tx_hint);
            }
        } else {
            goHome = v -> popBackStack(R.id.assetFragment, false);
            WatchWallet wallet = WatchWallet.getWatchWallet(mActivity);
            if (wallet.supportSdcard()) {
                mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
                mBinding.exportToSdcard.setVisibility(View.VISIBLE);
                mBinding.exportToSdcard.setOnClickListener(v ->
                        showExportPsbtDialog(mActivity, txEntity, null));
            } else {
                mBinding.exportToSdcard.setVisibility(View.GONE);
            }
            mBinding.signStatus.setVisibility(View.GONE);
            mBinding.scanHint.setText(getString(R.string.use_wallet_to_broadcast,
                    WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
            if (wallet == BTCPAY) {
                mBinding.info.setVisibility(View.VISIBLE);
                mBinding.info.setOnClickListener(v -> showBroadcastGuide(R.string.btc_pay_broadcast_guide,
                        R.string.btc_pay_broadcast_guide_content));
            }
            if (wallet == SPARROW) {
                mBinding.info.setVisibility(View.VISIBLE);
                mBinding.info.setOnClickListener(v -> showBroadcastGuide(R.string.sparrow_broadcast_guide,
                        R.string.sparrow_broadcast_guide_content));
            }
        }
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
    }

    private void showBroadcastGuide(int title, int content) {
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

    private String getSignStatus(TxEntity txEntity) {
        String signStatus = txEntity.getSignStatus();

        String[] splits = signStatus.split("-");
        int sigNumber = Integer.parseInt(splits[0]);
        int reqSigNumber = Integer.parseInt(splits[1]);

        String text;
        if (sigNumber == 0) {
            text = getString(R.string.unsigned);
        } else if (sigNumber < reqSigNumber) {
            text = getString(R.string.partial_signed);
        } else {
            text = getString(R.string.signed);
            signed = true;
        }

        return text;
    }

    private String getSignStatus(CasaSignature txEntity) {
        String signStatus = txEntity.getSignStatus();

        String[] splits = signStatus.split("-");
        int sigNumber = Integer.parseInt(splits[0]);
        int reqSigNumber = Integer.parseInt(splits[1]);

        String text;
        if (sigNumber == 0) {
            text = getString(R.string.unsigned);
        } else if (sigNumber < reqSigNumber) {
            text = getString(R.string.partial_signed);
        } else {
            text = getString(R.string.signed);
            signed = true;
        }

        return text;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
