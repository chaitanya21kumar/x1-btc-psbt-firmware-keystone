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

import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_IS_CHANGE_ADDRESS;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.getAccount;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.AddressFragmentBinding;
import com.x1-btc-psbt-firmware.cold.db.entity.AddressEntity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.util.Keyboard;
import com.x1-btc-psbt-firmware.cold.viewmodel.CoinViewModel;

import java.util.List;
import java.util.Objects;

public class AddressFragment extends BaseFragment<AddressFragmentBinding> {

    private String query;
    private CoinViewModel viewModel;
    private boolean isChangeAddress;
    private String accountHdPath;
    private List<AddressEntity> addressEntities;
    private final AddressCallback mAddrCallback = new AddressCallback() {
        @Override
        public void onClick(AddressEntity addr) {
            if (mAddressAdapter.isEditing()) {
                mAddressAdapter.exitEdit();
            } else {
                Bundle bundle = Objects.requireNonNull(getArguments());
                Bundle data = new Bundle();
                data.putString(KEY_COIN_CODE, bundle.getString(KEY_COIN_CODE));
                data.putString(KEY_ADDRESS, addr.getAddressString());
                data.putString(KEY_ADDRESS_NAME, addr.getName());
                data.putString(KEY_ADDRESS_PATH, addr.getPath());
                navigate(R.id.action_to_receiveCoinFragment, data);
            }
        }

        @Override
        public void onNameChange(AddressEntity addr) {
            viewModel.updateAddress(addr);
        }
    };

    private AddressAdapter mAddressAdapter;

    public static AddressFragment newInstance(@NonNull String coinId,
                                              boolean isChange) {
        AddressFragment fragment = new AddressFragment();
        Bundle args = new Bundle();
        args.putString(KEY_COIN_ID, coinId);
        args.putString(KEY_COIN_CODE, Coins.coinCodeFromCoinId(coinId));
        args.putBoolean(KEY_IS_CHANGE_ADDRESS, isChange);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.address_fragment;
    }

    @Override
    protected void init(View view) {
        mAddressAdapter = new AddressAdapter(mActivity, mAddrCallback);
        mBinding.addrList.setAdapter(mAddressAdapter);
        mAddressAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (!TextUtils.isEmpty(query) && mAddressAdapter.getItemCount() == 0) {
                    mBinding.empty.setVisibility(View.VISIBLE);
                    mBinding.addrList.setVisibility(View.GONE);
                } else {
                    mBinding.empty.setVisibility(View.GONE);
                    mBinding.addrList.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle data = Objects.requireNonNull(getArguments());
        isChangeAddress = data.getBoolean(KEY_IS_CHANGE_ADDRESS);
        accountHdPath = Objects.requireNonNull(getAccount(mActivity)).getPath();
        Objects.requireNonNull(getParentFragment());
        viewModel = ViewModelProviders.of(getParentFragment()).get(CoinViewModel.class);
        viewModel.initDefultAddress(isChangeAddress, accountHdPath);
        subscribeUi(viewModel.getAddress());
    }

    private void subscribeUi(LiveData<List<AddressEntity>> address) {
        address.observe(this, entities -> {
            addressEntities = entities;
            updateAddressList(entities);
        });
    }

    private void updateAddressList(List<AddressEntity> entities) {
        List<AddressEntity> filteredEntity = viewModel.filterByAccountHdPath(entities, accountHdPath);
        List<AddressEntity> addressEntities;
        if (isChangeAddress) {
            addressEntities = viewModel.filterChangeAddress(filteredEntity);
        } else {
            addressEntities = viewModel.filterReceiveAddress(filteredEntity);
        }
        mAddressAdapter.setItems(addressEntities);
    }

    @Override
    public void onStop() {
        super.onStop();
        Keyboard.hide(mActivity, Objects.requireNonNull(getView()));
    }
}
