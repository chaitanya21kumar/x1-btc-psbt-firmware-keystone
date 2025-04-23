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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.SyncFragmentBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.CoinListViewModel;

import java.util.List;

public class SyncFragment extends SetupVaultBaseFragment<SyncFragmentBinding> {

    public static final String TAG = "SyncFragment";
    private CoinListViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.sync_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        subscribe(viewModel.getCoins());
        if (mActivity instanceof MainActivity) {
            mBinding.complete.setOnClickListener(v -> popBackStack(R.id.assetFragment, false));
        } else {
            mBinding.complete.setOnClickListener(v -> {
                startActivity(new Intent(mActivity, MainActivity.class));
                mActivity.finish();
            });
        }
        mBinding.sync.info.setOnClickListener(v -> showX1-BTC-PSBT-FirmwareInfo());
    }

    private void subscribe(LiveData<List<CoinEntity>> coins) {
        coins.observe(this, this::generateSyncData);
    }

    private void generateSyncData(List<CoinEntity> coinEntities) {
        if (coinEntities != null) {
            viewModel.generateSync(coinEntities).observe(this, sync -> {
                if (!TextUtils.isEmpty(sync)) {
                    mBinding.sync.qrcodeLayout.qrcode.setData(sync);
                }
            });
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void showX1-BTC-PSBT-FirmwareInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.export_xpub_guide_text1_x1-btc-psbt-firmware);
        binding.subTitle.setText(R.string.export_xpub_guide_text2_x1-btc-psbt-firmware_info);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }
}
