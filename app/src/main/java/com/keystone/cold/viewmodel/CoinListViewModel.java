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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.coinlib.utils.Account;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.DataRepository;
import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.db.entity.AccountEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.CasaSignature;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.protocol.EncodeConfig;
import com.x1-btc-psbt-firmware.cold.protocol.builder.SyncBuilder;

import java.util.List;

public class CoinListViewModel extends AndroidViewModel {

    private final DataRepository mRepository;

    public CoinListViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }

    public LiveData<List<CoinEntity>> getCoins() {
        return mRepository.loadCoins();
    }

    public LiveData<TxEntity> loadTx(String txId) {
        return mRepository.loadTx(txId);
    }

    public LiveData<CasaSignature> loadCasaSignature(String id) {
        return mRepository.loadCasaSignature(id);
    }

    public LiveData<List<TxEntity>> loadTxs(String coinId) {
        return mRepository.loadTxs(coinId);
    }

    private List<AccountEntity> loadAccountForCoin(CoinEntity coin) {
        return mRepository.loadAccountsForCoin(coin);
    }

    public LiveData<String> generateSync(List<CoinEntity> coinEntities) {
        MutableLiveData<String> sync = new MutableLiveData<>();
        sync.setValue("");
        AppExecutors.getInstance().diskIO().execute(() -> {
            SyncBuilder syncBuilder = new SyncBuilder(EncodeConfig.DEFAULT);
            for (CoinEntity entity : coinEntities) {
                SyncBuilder.Coin coin = new SyncBuilder.Coin();
                coin.setActive(entity.isShow());
                coin.setCoinCode(entity.getCoinCode());
                List<AccountEntity> accounts = loadAccountForCoin(entity);
                for (AccountEntity accountEntity : accounts) {
                    //only sync account M/49'/0'/0' to X1-BTC-PSBT-Firmware mobile
                    if (accountEntity.getHdPath().equals(Account.P2SH_P2WPKH.getPath())) {
                        SyncBuilder.Account account = new SyncBuilder.Account();
                        account.addressLength = accountEntity.getAddressLength();
                        account.hdPath = accountEntity.getHdPath();
                        account.xPub = accountEntity.getExPub();
                        account.isMultiSign = false;
                        coin.addAccount(account);
                    }
                }
                if (coin.accounts.isEmpty()) continue;
                syncBuilder.addCoin(coin);
            }
            if (syncBuilder.getCoinsCount() == 0) {
                sync.postValue("");
            } else {
                sync.postValue(syncBuilder.build());
            }

        });
        return sync;
    }
}
