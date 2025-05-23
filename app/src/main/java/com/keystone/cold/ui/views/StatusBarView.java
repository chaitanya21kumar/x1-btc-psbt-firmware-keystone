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

package com.x1-btc-psbt-firmware.cold.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;

import static com.x1-btc-psbt-firmware.cold.Utilities.NET_MDOE;

public class StatusBarView extends LinearLayout {

    private static final String TAG = "Vault.StatusBarView";

    private final SharedPreferences sp;

    private final OnSharedPreferenceChangeListener listener = (sp, key) -> {
        if (NET_MDOE.equals(key)) {
            updateBg();
        }
    };

    public StatusBarView(Context context) {
        this(context, null);
    }

    public StatusBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sp = Utilities.getPrefs(mContext);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateBg();
    }

    private void updateBg() {
        boolean isMainNet = Utilities.isMainNet(getContext());
        if (!isMainNet) {
            setBackgroundColor(Color.parseColor("#FFC700"));
            findViewById(R.id.text).setVisibility(VISIBLE);
            ((TextView)findViewById(R.id.text)).setText(R.string.test_net);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
            findViewById(R.id.text).setVisibility(GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
