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

package com.x1-btc-psbt-firmware.cold.ui.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.BlueExportBinding;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoAccount;

public class BlueWalletExportFragment extends BaseFragment<BlueExportBinding> {
    @Override
    protected int setView() {
        return R.layout.blue_export;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        setQrCode();
        mBinding.info.setOnClickListener(v -> showBlueWalletInfo());
        mBinding.done.setOnClickListener(v -> {
            if (mActivity instanceof SetupVaultActivity) {
                startActivity(new Intent(mActivity, MainActivity.class));
                mActivity.finish();
            } else {
                MainActivity activity = (MainActivity) mActivity;
                activity.getNavController().popBackStack(R.id.assetFragment, false);
            }
        });
    }

    private void setQrCode() {
        CryptoAccount account = GlobalViewModel.generateCryptoAccount(mActivity);
        UR ur = account.toUR();
        mBinding.qrcode.setData(ur.toString().toUpperCase());
        mBinding.enlarge.setOnClickListener(v -> mBinding.qrcode.showModal());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void showBlueWalletInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.export_xpub_guide_text1_blue);
        binding.subTitle.setText(R.string.export_xpub_guide_text2_blue_info);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }
}
