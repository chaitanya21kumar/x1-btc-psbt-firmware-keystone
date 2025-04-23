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

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CalcMnemonicWarningBinding;
import com.x1-btc-psbt-firmware.cold.databinding.TabletQrcodeBinding;
import com.x1-btc-psbt-firmware.cold.mnemonic.MnemonicInputTable;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;

public class TabletQrcodeFragment extends SetupVaultBaseFragment<TabletQrcodeBinding> {
    @Override
    protected int setView() {
        return R.layout.tablet_qrcode;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.next.setOnClickListener(v -> next());
        mBinding.createSharding.setOnClickListener(v->navigate(R.id.action_to_shardingSettingFragment));
        mBinding.tablet.setOnClickListener(new TapHandler(() -> navigate(R.id.action_to_rollingDiceGuideFragment)));
        mBinding.egg.setOnClickListener(new TapHandler(this::createMnemonic));
    }

    private void next() {
        viewModel.setMnemonicCount(MnemonicInputTable.TWEENTYFOUR);
        navigate(R.id.action_to_generateMnemonicFragment);
    }

    private void createMnemonic() {
        ModalDialog dialog = ModalDialog.newInstance();
        CalcMnemonicWarningBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.calc_mnemonic_warning,null,false);
        dialog.setBinding(binding);
        binding.close.setOnClickListener(v->dialog.dismiss());
        binding.confirm.setOnClickListener(v-> {
            dialog.dismiss();
            navigate(R.id.action_to_createMnemonicGuide);
        });
        dialog.show(mActivity.getSupportFragmentManager(),"");
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    static class TapHandler implements View.OnClickListener {
        final int COUNTS = 3;
        final long DURATION = 3000L;
        long[] mHits = new long[COUNTS];
        private final Runnable runnable;

        TapHandler(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onClick(View v) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                mHits = new long[COUNTS];
                runnable.run();
            }
        }
    }
}
