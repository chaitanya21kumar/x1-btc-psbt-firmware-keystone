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

package com.x1-btc-psbt-firmware.cold.ui.fragment.main;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.TxDetailItemBinding;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;

import java.util.Collections;
import java.util.List;

public class TransactionItemAdapter extends BaseBindingAdapter<TransactionItem, TxDetailItemBinding> {

    private final TransactionItem.ItemType type;
    private final List<String> changeAddress;

    public TransactionItemAdapter(Context context, TransactionItem.ItemType type) {
        this(context, type, Collections.emptyList());
    }

    public TransactionItemAdapter(Context context,
                                  TransactionItem.ItemType type,
                                  List<String> changeAddress) {
        super(context);
        this.type = type;
        this.changeAddress = changeAddress;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.tx_detail_item;
    }

    @Override
    protected void onBindItem(TxDetailItemBinding binding, TransactionItem item) {
        boolean isChange = changeAddress.contains(item.address) || !TextUtils.isEmpty(item.changePath);
        if (getItemCount() == 1 && type == TransactionItem.ItemType.TO) {
            binding.info.setText(item.address);
            binding.label.setText(context.getString(R.string.tx_to));
        } else {
            String info;
            if (!TextUtils.isEmpty(item.changePath)) {
                info = item.amount + "\n" + item.address + "\n(" + item.changePath + ")";
            } else {
                info = item.amount + "\n" + item.address;
            }
            binding.info.setText(info);
            binding.label.setText(getLabel(item.id));
        }
        if (isChange && (type == TransactionItem.ItemType.TO
                || type == TransactionItem.ItemType.OUTPUT)) {
            binding.change.setVisibility(View.VISIBLE);
        } else {
            binding.change.setVisibility(View.GONE);
        }
    }

    private String getLabel(int index) {
        int resId;
        switch (type) {
            case TO:
                resId = R.string.receive_address;
                break;
            case FROM:
                resId = R.string.send_address;
                break;
            case INPUT:
                resId = R.string.input;
                break;
            case OUTPUT:
                resId = R.string.output;
                break;
            default:
                return "";
        }

        return context.getString(resId, index + 1);
    }
}
