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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.app.LocalePicker;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.SettingItemSelectableBinding;
import com.x1-btc-psbt-firmware.cold.databinding.SetupLanguageBinding;
import com.x1-btc-psbt-firmware.cold.setting.LanguageHelper;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setting.ListPreferenceCallback;
import com.x1-btc-psbt-firmware.cold.viewmodel.AboutViewModel;

import java.util.Arrays;

import static com.x1-btc-psbt-firmware.cold.setting.LanguageHelper.DEFAULT;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.SystemPreferenceFragment.SETTING_LANGUAGE;

public class SetupLanguageFragment extends BaseFragment<SetupLanguageBinding>
        implements ListPreferenceCallback {
    protected Adapter adapter;
    protected SharedPreferences prefs;
    protected CharSequence[] values;
    protected String value;
    private CharSequence[] entries;

    @Override
    protected int setView() {
        return R.layout.setup_language;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            value = savedInstanceState.getString(SETTING_LANGUAGE);
        } else {
            value = DEFAULT;
        }

        prefs = Utilities.getPrefs(mActivity);
        prefs.edit().putString(SETTING_LANGUAGE, value).apply();
        AppExecutors.getInstance().diskIO().execute(
                ()->LocalePicker.updateLocale(LanguageHelper.getLocaleByLanguage(value)));
    }

    @Override
    protected void init(View view) {
        entries = getResources().getStringArray(R.array.language_entries);
        values = getResources().getStringArray(R.array.language_values);
        adapter = new Adapter(mActivity);
        adapter.setItems(Arrays.asList(entries));
        mBinding.list.setAdapter(adapter);
        mBinding.next.setOnClickListener(this::next);
    }

    private void next(View view) {
        Utilities.setLanguageSet(mActivity);
        navigate(R.id.action_setupLanguage_to_securityCheck);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mBinding.setViewModel(ViewModelProviders.of(this).get(AboutViewModel.class));
    }

    @Override
    public void onSelect(int position) {
        String old = value;
        value = values[position].toString();
        if (!old.equals(value)) {
            prefs.edit().putString(SETTING_LANGUAGE, value).apply();
            adapter.notifyDataSetChanged();
            mActivity.recreate();
            AppExecutors.getInstance().diskIO().execute(
                    () -> LocalePicker.updateLocale(LanguageHelper.getLocaleByLanguage(value)));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SETTING_LANGUAGE,value);
    }

    class Adapter extends BaseBindingAdapter<CharSequence, SettingItemSelectableBinding> {

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
            binding.setIndex(position);
            binding.setCallback(SetupLanguageFragment.this);
            binding.setChecked(values[position].equals(value));
        }

        @Override
        protected void onBindItem(SettingItemSelectableBinding binding, CharSequence item) {
        }
    }
}
