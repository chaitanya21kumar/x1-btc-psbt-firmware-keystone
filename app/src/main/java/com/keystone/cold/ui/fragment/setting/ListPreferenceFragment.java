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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.ListPreferenceBinding;
import com.x1-btc-psbt-firmware.cold.databinding.SettingItemSelectableBinding;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;

import java.util.Arrays;

import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_TITLE;

public abstract class ListPreferenceFragment
        extends BaseFragment<ListPreferenceBinding> implements ListPreferenceCallback {

    protected Adapter adapter;
    protected SharedPreferences prefs;
    protected CharSequence[] values;
    protected String value;
    protected CharSequence[] entries;
    protected CharSequence[] subTitles;

    @Override
    protected int setView() {
        return R.layout.list_preference;
    }

    protected abstract int getEntries();

    protected abstract int getValues();

    protected abstract String getKey();

    protected abstract String defaultValue();

    @Override
    protected void init(View view) {
        Bundle data = getArguments();
        if (data != null) {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
            mBinding.toolbarTitle.setText(data.getInt(KEY_TITLE));
        } else {
            mBinding.toolbar.setVisibility(View.GONE);
        }
        prefs = Utilities.getPrefs(mActivity);
        entries = getResources().getStringArray(getEntries());
        values = getResources().getStringArray(getValues());
        value = prefs.getString(getKey(), defaultValue());
        adapter = new Adapter(mActivity);
        adapter.setItems(Arrays.asList(entries));
        mBinding.list.setAdapter(adapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    protected class Adapter extends BaseBindingAdapter<CharSequence, SettingItemSelectableBinding> {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.setting_item_selectable;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SettingItemSelectableBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.title.setText(entries[position]);
            if (subTitles == null) {
                binding.subTitle.setVisibility(View.GONE);
            } else {
                binding.subTitle.setVisibility(View.VISIBLE);
                binding.subTitle.setText(subTitles[position]);
            }
            binding.setIndex(position);
            binding.setCallback(ListPreferenceFragment.this);
            binding.setChecked(values[position].equals(value));
        }

        @Override
        protected void onBindItem(SettingItemSelectableBinding binding, CharSequence item) {
        }
    }
}

