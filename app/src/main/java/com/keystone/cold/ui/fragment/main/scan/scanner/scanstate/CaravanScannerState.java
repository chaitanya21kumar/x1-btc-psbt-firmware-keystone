package com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.scanstate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.ScanResultTypes;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.PsbtCaravanConfirmViewModel;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.PsbtLegacyConfirmViewModel;

import java.util.List;

public class CaravanScannerState extends LegacyScannerState{
    public CaravanScannerState(List<ScanResultTypes> desiredResults) {
        super(desiredResults);
    }

    @NonNull
    @Override
    protected PsbtLegacyConfirmViewModel initPsbtLegacyConfirmViewModell() {
        return ViewModelProviders.of(mActivity).get(PsbtCaravanConfirmViewModel.class);
    }

    @Override
    protected void navigateTo(Bundle bundle) {
        mFragment.navigate(R.id.action_to_psbtCaravanTxConfirmFragment, bundle);
    }
}
