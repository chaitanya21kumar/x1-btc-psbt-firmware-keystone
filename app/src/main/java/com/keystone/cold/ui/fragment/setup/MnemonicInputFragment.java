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

import static com.x1-btc-psbt-firmware.cold.Utilities.IS_SETUP_VAULT;
import static com.x1-btc-psbt-firmware.cold.mnemonic.MnemonicInputTable.TWENTYTHREE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.handleRuntimeStateAbnormal;
import static com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel.VAULT_CREATE_STEP_DONE;
import static com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATED;
import static com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING;
import static com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING_FAILED;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.CreateVaultModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.MnemonicInputFragmentBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ModalWithTwoButtonBinding;
import com.x1-btc-psbt-firmware.cold.db.PresetData;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;
import com.x1-btc-psbt-firmware.cold.selfcheck.RuntimeStatusCode;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.SetupVaultActivity;
import com.x1-btc-psbt-firmware.cold.ui.fragment.unlock.VerifyMnemonicFragment;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.util.Keyboard;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import iton.slip.secret.Share;
import iton.slip.secret.SharedSecretException;
import iton.slip.secret.words.Mnemonic;

public class MnemonicInputFragment extends SetupVaultBaseFragment<MnemonicInputFragmentBinding> {

    protected ModalDialog dialog;
    protected String action;
    private final List<Observable.OnPropertyChangedCallback> callbacks = new ArrayList<>();
    private boolean notMatch;

    public static boolean isValidWord(String s, String[] list) {
        return !TextUtils.isEmpty(s) && Arrays.asList(list).contains(s);
    }

