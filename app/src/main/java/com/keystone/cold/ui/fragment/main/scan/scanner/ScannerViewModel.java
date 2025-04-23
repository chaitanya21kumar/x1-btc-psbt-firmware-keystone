package com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ScannerViewModel extends AndroidViewModel {
    private ScannerState state;

    public ScannerViewModel(@NonNull Application application) {
        super(application);
    }

    public void setState(ScannerState state) {
        this.state = state;
    }

    public ScannerState getState() {
        return state;
    }

    public void reset() {
        this.state = null;
    }
}
