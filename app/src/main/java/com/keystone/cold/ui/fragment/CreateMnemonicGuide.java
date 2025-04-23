/*
 *
 *  Copyright (c) 2021 X1-BTC-PSBT-Firmware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file LICENSE.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.x1-btc-psbt-firmware.cold.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CreateMnemonicGuideBinding;
import com.x1-btc-psbt-firmware.cold.mnemonic.MnemonicInputTable;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetupVaultBaseFragment;

public class CreateMnemonicGuide extends SetupVaultBaseFragment<CreateMnemonicGuideBinding> {

    @Override
    protected int setView() {
        return R.layout.create_mnemonic_guide;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            viewModel.setIsCreateMnemonic(false);
            navigateUp();
        });
        mBinding.done.setOnClickListener(v ->calculateLastWord());
    }

    private void calculateLastWord() {
        viewModel.setMnemonicCount(MnemonicInputTable.TWEENTYFOUR);
        viewModel.setIsCreateMnemonic(true);
        navigate(R.id.action_to_createMnemonic);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
