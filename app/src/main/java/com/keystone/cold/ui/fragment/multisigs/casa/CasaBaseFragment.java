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

package com.x1-btc-psbt-firmware.cold.ui.fragment.multisigs.casa;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;

import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.CasaMultiSigViewModel;

public abstract class CasaBaseFragment<T extends ViewDataBinding>
        extends BaseFragment<T> {
    protected CasaMultiSigViewModel casaMultiSigViewModel;

    @Override
    protected void init(View view) {
        casaMultiSigViewModel = ViewModelProviders.of(mActivity).get(CasaMultiSigViewModel.class);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
    }


}
