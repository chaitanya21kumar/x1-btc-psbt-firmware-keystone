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

package com.x1-btc-psbt-firmware.cold.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.coinlib.accounts.Account;
import com.x1-btc-psbt-firmware.cold.callables.VerifyMnemonicCallable;

public class SharedDataViewModel extends AndroidViewModel {

    private final MutableLiveData<String> scanResult = new MutableLiveData<>();

    private Account targetMultiSigAccount = Account.MULTI_P2WSH;

    public SharedDataViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getScanResult() {
        return scanResult;
    }

    public void updateScanResult(String s) {
        scanResult.setValue(s);
    }

    public void setTargetMultiSigAccount(Account account) {
        this.targetMultiSigAccount = account;
    }

    public Account getTargetMultiSigAccount() {
        return this.targetMultiSigAccount;
    }

    public boolean verifyMnemonic(String mnemonic) {
        return new VerifyMnemonicCallable(mnemonic, null, 0).call();
    }

    private final MutableLiveData<Object> scanResultObj = new MutableLiveData<>();

    public void setScanResultObj(Object o) {
        scanResultObj.setValue(o);
    }

    public MutableLiveData<Object> getScanResultObj(){
        return scanResultObj;
    }
}
