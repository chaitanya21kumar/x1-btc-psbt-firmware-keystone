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

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.FingerprintSettingBinding;
import com.x1-btc-psbt-firmware.cold.databinding.SettingItemWithArrowBinding;
import com.x1-btc-psbt-firmware.cold.fingerprint.FingerprintKit;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;

import java.util.List;
import java.util.Objects;

public class FingerprintSettingFragment extends BaseFragment<FingerprintSettingBinding> {

    private ListAdapter adapter;
    private FingerprintKit fingerprint;

    @Override
    protected int setView() {
        return R.layout.fingerprint_setting;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        fingerprint = new FingerprintKit(mActivity);
        adapter = new ListAdapter(mActivity);
        refreshList();
        mBinding.add.setOnClickListener(v ->
                navigate(R.id.action_to_fingerprintEnrollFragment, getArguments()));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void refreshList() {
        List<Fingerprint> fingerprints = fingerprint.getEnrolledFingerprints();
        mBinding.add.setVisibility(fingerprints.size() >= 5 ? View.GONE : View.VISIBLE);
        adapter.setItems(fingerprints);
        mBinding.list.setAdapter(adapter);
        mBinding.list.setHasFixedSize(true);
    }

    class ListAdapter extends BaseBindingAdapter<Fingerprint, SettingItemWithArrowBinding> {
        ListAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.setting_item_with_arrow;
        }

        @Override
        protected void onBindItem(SettingItemWithArrowBinding binding, Fingerprint item) {
            binding.title.setText(item.getName());
            Bundle data = getArguments();
            binding.getRoot().setOnClickListener(v -> {
                Objects.requireNonNull(data).putParcelable("xfp", item);
                navigate(R.id.action_to_fingerprintManage, data);
            });
        }
    }
}


