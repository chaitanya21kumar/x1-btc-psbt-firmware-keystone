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

import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.TxBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.protocol.builder.SignTxResultBuilder;
import com.x1-btc-psbt-firmware.cold.ui.BindingAdapters;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.viewmodel.CoinListViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.x1-btc-psbt-firmware.cold.ui.fragment.main.FeeAttackChecking.KEY_DUPLICATE_TX;


public class TxFragment extends BaseFragment<TxBinding> {

    public static final String KEY_TX_ID = "txid";
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.tx;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (data.getBoolean(KEY_DUPLICATE_TX)) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.assetFragment, false);
            } else {
                navigateUp();
            }
        });
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TX_ID)).observe(this, txEntity -> {
            mBinding.setTx(txEntity);
            this.txEntity = txEntity;
            new Handler().postDelayed(() -> mBinding.qrcodeLayout.qrcode.setData(getSignTxJson(txEntity)), 500);
            refreshAmount();
            refreshFromList();
            refreshReceiveList();
            refreshTokenUI();
        });

    }

    private void refreshAmount() {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
    }

    private void refreshTokenUI() {
        String assetCode = null;
        try {
            assetCode = txEntity.getAmount().split(" ")[1];
        } catch (Exception ignore) {
        }
        if (TextUtils.isEmpty(assetCode)) {
            assetCode = txEntity.getCoinCode();
        }
        BindingAdapters.setIcon(mBinding.txDetail.icon,
                txEntity.getCoinCode(),
                assetCode);
        if (!assetCode.equals(txEntity.getCoinCode())) {
            mBinding.txDetail.coinId.setText(assetCode);
        } else {
            mBinding.txDetail.coinId.setText(Coins.coinNameOfCoinId(txEntity.getCoinId()));
        }
    }

    private void refreshReceiveList() {
        String to = txEntity.getTo();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(to);
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                if (output.optBoolean("isChange")) {
                    continue;
                }

                long value;
                Object valueObj = output.get("value");
                if (valueObj instanceof Long) {
                    value = (Long) valueObj;
                } else if(valueObj instanceof Integer) {
                    value = ((Integer) valueObj).longValue();
                } else {
                    double satoshi = Double.parseDouble(((String) valueObj).split(" ")[0]);
                    value = (long) (satoshi * Math.pow(10,8));
                }
                items.add(new TransactionItem(i,
                        value,
                        output.getString("address"),
                        txEntity.getCoinCode()
                ));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter =
                new TransactionItemAdapter(mActivity,
                        TransactionItem.ItemType.TO);
        adapter.setItems(items);
        mBinding.txDetail.toList.setVisibility(View.VISIBLE);
        mBinding.txDetail.toInfo.setVisibility(View.GONE);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        mBinding.txDetail.from.setText(from);
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray inputs = new JSONArray(from);
            for (int i = 0; i < inputs.length(); i++) {
                items.add(new TransactionItem(i,
                        inputs.getJSONObject(i).getLong("value"),
                        inputs.getJSONObject(i).getString("address"),
                        txEntity.getCoinCode()
                ));
            }
            String fromAddress = inputs.getJSONObject(0).getString("address");
            mBinding.txDetail.from.setText(fromAddress);
        } catch (JSONException ignore) {}
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getSignTxJson(TxEntity txEntity) {
        SignTxResultBuilder signTxResult = new SignTxResultBuilder();
        signTxResult.setRawTx(txEntity.getSignedHex())
                .setSignId(txEntity.getSignId())
                .setTxId(txEntity.getTxId());
        return signTxResult.build();
    }

}
