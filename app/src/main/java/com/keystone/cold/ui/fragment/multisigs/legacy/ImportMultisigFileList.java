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

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.x1-btc-psbt-firmware.coinlib.accounts.Account;
import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.FileListBinding;
import com.x1-btc-psbt-firmware.cold.databinding.FileListItemBinding;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.electrum.Callback;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScanResult;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScannerState;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.InvalidMultisigWalletException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.MultisigWalletNetNotMatchException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.XfpNotMatchException;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.LegacyMultiSigViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.x1-btc-psbt-firmware.coinlib.Util.getExpubFingerprint;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.hasSdcard;

public class ImportMultisigFileList extends MultiSigBaseFragment<FileListBinding>
        implements Callback, Toolbar.OnMenuItemClickListener {
    private Adapter adapter;
    private AtomicBoolean showEmpty;

    @Override
    protected int setView() {
        return R.layout.file_list;
    }

    private Map<String, JSONObject> walletFiles = new HashMap<>();

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.inflateMenu(R.menu.main);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbarTitle.setText(R.string.import_multisig_wallet);
        adapter = new Adapter(mActivity, this);
        initViews();
    }

    private void initViews() {
        showEmpty = new AtomicBoolean(false);
        if (!hasSdcard()) {
            showEmpty.set(true);
            mBinding.emptyTitle.setText(R.string.no_sdcard);
            mBinding.emptyMessage.setText(R.string.no_sdcard_hint);
        } else {
            mBinding.list.setAdapter(adapter);
            multiSigViewModel.loadWalletFile().observe(this, files -> {
                walletFiles = files;
                if (files.size() > 0) {
                    List<String> fileNames = new ArrayList<>(files.keySet());
                    fileNames.sort(String::compareTo);
                    adapter.setItems(fileNames);
                } else {
                    showEmpty.set(true);
                    mBinding.emptyTitle.setText(R.string.no_multisig_wallet_file);
                    mBinding.emptyMessage.setText(R.string.no_multisig_wallet_file_hint);
                }
                updateUi();
            });
        }
        updateUi();
    }

    private void updateUi() {
        if (showEmpty.get()) {
            mBinding.emptyView.setVisibility(View.VISIBLE);
            mBinding.list.setVisibility(View.GONE);
        } else {
            mBinding.emptyView.setVisibility(View.GONE);
            mBinding.list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(String file) {
        JSONObject walletFile = walletFiles.get(file);
        boolean isWalletFileTest = walletFile.optBoolean("isTest", false);
        boolean isTestnet = !Utilities.isMainNet(mActivity);
        if (isWalletFileTest != isTestnet) {
            String currentNet = isTestnet ? getString(R.string.testnet) : getString(R.string.mainnet);
            String walletFileNet = isWalletFileTest ? getString(R.string.testnet) : getString(R.string.mainnet);
            ModalDialog.showCommonModal(mActivity, getString(R.string.import_failed),
                    getString(R.string.import_failed_network_not_match, currentNet, walletFileNet, walletFileNet),
                    getString(R.string.know), null);
            return;
        }

        Bundle data = new Bundle();
        data.putString("wallet_info", walletFile.toString());
        navigate(R.id.import_multisig_wallet, data);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            Bundle data = new Bundle();
            data.putString("purpose", "importMultiSigWallet");
            ViewModelProviders.of(mActivity).get(ScannerViewModel.class)
                    .setState(new ScannerState(Collections.singletonList(ScanResultTypes.UR_BYTES)) {
                        String currentNet;
                        String walletFileNet;

                        @Override
                        public void handleScanResult(ScanResult result) throws Exception {
                            if (handleMultisigWallet(result)) return;
                            throw new UnknowQrCodeException("Unknown qrcode");
                        }

                        @Override
                        public boolean handleException(Exception e) {
                            e.printStackTrace();
                            if (e instanceof InvalidMultisigWalletException) {
                                mFragment.alert(getString(R.string.invalid_multisig_wallet), getString(R.string.invalid_multisig_wallet_hint));
                                return true;
                            } else if (e instanceof XfpNotMatchException) {
                                mFragment.alert(getString(R.string.import_multisig_wallet_fail), getString(R.string.import_multisig_wallet_fail_hint));
                                return true;
                            } else if (e instanceof JSONException) {
                                mFragment.alert(getString(R.string.incorrect_qrcode));
                                return true;
                            } else if (e instanceof MultisigWalletNetNotMatchException) {
                                mFragment.alert(getString(R.string.import_failed),
                                        getString(R.string.import_failed_network_not_match, currentNet, walletFileNet, walletFileNet));
                                return true;
                            }
                            return super.handleException(e);
                        }

                        private boolean handleMultisigWallet(ScanResult result) throws InvalidMultisigWalletException, XfpNotMatchException, JSONException, MultisigWalletNetNotMatchException {
                            byte[] bytes = (byte[]) result.resolve();
                            String hex = Hex.toHexString(bytes);
                            JSONObject object = LegacyMultiSigViewModel.decodeColdCardWalletFile(new String(Hex.decode(hex), StandardCharsets.UTF_8));
                            if (object == null) {
                                object = LegacyMultiSigViewModel.decodeCaravanWalletFile(new String(Hex.decode(hex), StandardCharsets.UTF_8));
                            }
                            if (object != null) {
                                return handleImportMultisigWallet(object);
                            }
                            throw new InvalidMultisigWalletException("not multisig wallet");
                        }

                        public boolean handleImportMultisigWallet(JSONObject obj) throws JSONException, XfpNotMatchException, MultisigWalletNetNotMatchException {
                            LegacyMultiSigViewModel viewModel = ViewModelProviders.of(mActivity).get(LegacyMultiSigViewModel.class);
                            String xfp = viewModel.getXfp();
                            boolean isWalletFileTest = obj.optBoolean("isTest", false);
                            Account account = MultiSig.ofPath(obj.getString("Derivation"), !isWalletFileTest).get(0);
                            boolean isTestnet = !Utilities.isMainNet(mActivity);
                            if (isWalletFileTest != isTestnet) {
                                currentNet = isTestnet ? getString(R.string.testnet) : getString(R.string.mainnet);
                                walletFileNet = isWalletFileTest ? getString(R.string.testnet) : getString(R.string.mainnet);
                                throw new MultisigWalletNetNotMatchException("network not match");
                            }

                            Bundle bundle = new Bundle();
                            bundle.putString("wallet_info", obj.toString());
                            JSONArray array = obj.getJSONArray("Xpubs");
                            boolean matchXfp = false;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject xpubInfo = array.getJSONObject(i);
                                String thisXfp = xpubInfo.getString("xfp");
                                if (thisXfp.equalsIgnoreCase(xfp)
                                        || thisXfp.equalsIgnoreCase(getExpubFingerprint(viewModel.getXPub(account)))) {
                                    matchXfp = true;
                                    break;
                                }
                            }
                            if (!matchXfp) {
                                throw new XfpNotMatchException("xfp not match");
                            } else {
                                mFragment.navigate(R.id.import_multisig_wallet, bundle);
                                return true;
                            }
                        }
                    });
            navigate(R.id.action_to_scanner);
        }
        return true;
    }


    static class Adapter extends BaseBindingAdapter<String, FileListItemBinding> {
        private final Callback callback;

        Adapter(Context context, Callback callback) {
            super(context);
            this.callback = callback;
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.file_list_item;
        }

        @Override
        protected void onBindItem(FileListItemBinding binding, String item) {
            binding.setFile(item);
            binding.setCallback(callback);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
        }
    }
}
