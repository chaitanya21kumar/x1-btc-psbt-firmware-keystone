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

package com.x1-btc-psbt-firmware.cold.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.CommonModalBinding;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.ui.modal.ProgressModalDialog;

import java.util.Objects;

public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {
    private static final boolean DEBUG = false;
    private ModalDialog dialog;
    private ProgressModalDialog progressModalDialog;
    protected final String TAG = getClass().getSimpleName();

    protected T mBinding;
    protected AppCompatActivity mActivity;
    private NavController mNavController;

    protected abstract int setView();

    protected abstract void init(View view);

    protected abstract void initData(Bundle savedInstanceState);

    private void log(String s) {
        if (DEBUG) {
            Log.d(TAG, s);
        }
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) getActivity();
        log("onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        log("onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, setView(), container, false);
        log("onCreateView");
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        log("onViewCreated");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData(savedInstanceState);
        log("onActivityCreated");
        try {
            mNavController = Navigation.findNavController(mActivity, getId());
        } catch (IllegalStateException e) {
            Log.i(TAG, "onViewCreated: " + e.getMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        log("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        log("onDetach");
    }

    public void navigateUp() {
        AppExecutors.getInstance().mainThread().execute(() -> mNavController.popBackStack());
    }

    public MutableLiveData<String> getScanResult() {
        return Objects.requireNonNull(mNavController.getCurrentBackStackEntry()).getSavedStateHandle().getLiveData("scan_result");
    }

    public void setScanResult(String value) {
        Objects.requireNonNull(mNavController.getCurrentBackStackEntry()).getSavedStateHandle().set("scan_result", value);
    }

    public void popBackStack(@IdRes int id, boolean inclusive) {
        try {
            AppExecutors.getInstance().mainThread().execute(() -> mNavController.popBackStack(id, inclusive));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void navigate(@IdRes int id) {
        navigate(id, null);
    }

    public void navigate(@IdRes int id, Bundle data) {
        try {
            dismissLoading();
            AppExecutors.getInstance().mainThread().execute(() -> mNavController.navigate(id, data));
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            alert(mActivity.getString(R.string.hint), mActivity.getString(R.string.unknown_error));
        }
    }

    public AppCompatActivity getHostActivity() {
        return mActivity;
    }

    public void alert(String message) {
        alert(null, message);
    }

    public void alert(String title, String message) {
        alert(title, message, null);
    }

    public void alert(String title, String message, Runnable run) {
        dismissLoading();
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        if (title != null) {
            binding.title.setText(title);
        } else {
            binding.title.setText(R.string.fail);
        }
        binding.subTitle.setText(message);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (run != null) {
                run.run();
            }
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "failed");
    }

    public void showLoading(String tag) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            if (progressModalDialog == null) {
                progressModalDialog = ProgressModalDialog.newInstance();
            } else {
                progressModalDialog.dismiss();
            }
            progressModalDialog.show(mActivity.getSupportFragmentManager(), tag);
        });
    }

    public void dismissLoading() {
        AppExecutors.getInstance().mainThread().execute(() -> {
            if (progressModalDialog != null) {
                progressModalDialog.dismiss();
            }
        });
    }

}

