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

import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.ModalWithTwoButtonBinding;
import com.x1-btc-psbt-firmware.cold.databinding.MultisigWalletBinding;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.ui.views.AuthenticateModal;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;

import java.util.Objects;

public class WalletFragment extends MultiSigBaseFragment<MultisigWalletBinding>
        implements ClickHandler {
    @Override
    protected int setView() {
        return R.layout.multisig_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.setClickHandler(this);
        if (Utilities.getMultiSigMode(mActivity).equals(MultiSigMode.CARAVAN.getModeId())) {
            mBinding.exportWalletToCaravan.setVisibility(View.VISIBLE);
            mBinding.deleteWallet.setVisibility(View.VISIBLE);
            mBinding.deleteWallet.setOnClickListener(v -> showAlert());
            mBinding.exportWalletToCosigner.setOnClickListener(v -> onClick(R.id.action_export_caravan_wallet_to_cosigner));
        } else {
            mBinding.exportWalletToCosigner.setOnClickListener(v -> onClick(R.id.action_export_wallet_to_cosigner));
        }
    }

    private void showAlert() {
        ModalDialog dialog = new ModalDialog();
        ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.modal_with_two_button, null, false);
        binding.title.setText(R.string.delete_wallet_title);
        binding.subTitle.setText(R.string.delete_wallet_hint);
        binding.left.setText(R.string.cancel);
        binding.left.setOnClickListener(v -> dialog.dismiss());
        binding.right.setText(R.string.confirm);
        binding.right.setOnClickListener(v -> {
            dialog.dismiss();
            final Runnable forgetPassword = () -> {
                Bundle bundle = new Bundle();
                bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
                navigate(R.id.action_to_preImportFragment, bundle);
            };
            AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "",
                    token -> AppExecutors.getInstance().diskIO().execute(() -> {
                        multiSigViewModel.deleteWallet(requireArguments().getString("wallet_fingerprint"));
                        popBackStack(R.id.caravanMultisigFragment, false);
                    }),
                    forgetPassword);
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    public void onClick(int id) {
        Bundle data = getArguments();
        Objects.requireNonNull(data).putBoolean("setup",false);
        data.putBoolean("multisig",true);
        navigate(id, data);
    }

}

