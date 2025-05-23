/*
 *
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
 *
 */

package com.x1-btc-psbt-firmware.cold.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.SelectableItemBinding;

public class SelectableItem extends RelativeLayout {

    String title;
    String remindText;
    int color;
    private SelectableItemBinding binding;
    public SelectableItem(Context context) {
        this(context,null);
    }

    public SelectableItem(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SelectableItem(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray mTypedArray=context.obtainStyledAttributes(attributeSet, R.styleable.SelectableItem);
        title = mTypedArray.getString(R.styleable.SelectableItem_title);
        color = mTypedArray.getColor(R.styleable.SelectableItem_title_color, Color.WHITE);
        remindText = mTypedArray.getString(R.styleable.SelectableItem_remind_text);
        mTypedArray.recycle();
        initView(context);
    }

    public void initView(Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.selectable_item,this,true);
        binding.title.setText(title);
        binding.title.setTextColor(color);
        binding.remind.setText(remindText);
        binding.executePendingBindings();
    }

    public void setRemindText(String text) {
        this.remindText = text;
        binding.remind.setText(remindText);
    }
}
