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

import com.android.internal.app.LocalePicker;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.setting.LanguageHelper;

import static com.x1-btc-psbt-firmware.cold.setting.LanguageHelper.DEFAULT;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.SystemPreferenceFragment.SETTING_LANGUAGE;

public class LanguagePreferenceFragment extends ListPreferenceFragment {

    @Override
    protected int getEntries() {
        return R.array.language_entries;
    }

    @Override
    protected int getValues() {
        return R.array.language_values;
    }

    @Override
    protected String getKey() {
        return SETTING_LANGUAGE;
    }

    @Override
    protected String defaultValue() {
        return DEFAULT;
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
}

