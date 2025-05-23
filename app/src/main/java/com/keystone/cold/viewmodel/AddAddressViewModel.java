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
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Btc;
import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Deriver;
import com.x1-btc-psbt-firmware.coinlib.exception.InvalidPathException;
import com.x1-btc-psbt-firmware.coinlib.path.AddressIndex;
import com.x1-btc-psbt-firmware.coinlib.path.CoinPath;
import com.x1-btc-psbt-firmware.coinlib.utils.Account;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.DataRepository;
import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.db.entity.AccountEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.AddressEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddAddressViewModel extends AndroidViewModel {

    private final DataRepository mRepo;
    public CoinEntity coin;

    private final MutableLiveData<Boolean> addComplete = new MutableLiveData<>();

    public AddAddressViewModel(@NonNull Application application) {
        super(application);
        mRepo = ((MainApplication)application).getRepository();
    }

    public void addAddress(int count, Btc.AddressType type, int changeIndex) {
        String hdPath;
        boolean isMainNet = Utilities.isMainNet(getApplication());
        switch (type) {
            case P2SH_P2WPKH:
                hdPath = isMainNet ? Account.P2SH_P2WPKH.getPath() : Account.P2SH_P2WPKH_TESTNET.getPath();
                break;
            case P2WPKH:
                hdPath = isMainNet ? Account.P2WPKH.getPath() : Account.P2WPKH_TESTNET.getPath();
                break;
           default:
                hdPath = isMainNet ? Account.P2PKH.getPath() : Account.P2PKH_TESTNET.getPath();
                break;
        }
        List<AccountEntity> accounts = mRepo.loadAccountsForCoin(coin);
        accounts.stream()
                .filter(account -> account.getHdPath().toUpperCase().equals(hdPath))
                .findFirst()
                .ifPresent(accountEntity -> new AddAddressTask(coin, mRepo,
                        () -> addComplete.setValue(Boolean.TRUE), accountEntity.getExPub(),
                        changeIndex).execute(count));

    }

    public CoinEntity getCoin(String coinId) {
        coin = mRepo.loadCoinSync(coinId);
        return coin;
    }

    public LiveData<Boolean> getObservableAddState() {
        return addComplete;
    }

    public static class AddAddressTask extends AsyncTask<Integer, Void, Void> {
        private final CoinEntity coinEntity;
        private final DataRepository repo;
        private final Runnable onComplete;
        private final String xpub;
        private final int changeIndex;

        public AddAddressTask(CoinEntity coinEntity,
                              DataRepository repo,
                              Runnable onComplete,
                              @NonNull String xpub,
                              int changeIndex) {
            this.coinEntity = coinEntity;
            this.repo = repo;
            this.onComplete = onComplete;
            this.changeIndex = changeIndex;
            this.xpub = xpub;
        }

        @Override
        protected Void doInBackground(Integer... count) {
            AccountEntity accountEntity = repo.loadAccountsByXpub(coinEntity.getId(), xpub);
            List<AddressEntity> address = repo.loadAddressSync(coinEntity.getCoinId());

            Optional<AddressEntity> optional = address.stream()
                    .filter(addressEntity -> addressEntity.getPath()
                    .startsWith(accountEntity.getHdPath()+"/" + changeIndex))
                    .max((o1, o2) -> o1.getIndex() - o2.getIndex());
            int index = -1;
            if (optional.isPresent()) {
                try {
                    AddressIndex addressIndex = CoinPath.parsePath(optional.get().getPath());
                    index = addressIndex.getValue();
                } catch (InvalidPathException e) {
                    e.printStackTrace();
                }
            }

            int addressCount = index + 1;
            Btc.AddressType addressType = getAddressType(accountEntity);
            List<AddressEntity> entities = new ArrayList<>();
            boolean isMainNet = Coins.BTC.coinCode().equals(coinEntity.getCoinCode());
            Deriver deriver = new Deriver(isMainNet);
            for (int i = 0; i < count[0]; i++) {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setPath(accountEntity.getHdPath()+"/" + changeIndex+"/" + (addressCount + i));
                String addr = deriver.derive(xpub, changeIndex, i + addressCount, addressType);
                addressEntity.setAddressString(addr);
                addressEntity.setCoinId(coinEntity.getCoinId());
                addressEntity.setIndex(i + addressCount);
                addressEntity.setName("BTC-" + (i + addressCount));
                addressEntity.setBelongTo(coinEntity.getBelongTo());
                entities.add(addressEntity);
            }

            coinEntity.setAddressCount(coinEntity.getAddressCount() + count[0]);
            accountEntity.setAddressLength(addressCount + count[0]);
            repo.updateAccount(accountEntity);
            repo.updateCoin(coinEntity);
            repo.insertAddress(entities);
            return null;
        }

        public static Btc.AddressType getAddressType(AccountEntity accountEntity) {
            String hdPath = accountEntity.getHdPath();
            if (Account.P2SH_P2WPKH.getPath().equals(hdPath)
                    || Account.P2SH_P2WPKH_TESTNET.getPath().equals(hdPath)) {
                return Btc.AddressType.P2SH_P2WPKH;
            } else if(Account.P2WPKH.getPath().equals(hdPath)
                    || Account.P2WPKH_TESTNET.getPath().equals(hdPath) ) {
                return Btc.AddressType.P2WPKH;
            } else {
                return Btc.AddressType.P2PKH;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (onComplete != null) {
                onComplete.run();
            }
        }

    }
}
