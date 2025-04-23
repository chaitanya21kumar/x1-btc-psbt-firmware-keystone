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

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.BrightnessSettingBinding;
import com.x1-btc-psbt-firmware.cold.setting.BrightnessHelper;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;

public class BrightnessSettingFragment extends BaseFragment<BrightnessSettingBinding> {
    @Override
    protected int setView() {
        return R.layout.brightness_setting;
    }

    @Override
    protected void init(View view) {
        BrightnessHelper.setManualMode(mActivity);
        int brightness = BrightnessHelper.getBrightness(mActivity);
        mBinding.seekbar.setProgress(brightness);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BrightnessHelper.setBrightness(mActivity, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
