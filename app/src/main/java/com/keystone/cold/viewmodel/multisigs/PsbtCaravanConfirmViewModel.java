package com.x1-btc-psbt-firmware.cold.viewmodel.multisigs;

import android.app.Application;

import androidx.annotation.NonNull;

public class PsbtCaravanConfirmViewModel extends PsbtLegacyConfirmViewModel{
    public PsbtCaravanConfirmViewModel(@NonNull Application application) {
        super(application);
        mode = "caravan";
    }
}