    @Override
    protected int setView() {
        return R.layout.mnemonic_input_fragment;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.hint.setText(getString(R.string.input_mnemonic_hint, viewModel.getMnemonicCount().get()));

        Bundle data = getArguments();
        if (data != null) {
            viewModel.setPassword(data.getString(PASSWORD));
        }

        mBinding.table.setMnemonicNumber(viewModel.getMnemonicCount().get());
        if (viewModel.isShardingMnemonic()) {
            initImportSharding();
        } else if (viewModel.isCreateMnemonic()) {
            mBinding.hint.setText(R.string.please_input_23_words);
            mBinding.importMnemonic.setText(R.string.calculate_24th_word);
            mBinding.table.setMnemonicNumber(TWENTYTHREE);
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        } else {
            mBinding.toolbar.setNavigationOnClickListener(v -> {
                Keyboard.hide(mActivity, mBinding.table);
                navigateUp();
            });
        }

        mBinding.importMnemonic.setOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.importMnemonic);
            validateMnemonic(v);
        });
        addMnemonicChangeCallback();
        subscribeVaultState(viewModel.getVaultCreateState());
    }


    protected void addMnemonicChangeCallback() {
        mBinding.table.getWordsList().forEach(o -> {
            Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    mBinding.importMnemonic.setEnabled(mBinding.table.getWordsList()
                            .stream()
                            .allMatch(s -> isValidWord(s.get(), mBinding.table.getMnemonicList())));

                    if (viewModel.currentSequence() > 0) {
                        @SuppressWarnings("unchecked")
                        ObservableField<String> observableField = (ObservableField<String>) sender;
                        String word = observableField.get();
                        if (TextUtils.isEmpty(word)) {
                            return;
                        }
                        View view = mBinding.table.findFocus();
                        if (view != null) {
                            FrameLayout parent = (FrameLayout) view.getParent();
                            TextView textView = (TextView) parent.getChildAt(0);
                            int index = Integer.parseInt(textView.getText().toString()) - 1;
                            isInputMatchFirstSharding(index, word);
                        }
                    }
                }
            };

            o.addOnPropertyChangedCallback(callback);
            callbacks.add(callback);
        });
    }

    private void isInputMatchFirstSharding(int index, String word) {
        if (notMatch) {
            return;
        }

        Share share = viewModel.firstShare;
        int group_count = share.group_count;
        if (group_count == 1) {
            if (index > 2) return;
        } else {
            if (index > 1) return;
        }

        String[] firstShardingWords = viewModel.getShareByIndex(0).split(" ");

        if (!firstShardingWords[index].startsWith(word)) {
            notMatch = true;
            showDialog(mActivity, getString(R.string.notice),
                    getString(R.string.sharding_id_not_match),
                    getString(R.string.cancel_import_sharding),
                    getString(R.string.confirm),
                    () -> {
                        mBinding.table.getWordsList().get(index).set("");
                        mBinding.table.getChildAt(index).requestFocus();
                        notMatch = false;
                        cancelImportSharding();
                    },
                    () -> {
                        mBinding.table.getWordsList().get(index).set("");
                        mBinding.table.getChildAt(index).requestFocus();
                        notMatch = false;
                    });
        }
    }

    protected void clearInput() {
        removeMnemonicChangeCallback();
        mBinding.table.getWordsList().forEach(word -> word.set(""));
        mBinding.importMnemonic.setEnabled(false);
        notMatch = false;
        mBinding.table.postDelayed(this::addMnemonicChangeCallback, 1000);
    }

    private void removeMnemonicChangeCallback() {
        for (int i = 0; i < mBinding.table.getWordsList().size(); i++) {
            mBinding.table.getWordsList().get(i).removeOnPropertyChangedCallback(callbacks.get(i));
        }
    }

    private void initImportSharding() {
        mBinding.shardingHint.setText(getString(R.string.sharding_no, viewModel.currentSequence() + 1));
        mBinding.shardingHint.setVisibility(View.VISIBLE);
        mBinding.importMnemonic.setText(R.string.next_sharding);
        mBinding.toolbar.setNavigationIcon(new ColorDrawable(Color.TRANSPARENT));
        mBinding.toolbar.setNavigationOnClickListener(null);
        mBinding.toolbar.inflateMenu(R.menu.cancel);
        mBinding.toolbar.setOnMenuItemClickListener(item -> {
            cancelImportSharding();
            return true;
        });
    }

    protected void cancelImportSharding() {
        String title;
        if (this instanceof ConfirmMnemonicFragment) {
            title = getString(R.string.ask_confirm_cancel_create_sharding);
        } else if (this instanceof VerifyMnemonicFragment) {
            if (PreImportFragment.ACTION_CHECK.equals(action)) {
                title = getString(R.string.ask_confirm_cancel_check_sharding);
            } else {
                title = getString(R.string.ask_confirm_cancel_enter_sharding);
            }
        } else {
            title = getString(R.string.ask_confirm_cancel_import_sharding);
        }

        showDialog(mActivity, title,
                getString(R.string.cancel_import_sharding_notice),
                getString(R.string.confirm_cancel_import_sharding),
                getString(R.string.continue_import_sharding),
                () -> {
                    viewModel.resetSharding();
                    Keyboard.hide(mActivity, getView());
                    navBack();
                }, null);
    }

    protected void navBack() {
        navigateUp();
    }

    protected static void showDialog(AppCompatActivity activity, String title,
                                     String text,
                                     String left,
                                     String right,
                                     Runnable leftAction,
                                     Runnable rightAction) {
        ModalDialog dialog = new ModalDialog();
        ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.modal_with_two_button,
                null, false);
        binding.title.setText(title);
        binding.subTitle.setText(text);
        binding.left.setText(left);
        binding.left.setOnClickListener(v -> {
            dialog.dismiss();
            if (leftAction != null) {
                leftAction.run();
            }
        });
        binding.right.setText(right);
        binding.right.setOnClickListener(v -> {
            dialog.dismiss();
            if (rightAction != null) {
                rightAction.run();
            }
        });
        dialog.setBinding(binding);
        dialog.show(activity.getSupportFragmentManager(), "");
    }

    void subscribeVaultState(MutableLiveData<Integer> vaultState) {
        vaultState.observe(this, state -> {
            if (state == VAULT_STATE_CREATING) {
                showModal();
            } else if (state == VAULT_STATE_CREATED) {
                Utilities.setVaultCreated(mActivity);
                Utilities.setVaultId(mActivity, viewModel.getVaultId());
                Utilities.setCurrentBelongTo(mActivity, "main");
                Runnable onComplete = () -> {
                    if (dialog != null && dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                        dialog.dismiss();
                    }
                    boolean isSetupProcess = ((SetupVaultActivity) mActivity).inSetupProcess;
                    if (isSetupProcess) {
                        viewModel.setVaultCreateStep(VAULT_CREATE_STEP_DONE);
                        Bundle data = new Bundle();
                        data.putBoolean(IS_SETUP_VAULT, isSetupProcess);
                        navigate(R.id.action_to_setupSyncFragment, data);
                    } else {
                        Intent intent = new Intent(mActivity, MainActivity.class);
                        startActivity(intent);
                        mActivity.finish();
                    }
                };

                List<CoinEntity> coins = PresetData.generateCoins(mActivity);
                viewModel.presetData(coins, onComplete);
            } else if (state == VAULT_STATE_CREATING_FAILED) {
                viewModel.getVaultCreateState().removeObservers(this);
                if (dialog != null && dialog.getDialog() != null
                        && dialog.getDialog().isShowing()) {
                    dialog.dismiss();
                }
                handleRuntimeStateAbnormal(mActivity, RuntimeStatusCode.RUNTIME_VAULT_CREATE_FAILED);
            }
        });
    }

    private void validateMnemonic(View view) {
        String mnemonic = mBinding.table.getWordsList()
                .stream()
                .map(ObservableField::get)
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");


        if (viewModel.validateMnemonic(mnemonic)) {
            if (viewModel.isShardingMnemonic()) {
                if (viewModel.getShares() == null || viewModel.getShares().size() == 0) {
                    addFirstShard(mnemonic);
                } else {
                    addSharding(mnemonic);
                }
            } else if (viewModel.isCreateMnemonic()) {
                navigateToCalculateMnemonic(mnemonic);
            } else {
                viewModel.writeMnemonic(mnemonic);
                mBinding.table.getWordsList().forEach(word -> word.set(""));
            }
        } else {
            Utilities.alert(mActivity,
                    getString(R.string.hint),
                    getString(R.string.wrong_mnemonic_please_check),
                    getString(R.string.confirm), null);
        }
    }

    private void navigateToCalculateMnemonic(String mnemonic) {
        Bundle data = new Bundle();
        data.putString("words", mnemonic);
        data.putBoolean("seed_pick", true);
        navigate(R.id.action_to_selectLastMnemonicFragment, data);
    }

    private void addFirstShard(String mnemonic) {

        try {
            Share share = Mnemonic.INSTANCE.decode(mnemonic);
            if (share.group_count == 1) {
                int remainCount = share.member_threshold - 1;
                showDialog(mActivity, getString(R.string.verify_pass),
                        getString(R.string.first_sharding_hint, remainCount),
                        getString(R.string.cancel_import_sharding),
                        getString(R.string.continue_import_sharding),
                        this::cancelImportSharding,
                        () -> addSharding(mnemonic));
            } else {
                //current not support group_count > 1
                showDialog(mActivity, getString(R.string.notice1),
                        getString(R.string.not_support_multi_group_sharding),
                        getString(R.string.cancel_import_sharding),
                        getString(R.string.confirm),
                        () -> {
                            viewModel.resetSharding();
                            navBack();
                        },
                        this::clearInput);
            }
        } catch (SharedSecretException e) {
            e.printStackTrace();
        }
    }

    protected void addSharding(String mnemonic) {
        try {
            String title = getString(R.string.notice);
            String text;
            String left = getString(R.string.cancel_import_sharding);
            String right = getString(R.string.confirm);
            Runnable leftAction = this::cancelImportSharding;
            Runnable rightAction = this::clearInput;
            SetupVaultViewModel.AddShareResult result = viewModel.addShare(mnemonic);
            switch (result) {
                case RESULT_NEED_MORE:
                    viewModel.nextSequence();
                    mBinding.shardingHint.setText(getString(R.string.sharding_no, viewModel.currentSequence() + 1));
                    rightAction.run();
                    if (viewModel.currentSequence() + 1 == viewModel.firstShare.member_threshold) {
                        mBinding.importMnemonic.setText(R.string.complete);
                    }
                    mBinding.scroll.post(() -> {
                        mBinding.scroll.fullScroll(ScrollView.FOCUS_UP);
                        mBinding.table.scrollToPosition(0);
                        mBinding.table.getChildAt(0).requestFocus();
                    });
                    break;
                case RESULT_OK:
                    onAllShardsCollect();
                    break;
                case RESULT_REPEAT:
                    text = getString(R.string.sharding_repeat);
                    showDialog(mActivity, title, text, left, right, leftAction, rightAction);
                    break;
                case RESULT_NOT_MATCH:
                    text = getString(R.string.sharding_id_not_match);
                    showDialog(mActivity, title, text, left, right, leftAction, rightAction);
            }
        } catch (SharedSecretException e) {
            e.printStackTrace();
        }
    }

    protected void onAllShardsCollect() {
        viewModel.writeShardingMasterSeed();
        mBinding.table.getWordsList().forEach(word -> word.set(""));
        mBinding.getRoot().setVisibility(View.INVISIBLE);
    }


    private void showModal() {
        CreateVaultModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.create_vault_modal, null, false);
        dialog = ModalDialog.newInstance();
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
        String[] steps = mActivity.getResources().getStringArray(R.array.create_vault_step);
        binding.step.setText(steps[0]);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 4000);
                    binding.step.setText(steps[i]);
                    i++;
                    if (i > 4) {
                        handler.removeCallbacks(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.post(runnable);
    }
}
