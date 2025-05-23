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

import com.x1-btc-psbt-firmware.coinlib.accounts.Account;
import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportSdcardModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportXpubToElectrumBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ModalWithTwoButtonBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.update.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showExportResult;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.writeToSdcard;

public class ExportXpubToElectrumFragment extends MultiSigBaseFragment<ExportXpubToElectrumBinding> {

    private ArrayList<XpubInfo> xpubs;
    private int index = 0;
    private MultiSigWalletEntity walletEntity;

    @Override
    protected int setView() {
        return R.layout.export_xpub_to_electrum;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        multiSigViewModel.getWalletEntity(data.getString("wallet_fingerprint"))
                .observe(this, walletEntity -> {
                    this.walletEntity = walletEntity;
                    try {
                        JSONArray array = new JSONArray(walletEntity.getExPubs());
                        xpubs = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {

                            xpubs.add(new XpubInfo(array.getJSONObject(i).getString("xfp"),
                                    array.getJSONObject(i).getString("xpub")));
                        }

                        updateUI();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

        mBinding.next.setOnClickListener(v -> {
            if (index < xpubs.size() - 1) {
                index++;
                updateUI();
            } else {
                popBackStack(R.id.legacyMultisigFragment, false);
            }
        });

        mBinding.prev.setOnClickListener(v -> {
            if (index > 0) {
                index--;
            }
            updateUI();
        });

        mBinding.exportToSdcard.setOnClickListener(v -> exportXpub());
        mBinding.info.setOnClickListener(v -> showElectrumInfo());
    }

    private void onBackPressed() {
        if (index < xpubs.size() - 1) {
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button, null, false);
            binding.title.setText(R.string.stop_export_xpub);
            binding.subTitle.setText(getString(R.string.stop_export_xpub_hint, xpubs.size() - 1 - index));
            binding.actionHint.setText("");
            binding.left.setText(R.string.create_later);
            binding.left.setOnClickListener(left -> {
                dialog.dismiss();
                popBackStack(R.id.legacyMultisigFragment, false);
            });
            binding.right.setText(R.string.keep_create);
            binding.right.setOnClickListener(right -> dialog.dismiss());
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        } else {
            navigateUp();
        }
    }

    private void showElectrumInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.electrum_import_xpub_guide_title);
        binding.subTitle.setText(getString(R.string.export_multisig_wallet_to_electrum_guide,
                walletEntity.getTotal(), walletEntity.getThreshold()));
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void exportXpub() {
        Storage storage = Storage.createByEnvironment();
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog modalDialog = ModalDialog.newInstance();
            ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.export_sdcard_modal, null, false);
            binding.title.setText(R.string.export_xpub_text_file);
            binding.fileName.setText(getFileName());
            binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
            binding.confirm.setOnClickListener(vv -> {
                modalDialog.dismiss();
                boolean result = writeToSdcard(storage, xpubs.get(index).xpub, getFileName());
                showExportResult(mActivity, null, result);
            });
            modalDialog.setBinding(binding);
            modalDialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private String getFileName() {
        Account account = MultiSig.ofPath(walletEntity.getExPubPath()).get((0));
        return xpubs.get(index).xfp + "-" + account.getScript() + ".txt";
    }

    private void updateUI() {
        mBinding.index.setText((index + 1) + "/" + xpubs.size());
        mBinding.qrcode.setData(xpubs.get(index).xpub);
        mBinding.expub.setText(xpubs.get(index).xpub);
        mBinding.prev.setVisibility(index > 0 ? View.VISIBLE : View.INVISIBLE);
        mBinding.next.setText(index < xpubs.size() - 1 ? R.string.next_one : R.string.complete);
        mBinding.totalKeyNumber.setText(getString(R.string.total_key_number, xpubs.size()));
    }

    static class XpubInfo {
        String xfp;
        String xpub;

        XpubInfo(String xfp, String xpub) {
            this.xfp = xfp;
            this.xpub = xpub;
        }
    }
}
