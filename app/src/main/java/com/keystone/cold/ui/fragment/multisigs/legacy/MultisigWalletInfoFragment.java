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

package com.x1-btc-psbt-firmware.cold.ui.fragment.multisigs.legacy;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.coinlib.ExtendPubkeyFormat;
import com.x1-btc-psbt-firmware.coinlib.accounts.Account;
import com.x1-btc-psbt-firmware.coinlib.accounts.ExtendedPublicKeyVersion;
import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.MultisigWalletInfoBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.util.Keyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MultisigWalletInfoFragment extends MultiSigBaseFragment<MultisigWalletInfoBinding> {

    private MultiSigWalletEntity wallet;
    private boolean isTestNet;

    @Override
    protected int setView() {
        return R.layout.multisig_wallet_info;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        isTestNet = !Utilities.isMainNet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            navigateUp();
            Keyboard.hide(mActivity, mBinding.walletName);
        });
        subscribeGetWallet(data);
    }

    private void subscribeGetWallet(Bundle data) {
        multiSigViewModel.getWalletEntity(data.getString("wallet_fingerprint"))
                .observe(this, w -> {
                    wallet = w;
                    setBindings(w);
                });
    }

    private void setBindings(MultiSigWalletEntity w) {
        mBinding.setWallet(w);
        mBinding.path.setText(w.getExPubPath() + String.format("(%s)",
                isTestNet ? getString(R.string.testnet) : getString(R.string.mainnet)));
        mBinding.setAddressType(MultiSig.ofPath(w.getExPubPath()).get(0).getScript());
        mBinding.setXpubInfo(getXpub(w));
        mBinding.showAsXpub.setOnClickListener(v -> showAsXpub());
    }

    private void showAsXpub() {
        ModalDialog dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        binding.title.setText(R.string.check_xpub_info);
        binding.subTitle.setText(getDisplayXpubInfoForCC(wallet));
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setVisibility(View.GONE);
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private String getDisplayXpubInfoForCC(MultiSigWalletEntity wallet) {
        ExtendPubkeyFormat destFormat = isTestNet ? ExtendPubkeyFormat.tpub : ExtendPubkeyFormat.xpub;
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray array = new JSONArray(wallet.getExPubs());
            for (int i = 0; i < wallet.getTotal(); i++) {
                JSONObject info = array.getJSONObject(i);
                builder.append(i + 1).append(". ").append(info.getString("xfp").toUpperCase()).append("<br>")
                        .append(ExtendPubkeyFormat.convertExtendPubkey(info.getString("xpub"),
                                destFormat)).append("<br><br>");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private String getXpub(MultiSigWalletEntity wallet) {
        Account account = MultiSig.ofPath(wallet.getExPubPath()).get(0);
        ExtendedPublicKeyVersion extendedPublicKeyVersion = account.getXPubVersion();
        if (account == Account.MULTI_P2SH && isTestNet) {
            extendedPublicKeyVersion = ExtendedPublicKeyVersion.tpub;
        }
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray array = new JSONArray(wallet.getExPubs());
            for (int i = 0; i < wallet.getTotal(); i++) {
                JSONObject info = array.getJSONObject(i);
                builder.append(i + 1).append(". ").append(info.getString("xfp")).append("\n");
                String path = info.optString("path");
                if (!path.isEmpty()) {
                    mBinding.llDerivationPath.setVisibility(View.GONE);
                    builder.append("Derivation: ").append(path).append("\n");
                }
                String xpub = info.getString("xpub");
                xpub = ExtendedPublicKeyVersion.convertXPubVersion(xpub, extendedPublicKeyVersion);
                builder.append(xpub).append("\n");
                if (i < wallet.getTotal() - 1) builder.append("\n");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
