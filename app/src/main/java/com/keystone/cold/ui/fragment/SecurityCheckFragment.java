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

import static com.x1-btc-psbt-firmware.cold.selfcheck.SecurityCheck.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.SecurityCheckBinding;
import com.x1-btc-psbt-firmware.cold.selfcheck.SecurityCheck;
import com.x1-btc-psbt-firmware.cold.ui.AttackWarningActivity;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;

import java.util.concurrent.Executors;

public class SecurityCheckFragment extends BaseFragment<SecurityCheckBinding> {

    @Override
    protected int setView() {
        return R.layout.security_check;
    }

    @Override
    protected void init(View view) {
        Handler handler = new Handler();
        Executors.newSingleThreadExecutor().execute(() -> {
            SecurityCheck.CheckResult checkResult = new SecurityCheck().doSelfCheck(mActivity);
            handler.postDelayed(() -> {
                if (checkResult.result == RESULT_OK) {
                    boolean setupFinished = Utilities.getVaultCreateStep(mActivity).equals(SetupVaultViewModel.VAULT_CREATE_STEP_DONE);
                    Utilities.setAttackDetected(mActivity, false);
                    Log.d(TAG, "setupFinished = " + setupFinished);
                    Intent intent;
                    if (setupFinished) {
                        intent = new Intent(mActivity, MainActivity.class);
                    } else {
                        intent = new Intent(mActivity, SetupVaultActivity.class);
                        intent.putExtra("check_updating", true);
                    }
                    mActivity.startActivity(intent);
                    mActivity.finish();
                } else {
                    Utilities.setAttackDetected(mActivity, true);
                    Bundle data = new Bundle();
                    data.putInt("firmware", checkResult.firmwareStatusCode);
                    data.putInt("system", checkResult.systemStatusCode);
                    data.putInt("signature", checkResult.signatureStatusCode);
                    Intent intent = new Intent(mActivity, AttackWarningActivity.class);
                    intent.putExtras(data);
                    mActivity.startActivity(intent);
                }
            }, 1500);
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
