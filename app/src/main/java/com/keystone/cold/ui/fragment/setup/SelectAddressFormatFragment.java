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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.coinlib.utils.Account;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.ModalWithTwoButtonBinding;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setting.ListPreferenceFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet;

import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting. MainPreferenceFragment.SETTING_ADDRESS_FORMAT;

public class SelectAddressFormatFragment extends ListPreferenceFragment {

    public static final String KEY_NEED_CONFIRM = "need_confirm";
    private boolean needConfirm;

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        if (data != null) {
            needConfirm = data.getBoolean(KEY_NEED_CONFIRM);
        }
        subTitles = getResources().getStringArray(R.array.address_format_subtitle);
        if (!Utilities.isMainNet(mActivity)) {
            subTitles = getResources().getStringArray(R.array.address_format_subtitle_testnet);
        }
        mBinding.confirm.setVisibility(View.VISIBLE);
        mBinding.confirm.setText(R.string.next);
        mBinding.confirm.setOnClickListener(v -> next());
    }

    private void next() {
        if (WatchWallet.getWatchWallet(mActivity)
                == WatchWallet.GENERIC) {
            navigate(R.id.action_to_export_xpub_generic);
        } else {
            navigate(R.id.action_to_export_xpub_guide);
        }
    }

    @Override
    protected int getEntries() {
        if (WatchWallet.getWatchWallet(mActivity).equals(WatchWallet.GENERIC)) {
            return R.array.address_format_generic;
        } else {
            return R.array.address_format;
        }
    }

    @Override
    protected int getValues() {
        return R.array.address_format_value;
    }

    @Override
    protected String getKey() {
        return SETTING_ADDRESS_FORMAT;
    }

    @Override
    protected String defaultValue() {
        return Account.P2SH_P2WPKH.getType();
    }

    @Override
    public void onSelect(int position) {
        String old = value;
        value = values[position].toString();
        if (!old.equals(value)) {
            if (!needConfirm) {
                onSwitchAddressFormat();
                return;
            }
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button,
                    null, false);
            binding.title.setText(R.string.confirm_toggle);
            binding.subTitle.setText(R.string.toggle_address_hint);
            binding.left.setText(R.string.toggle_later);
            binding.left.setOnClickListener(v -> {
                dialog.dismiss();
                value = old;
            });
            binding.right.setText(R.string.toggle_confirm);
            binding.right.setOnClickListener(v -> {
                dialog.dismiss();
                onSwitchAddressFormat();
            });
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private void onSwitchAddressFormat() {
        prefs.edit().putString(getKey(), value).apply();
        adapter.notifyDataSetChanged();
    }
}
