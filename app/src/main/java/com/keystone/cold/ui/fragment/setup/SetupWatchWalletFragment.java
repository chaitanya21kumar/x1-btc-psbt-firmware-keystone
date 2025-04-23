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

import static com.x1-btc-psbt-firmware.cold.Utilities.IS_SETUP_VAULT;

import android.os.Bundle;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.SetupWatchWalletBinding;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;

public class SetupWatchWalletFragment extends SetupVaultBaseFragment<SetupWatchWalletBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_watch_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putBoolean(IS_SETUP_VAULT, ((SetupVaultActivity) mActivity).inSetupProcess);
            navigate(R.id.action_to_export_xpub_guide, data);
        });
    }
}
