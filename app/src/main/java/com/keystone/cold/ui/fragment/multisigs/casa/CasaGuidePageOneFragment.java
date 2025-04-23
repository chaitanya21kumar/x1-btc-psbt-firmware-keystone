package com.x1-btc-psbt-firmware.cold.ui.fragment.multisigs.casa;

import android.os.Bundle;
import android.view.View;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.databinding.MultiCasaGuideOneBinding;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;

public class CasaGuidePageOneFragment extends BaseFragment<MultiCasaGuideOneBinding> {
    @Override
    protected int setView() {
        return R.layout.multi_casa_guide_one;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.btContinue.setOnClickListener(v -> navigate(R.id.action_to_casaGuidePageTwoFragment, getArguments()));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
