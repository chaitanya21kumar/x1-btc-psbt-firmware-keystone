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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.x1-btc-psbt-firmware.coinlib.exception.InvalidPathException;
import com.x1-btc-psbt-firmware.coinlib.path.AddressIndex;
import com.x1-btc-psbt-firmware.coinlib.path.CoinPath;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.DataRepository;
import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.callables.GetExtendedPublicKeyCallable;
import com.x1-btc-psbt-firmware.cold.db.entity.AddressEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CoinViewModel extends AndroidViewModel {

    private final DataRepository mRepository;

    public CoinViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }

    public List<AddressEntity> filterChangeAddress(List<AddressEntity> addressEntities) {
        return addressEntities.stream()
                .filter(this::isChangeAddress)
                .collect(Collectors.toList());
    }

    public List<AddressEntity> filterReceiveAddress(List<AddressEntity> addressEntities) {
        return addressEntities.stream()
                .filter(addressEntity -> !isChangeAddress(addressEntity))
                .collect(Collectors.toList());
    }

    public List<AddressEntity> filterByAccountHdPath(List<AddressEntity> addressEntities, String hdPath) {
        return addressEntities.stream()
                .filter(addressEntity -> addressEntity.getPath().toUpperCase().startsWith(hdPath))
                .collect(Collectors.toList());
    }

    private boolean isChangeAddress(AddressEntity addressEntity) {
        String path = addressEntity.getPath();
        try {
            AddressIndex addressIndex = CoinPath.parsePath(path);
            return !addressIndex.getParent().isExternal();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LiveData<List<AddressEntity>> getAddress() {
        return mRepository.loadAllAddress();
    }

    public void updateAddress(AddressEntity addr) {
        AppExecutors.getInstance().diskIO().execute(() -> mRepository.updateAddress(addr));
    }

    public void initDefultAddress(boolean isChangeAddress, String accountHdPath) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String coinId = Utilities.currentCoin(getApplication()).coinId();
            List<AddressEntity> filteredEntity = filterByAccountHdPath(mRepository.loadAddressSync(coinId), accountHdPath);
            List<AddressEntity> addressEntities;
            if (isChangeAddress) {
                addressEntities = filterChangeAddress(filteredEntity);
            } else {
                addressEntities = filterReceiveAddress(filteredEntity);
            }
            if (addressEntities.isEmpty()) {
                CoinEntity coinEntity = mRepository.loadCoinSync(coinId);
                String xPub = new GetExtendedPublicKeyCallable(accountHdPath).call();
                new AddAddressViewModel.AddAddressTask(coinEntity, mRepository,
                        null, xPub, isChangeAddress ? 1 : 0)
                        .execute(1);
            }
        });
    }
}
