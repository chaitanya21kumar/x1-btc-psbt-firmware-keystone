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

package com.x1-btc-psbt-firmware.cold.ui.fragment.setup;

import android.os.Bundle;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.RollingDiceGuideBinding;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;

public class RollingDiceGuideFragment extends BaseFragment<RollingDiceGuideBinding> {
    @Override
    protected int setView() {
        return R.layout.rolling_dice_guide;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.start.setOnClickListener(v -> navigate(R.id.action_to_rollingDiceFragment));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
