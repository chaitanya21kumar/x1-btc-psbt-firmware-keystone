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

import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment.ACTION;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.ManageWalletBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ModalWithTwoButtonBinding;
import com.x1-btc-psbt-firmware.cold.databinding.MultisigWalletItemBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;
import com.x1-btc-psbt-firmware.cold.ui.fragment.setup.PreImportFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.ui.views.AuthenticateModal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManageWalletFragment extends MultiSigBaseFragment<ManageWalletBinding> {

    private Adapter adapter;
    private String defaultWalletFp;

    @Override
    protected int setView() {
        return R.layout.manage_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        adapter = new Adapter(mActivity);
        mBinding.list.setAdapter(adapter);
        multiSigViewModel.getAllMultiSigWallet().observe(this, multiSigWalletEntities -> {
            multiSigWalletEntities = filterNetwork(multiSigWalletEntities);
            if (!multiSigWalletEntities.isEmpty()) {
                adapter.setItems(multiSigWalletEntities);
                mBinding.empty.setVisibility(View.GONE);
                mBinding.list.setVisibility(View.VISIBLE);
                mBinding.text.setVisibility(View.VISIBLE);
            } else {
                mBinding.empty.setVisibility(View.VISIBLE);
                mBinding.list.setVisibility(View.GONE);
                mBinding.text.setVisibility(View.GONE);
            }
        });
        subscribeGetCurrentWallet();
    }

    private List<MultiSigWalletEntity> filterNetwork(List<MultiSigWalletEntity> multiSigWalletEntities) {
        String netmode = Utilities.isMainNet(mActivity) ? "main" : "testnet";
        multiSigWalletEntities.sort((o1, o2) -> -1);
        return multiSigWalletEntities.stream()
                .filter(w->w.getNetwork().equals(netmode))
                .collect(Collectors.toList());
    }

    private void subscribeGetCurrentWallet() {
        multiSigViewModel.getCurrentWallet().observe(this, w -> {
            if (w != null) {
                defaultWalletFp = w.getWalletFingerPrint();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAlert(MultiSigWalletEntity item) {
        ModalDialog dialog = new ModalDialog();
        ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.modal_with_two_button, null, false);
        binding.title.setText(R.string.delete_wallet_title);
        binding.subTitle.setText(R.string.delete_wallet_hint);
        binding.left.setText(R.string.cancel);
        binding.left.setOnClickListener(v -> dialog.dismiss());
        binding.right.setText(R.string.confirm);
        binding.right.setOnClickListener(v -> {
            dialog.dismiss();
            final Runnable forgetPassword = () -> {
                Bundle bundle = new Bundle();
                bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
                navigate(R.id.action_to_preImportFragment, bundle);
            };

            AuthenticateModal.show(mActivity, getString(R.string.password_modal_title), "",
                    token -> AppExecutors.getInstance().diskIO().execute(() -> {
                        multiSigViewModel.deleteWallet(item.getWalletFingerPrint());
                        mActivity.runOnUiThread(ManageWalletFragment.this::subscribeGetCurrentWallet);
                    }),
                    forgetPassword);
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    class Adapter extends BaseBindingAdapter<MultiSigWalletEntity, MultisigWalletItemBinding> {

        Adapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.multisig_wallet_item;
        }

        @Override
        protected void onBindItem(MultisigWalletItemBinding binding, MultiSigWalletEntity item) {
            binding.setWalletItem(item);

            if (item.getWalletFingerPrint().equals(defaultWalletFp)) {
                binding.checked.setVisibility(View.VISIBLE);
            } else {
                binding.checked.setVisibility(View.GONE);
            }
            binding.delete.setOnClickListener(v -> {
                showAlert(item);
                mBinding.list.closeMenu();

            });
            binding.root.setOnClickListener(v -> {
                defaultWalletFp = item.getWalletFingerPrint();
                multiSigViewModel.setDefaultMultisigWallet(defaultWalletFp);
                adapter.notifyDataSetChanged();
            });
        }
    }
}
