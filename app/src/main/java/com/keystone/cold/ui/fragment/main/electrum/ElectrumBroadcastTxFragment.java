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

package com.x1-btc-psbt-firmware.cold.ui.fragment.main.electrum;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.x1-btc-psbt-firmware.coinlib.utils.Base43;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.BroadcastElectrumTxFragmentBinding;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.CoinListViewModel;

import org.spongycastle.util.encoders.Base64;

import java.util.Objects;

import static com.x1-btc-psbt-firmware.cold.ui.modal.ExportPsbtDialog.showExportPsbtDialog;
import static com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet.PSBT_MULTISIG_SIGN_ID;

public class ElectrumBroadcastTxFragment extends BaseFragment<BroadcastElectrumTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    private View.OnClickListener navUp = v -> popBackStack(R.id.assetFragment, false);
    private TxEntity txEntity;
    private boolean isMultisig;

    public static void showElectrumInfo(AppCompatActivity activity) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(activity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.electrum_broadcast_guide);
        binding.subTitle.setText(R.string.electrum_broadcast_action_guide);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    @Override
    protected int setView() {
        return R.layout.broadcast_electrum_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(navUp);
        mBinding.complete.setOnClickListener(navUp);
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            isMultisig = txEntity.getSignId().equals(PSBT_MULTISIG_SIGN_ID);
            mBinding.setCoinCode(txEntity.getCoinCode());
            String txString = getSignTxString(txEntity);
            if (isMultisig) {
                mBinding.status.setText(getString(R.string.sign_status) + ":" + getSignStatus(txEntity));
            }
            if (isMultisig) {
                navUp = v -> popBackStack(R.id.legacyMultisigFragment, false);
                mBinding.toolbar.setNavigationOnClickListener(navUp);
                mBinding.complete.setOnClickListener(navUp);
            }
            mBinding.qrcodeLayout.qrcode.setData(txString);
        });
        mBinding.hint.setOnClickListener(v -> {
            if (txEntity != null) {
                Runnable onSuccess;
                if (isMultisig) {
                    onSuccess = () -> popBackStack(R.id.legacyMultisigFragment, false);
                } else {
                    onSuccess = () -> popBackStack(R.id.assetFragment, false);
                }
                showExportPsbtDialog(mActivity, txEntity, onSuccess);
            }
        });
        mBinding.info.setOnClickListener(v -> showElectrumInfo(mActivity));
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
        }

        return text;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getSignTxString(TxEntity txEntity) {
        return Base43.encode(Base64.decode(txEntity.getSignedHex()));
    }
}
