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

package com.x1-btc-psbt-firmware.cold.ui;

import static com.x1-btc-psbt-firmware.cold.Utilities.IS_SETUP_VAULT;
import static com.x1-btc-psbt-firmware.cold.Utilities.hasPasswordSet;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setup.SetPasswordFragment.handleRuntimeStateAbnormal;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.selfcheck.RuntimeStatusCode;
import com.x1-btc-psbt-firmware.cold.ui.common.FullScreenActivity;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;

import java.util.Objects;

public class SetupVaultActivity extends FullScreenActivity {

    public boolean inSetupProcess;
    private SetupVaultViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_setup_vault);
        model = ViewModelProviders.of(this).get(SetupVaultViewModel.class);
        setupNavController(savedInstanceState);

        if (getIntent() != null) {
            String password = getIntent().getStringExtra(PASSWORD);
            model.setPassword(password);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupNavController(Bundle savedInstanceState) {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavInflater inflater = Objects.requireNonNull(navHostFragment).getNavController().getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.nav_graph_setup);
        Intent intent = getIntent();

        Bundle bundle = new Bundle();

        if (savedInstanceState != null) {
            inSetupProcess = savedInstanceState.getBoolean(IS_SETUP_VAULT);
        } else {
            inSetupProcess = intent != null && intent.getBooleanExtra(IS_SETUP_VAULT, true);
        }
        if (!inSetupProcess) {
            graph.setStartDestination(R.id.setupVaultFragment);
        } else {
            bundle.putBoolean(IS_SETUP_VAULT, true);
            switch (model.getVaultCreateStep()) {
                case SetupVaultViewModel.VAULT_CREATE_STEP_WELCOME: {
                    graph.setStartDestination(R.id.welcomeFragment);
                    break;
                }
                case SetupVaultViewModel.VAULT_CREATE_STEP_WEB_AUTH: {
                    graph.setStartDestination(R.id.webAuthFragment);
                    break;
                }
                case SetupVaultViewModel.VAULT_CREATE_STEP_SET_PASSWORD: {
                    graph.setStartDestination(R.id.setPasswordFragment);
                    break;
                }
                case SetupVaultViewModel.VAULT_CREATE_STEP_FIRMWARE_UPGRADE: {
                    graph.setStartDestination(R.id.firmwareUpgradeFragment);
                    break;
                }
                case SetupVaultViewModel.VAULT_CREATE_STEP_WRITE_MNEMONIC: {
                    graph.setStartDestination(R.id.setupVaultFragment);
                    break;
                }
                case SetupVaultViewModel.VAULT_CREATE_STEP_DONE: {
                    startActivity(new Intent(this, MainActivity.class));
                    this.finish();
                    return;
                }
                default: {
                    handleRuntimeStateAbnormal(this, RuntimeStatusCode.RUNTIME_INVALID_VAULT_CREATE_STEP);
                }
            }
        }
        navHostFragment.getNavController().setGraph(graph, bundle);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_SETUP_VAULT, inSetupProcess);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (model.getVaultCreateStep() >= SetupVaultViewModel.VAULT_CREATE_STEP_SET_PASSWORD && !hasPasswordSet(this)) {
            NavHostFragment navHostFragment =
                    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            Bundle data = new Bundle();
            data.putBoolean(IS_SETUP_VAULT, true);
            navHostFragment.getNavController().navigate(R.id.setPasswordFragment, data);
        }
    }
}
