package com.x1-btc-psbt-firmware.cold.ui.fragment.setup;

import android.os.Bundle;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.SetupWelcomeBinding;
import com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel;

public class WelcomeFragment extends SetupVaultBaseFragment<SetupWelcomeBinding> {
    @Override
    protected int setView() {
        return R.layout.setup_welcome;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(v -> {
            viewModel.setVaultCreateStep(SetupVaultViewModel.VAULT_CREATE_STEP_WEB_AUTH);
            navigate(R.id.action_to_webAuthFragment);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
