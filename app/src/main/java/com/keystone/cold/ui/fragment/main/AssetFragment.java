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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.AddAddressBottomSheetBinding;
import com.x1-btc-psbt-firmware.cold.databinding.AssetFragmentBinding;
import com.x1-btc-psbt-firmware.cold.databinding.DialogBottomSheetBinding;
import com.x1-btc-psbt-firmware.cold.db.PresetData;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScannerViewModel;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.scanstate.AssetScannerState;
import com.x1-btc-psbt-firmware.cold.ui.modal.ProgressModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.AddAddressViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.getAddressType;
import static com.x1-btc-psbt-firmware.cold.viewmodel.WatchWallet.getWatchWallet;

public class AssetFragment extends BaseFragment<AssetFragmentBinding>
        implements NumberPickerCallback {

    public static final String TAG = "AssetFragment";
    private Fragment[] fragments;
    private String coinId;
    private String[] title;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.asset_fragment;
    }

    @Override
    protected void init(View view) {
        coinId = Utilities.isMainNet(mActivity) ? Coins.BTC.coinId() : Coins.XTN.coinId();
        watchWallet = getWatchWallet(mActivity);
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        String walletName = watchWallet.getWalletName(mActivity);
        mBinding.toolbar.setTitle(walletName);
        mBinding.fab.setOnClickListener(v -> addAddress());
        title = new String[]{getString(R.string.tab_my_address), getString(R.string.tab_my_change_address)};
        initViewPager();
    }

    private void addAddress() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        AddAddressBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.add_address_bottom_sheet, null, false);
        String[] displayValue = IntStream.range(0, 9)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        binding.setValue(1);
        binding.title.setText(getString(R.string.select_address_num, title[mBinding.tab.getSelectedTabPosition()]));
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.picker.setValue(0);
        binding.picker.setDisplayedValues(displayValue);
        binding.picker.setMinValue(0);
        binding.picker.setMaxValue(8);
        binding.picker.setOnValueChangedListener((picker, oldVal, newVal) -> binding.setValue(newVal + 1));
        binding.confirm.setOnClickListener(v -> {
            onValueSet(binding.picker.getValue() + 1);
            dialog.dismiss();

        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.asset_hasmore, menu);
        if (!watchWallet.supportSdcard()) {
            menu.findItem(R.id.action_sdcard).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initViewPager() {
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = AddressFragment.newInstance(coinId, false);
            fragments[1] = AddressFragment.newInstance(coinId, true);
        }
        mBinding.viewpager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewpager);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        checkAndAddNewCoins();
    }

    private void checkAndAddNewCoins() {
        SetupVaultViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(SetupVaultViewModel.class);
        AppExecutors.getInstance().diskIO().execute(()
                -> viewModel.presetData(PresetData.generateCoins(mActivity), null)
        );

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_more:
                showBottomSheetMenu();
                break;
            case R.id.action_scan:
                scanQrCode();
                break;
            case R.id.action_sdcard:
                showFileList();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFileList() {
        switch (watchWallet) {
            case ELECTRUM:
            case WASABI:
            case BTCPAY:
            case GENERIC:
            case SPARROW:
                navigate(R.id.action_to_psbtListFragment);
                break;
        }
    }

    private void scanQrCode() {
        AndPermission.with(this)
                .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                .onGranted(permissions -> {
                    ViewModelProviders.of(mActivity).get(ScannerViewModel.class)
                            .setState(new AssetScannerState(Arrays.asList(ScanResultTypes.PLAIN_TEXT,
                                    ScanResultTypes.UR_CRYPTO_PSBT, ScanResultTypes.UR_BYTES)));
                    navigate(R.id.action_to_scanner);
                })
                .onDenied(permissions -> {
                    Uri packageURI = Uri.parse("package:" + mActivity.getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(mActivity, getString(R.string.scan_permission_denied), Toast.LENGTH_LONG).show();
                }).start();
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        DialogBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.dialog_bottom_sheet, null, false);
        binding.exportXpub.setOnClickListener(v -> {
            switch (watchWallet) {
                case ELECTRUM:
                    navigate(R.id.export_electrum_ypub);
                    break;
                case KEYSTONE:
                    navigate(R.id.export_xpub_x1-btc-psbt-firmware);
                    break;
                case WASABI:
                    navigate(R.id.action_to_export_xpub_guide);
                    break;
                case BTCPAY:
                case GENERIC:
                case SPARROW:
                    navigate(R.id.action_to_export_xpub_generic);
                    break;
                case BLUE:
                    navigate(R.id.action_to_export_xpub_blue);
                    break;
            }
            dialog.dismiss();

        });

        binding.signHistory.setOnClickListener(v -> {
            Bundle data = new Bundle();
            data.putString(KEY_COIN_ID, coinId);
            navigate(R.id.action_to_txList, data);
            dialog.dismiss();

        });

        binding.walletInfo.setOnClickListener(v -> {
            navigate(R.id.action_to_walletInfoFragment);
            dialog.dismiss();

        });

        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    @Override
    public void onValueSet(int value) {
        AddAddressViewModel viewModel = ViewModelProviders.of(this)
                .get(AddAddressViewModel.class);

        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coinEntity = viewModel.getCoin(coinId);
            if (coinEntity != null) {

                int tabPosition = mBinding.tab.getSelectedTabPosition();
                int changeIndex;
                if (tabPosition == 0) {
                    changeIndex = 0;
                } else {
                    changeIndex = 1;
                }

                viewModel.addAddress(value, getAddressType(mActivity), changeIndex);
                handler.post(() -> viewModel.getObservableAddState().observe(this, complete -> {
                    if (complete) {
                        handler.postDelayed(dialog::dismiss, 500);
                    }
                }));
            }
        });
    }
}
