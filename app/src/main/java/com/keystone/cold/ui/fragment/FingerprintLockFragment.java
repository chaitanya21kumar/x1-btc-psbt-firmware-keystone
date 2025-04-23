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
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.FingerprintLockFragmentBinding;
import com.x1-btc-psbt-firmware.cold.ui.UnlockActivity;

import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.IS_FORCE;

public class FingerprintLockFragment extends BaseFragment<FingerprintLockFragmentBinding> {

    public static final String TAG = "FingerprintLockFragment";
    public int attemptTimes;

    private final View.OnClickListener gotoPasswordUnlock =
            v -> {
                Bundle data = new Bundle();
                data.putBoolean(IS_FORCE, false);
                navigate(R.id.action_fingerprint_to_passwordLockFragment, data);
            };

    @Override
    protected int setView() {
        return R.layout.fingerprint_lock_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.switchToPassword.setOnClickListener(gotoPasswordUnlock);
        attemptTimes = Utilities.getPatternRetryTimes(mActivity);
        ((UnlockActivity) mActivity).setStatusHint(mBinding.verifyHint);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onResume() {
        super.onResume();
        ((UnlockActivity) mActivity).startIdentify();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((UnlockActivity) mActivity).cancelIdentify();

    }
}
