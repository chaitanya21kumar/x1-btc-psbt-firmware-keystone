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
import android.text.InputType;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class NoSuggestionEditText extends AppCompatEditText {
    public NoSuggestionEditText(Context context) {
        super(context);
        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setTransformationMethod(null);
    }

    public NoSuggestionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setTransformationMethod(null);
    }

    public NoSuggestionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setTransformationMethod(null);
    }

}
