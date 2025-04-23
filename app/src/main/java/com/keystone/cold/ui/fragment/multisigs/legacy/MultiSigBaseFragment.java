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

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;

import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.CaravanMultiSigViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.LegacyMultiSigViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;

public abstract class MultiSigBaseFragment<T extends ViewDataBinding>
        extends BaseFragment<T> {
    protected LegacyMultiSigViewModel multiSigViewModel;
    protected String mode;

    @Override
    protected void init(View view) {
        if (Utilities.getMultiSigMode(mActivity).equals(MultiSigMode.CARAVAN.getModeId())) {
            multiSigViewModel = ViewModelProviders.of(mActivity).get(CaravanMultiSigViewModel.class);
            mode = "caravan";
        } else {
            multiSigViewModel = ViewModelProviders.of(mActivity).get(LegacyMultiSigViewModel.class);
            mode = "generic";
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
    }


}
