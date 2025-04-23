package com.x1-btc-psbt-firmware.cold.viewmodel.multisigs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.x1-btc-psbt-firmware.coinlib.accounts.Account;
import com.x1-btc-psbt-firmware.coinlib.accounts.ExtendedPublicKeyVersion;
import com.x1-btc-psbt-firmware.cold.callables.GetExtendedPublicKeyCallable;
import com.x1-btc-psbt-firmware.cold.callables.GetMasterFingerprintCallable;

import java.util.HashMap;
import java.util.Map;

class ViewModelBase extends AndroidViewModel {
    private final Map<Account, String> xPubMap = new HashMap<>();
    private final String xfp;

    public ViewModelBase(@NonNull Application application) {
        super(application);
        xfp = new GetMasterFingerprintCallable().call();
    }

    public String getXfp() {
        return xfp;
    }

    public String getXPub(Account account) {
        if (!xPubMap.containsKey(account)) {
            String xPub = new GetExtendedPublicKeyCallable(account.getPath()).call();
            xPubMap.put(account, ExtendedPublicKeyVersion.convertXPubVersion(xPub, account.getXPubVersion()));
        }
        return xPubMap.get(account);
    }
}
