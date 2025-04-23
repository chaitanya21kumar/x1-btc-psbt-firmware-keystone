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
import static com.x1-btc-psbt-firmware.cold.update.utils.Storage.hasSdcard;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.SetupFirmwareUpgradeBinding;
import com.x1-btc-psbt-firmware.cold.ui.views.UpdatingHelper;
import com.x1-btc-psbt-firmware.cold.update.data.UpdateManifest;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;

public class FirmwareUpgradeFragment extends SetupVaultBaseFragment<SetupFirmwareUpgradeBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_firmware_upgrade;
    }

    @Override
    protected void init(View view) {
        super.init(view);

        mBinding.skip.setOnClickListener((v) -> {
            Bundle data = new Bundle();
            data.putBoolean(IS_SETUP_VAULT, true);
            viewModel.setVaultCreateStep(SetupVaultViewModel.VAULT_CREATE_STEP_WRITE_MNEMONIC);
            navigate(R.id.action_to_setupVaultFragment, data);
        });

        mBinding.confirm.setOnClickListener(v -> {
            if (hasSdcard()) {
                UpdatingHelper updatingHelper = new UpdatingHelper(mActivity);
                MutableLiveData<UpdateManifest> manifestLiveData = updatingHelper.getUpdateManifest();
                manifestLiveData.observe(this, updateManifest -> {
                    if (updateManifest != null) {
                        updatingHelper.onUpdatingDetect(updateManifest, true);
                    } else {
                        showError();
                    }
                    manifestLiveData.removeObservers(this);
                });
            } else {
                showError();
            }
        });
    }

    private void showError() {
        alert(getString(R.string.firmware_upgrade_unable), getString(R.string.firmware_upgrade_unable_description));
    }
}
