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
import android.os.Handler;
import android.text.TextUtils;
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
import com.x1-btc-psbt-firmware.cold.databinding.ExportSuccessBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ImportWalletBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.util.HashUtil;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.InvalidMultisigPathException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.XfpNotMatchException;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImportWalletFragment extends MultiSigBaseFragment<ImportWalletBinding> {

    private JSONObject walletInfo;
    private Account account;
    private int threshold;
    private String creator;
    private MultiSigWalletEntity dummyWallet;
    private boolean isTestNet;

    @Override
    protected int setView() {
        return R.layout.import_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        isTestNet = !Utilities.isMainNet(mActivity);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        dummyWallet = constructWalletEntity(data);
        mBinding.setWallet(dummyWallet);
        mBinding.path.setText(dummyWallet.getExPubPath() + String.format("(%s)",
                isTestNet ? getString(R.string.testnet) : getString(R.string.mainnet)));
        mBinding.setAddressType(MultiSig.ofPath(dummyWallet.getExPubPath()).get(0).getScript());
        mBinding.setXpubInfo(getXpub(dummyWallet));
        mBinding.confirm.setOnClickListener(v -> showVerifyCode());
        mBinding.cancel.setOnClickListener(v -> navigateUp());
        showCheckDialog();

    }

    private void showCheckDialog() {
        ModalDialog.showCommonModal(mActivity, getString(R.string.please_check_multisig_wallet_info),
                getString(R.string.check_multisig_wallet_hint),
                getString(R.string.know), null);
    }

    private void showVerifyCode() {
        if ("X1-BTC-PSBT-Firmware".equals(creator) || "Caravan".equals(creator)) {
            try {
                List<String> xpubs = new ArrayList<>();
                JSONArray array = new JSONArray(dummyWallet.getExPubs());
                for (int i = 0; i < array.length(); i++) {
                    xpubs.add(array.getJSONObject(i).getString("xpub"));
                }
                ModalDialog dialog = new ModalDialog();
                CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.common_modal, null, false);
                binding.title.setText(R.string.verify_multisig_wallet);
                binding.subTitle.setText(getString(R.string.verify_wallet_hint,
                        multiSigViewModel.calculateWalletVerifyCode(threshold, xpubs, account.getPath())));
                binding.close.setVisibility(View.GONE);
                binding.confirm.setText(R.string.verify_code_ok);
                binding.confirm.setOnClickListener(v -> {
                    importWallet();
                    dialog.dismiss();
                });
                binding.btn1.setVisibility(View.VISIBLE);
                binding.btn1.setText(R.string.error_verify_code);
                binding.btn1.setOnClickListener(v -> dialog.dismiss());
                dialog.setBinding(binding);
                dialog.show(mActivity.getSupportFragmentManager(), "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            importWallet();
        }
    }

    private MultiSigWalletEntity constructWalletEntity(Bundle data) {
        try {
            walletInfo = new JSONObject(data.getString("wallet_info"));
            threshold = Integer.parseInt(walletInfo.getString("Policy").split(" of ")[0]);
            int total = Integer.parseInt(walletInfo.getString("Policy").split(" of ")[1]);
            account = MultiSig.ofPath(walletInfo.getString("Derivation"), Utilities.isMainNet(mActivity)).get(0);
            creator = walletInfo.optString("Creator");
            JSONArray array = walletInfo.getJSONArray("Xpubs");

            for (int i = 0; i < array.length(); i++) {
                String xpub = array.getJSONObject(i).getString("xpub");
                if (TextUtils.equals(mode, "caravan")) {
                    if (isTestNet) {
                        xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.tpub);
                    } else {
                        xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.xpub);
                    }
                    array.getJSONObject(i).put("xpub", xpub);
                } else {
                    array.getJSONObject(i).put("xpub", ExtendedPublicKeyVersion.convertXPubVersion(xpub, account.getXPubVersion()));
                }
            }

            return new MultiSigWalletEntity(walletInfo.optString("Name", "KV_Multi_" + Hex.toHexString(Objects.requireNonNull(HashUtil.sha256(data.getString("wallet_info")))).substring(0, 6).toUpperCase()),
                    threshold, total, account.getPath(), array.toString(), "", "", "", creator, mode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void importWallet() {
        try {
            multiSigViewModel.createMultisigWallet(threshold, account, dummyWallet.getWalletName(), walletInfo.getJSONArray("Xpubs"), creator)
                    .observe(this, this::onImportWalletSuccess);
        } catch (XfpNotMatchException e) {
            e.printStackTrace();
            ModalDialog.showCommonModal(mActivity, getString(R.string.import_failed),
                    getString(R.string.not_include_current_vault)
                    , getString(R.string.know), null);
        } catch (JSONException e) {
            e.printStackTrace();
            ModalDialog.showCommonModal(mActivity, getString(R.string.not_valid_multisig_wallet),
                    getString(R.string.invalid_wallet_hint)
                    , getString(R.string.know), null);
        } catch (InvalidMultisigPathException e) {
            e.printStackTrace();
            ModalDialog.showCommonModal(mActivity, getString(R.string.import_failed),
                    getString(R.string.not_valid_path),
                    getString(R.string.know),
                    null);
        }

    }

    private void onImportWalletSuccess(MultiSigWalletEntity walletEntity) {
        Handler handler = new Handler();
        if (walletEntity != null) {
            ModalDialog dialog = ModalDialog.newInstance();
            ExportSuccessBinding binding =
                    DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.export_success,
                            null, false);
            binding.text.setText(R.string.import_success);
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
            handler.postDelayed(() -> {
                dialog.dismiss();
                if (Utilities.getMultiSigMode(mActivity).equals(MultiSigMode.CARAVAN.getModeId())) {
                    popBackStack(R.id.caravanMultisigFragment, false);
                } else {
                    popBackStack(R.id.legacyMultisigFragment, false);
                }
            }, 500);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getXpub(MultiSigWalletEntity wallet) {
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray array = new JSONArray(wallet.getExPubs());
            for (int i = 0; i < wallet.getTotal(); i++) {
                JSONObject info = array.getJSONObject(i);
                String xpub = info.getString("xpub");
                builder.append(i + 1).append(". ").append(info.getString("xfp")).append("\n");
                if (creator.equals("Coldcard") || creator.equals("Caravan") || TextUtils.equals(mode, "caravan")) {
                    if (isTestNet) {
                        xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.tpub);
                    } else {
                        xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.xpub);
                    }
                }
                String path = info.optString("path");
                if (!path.isEmpty()) {
                    mBinding.llDerivationPath.setVisibility(View.GONE);
                    builder.append("Derivation: ").append(path).append("\n");
                }
                builder.append(xpub).append("\n\n");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
