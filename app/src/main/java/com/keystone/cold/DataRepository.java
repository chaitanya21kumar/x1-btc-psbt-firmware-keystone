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

package com.x1-btc-psbt-firmware.cold;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.callables.GetMasterFingerprintCallable;
import com.x1-btc-psbt-firmware.cold.db.AppDatabase;
import com.x1-btc-psbt-firmware.cold.db.entity.AccountEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.AddressEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.CasaSignature;
import com.x1-btc-psbt-firmware.cold.db.entity.CoinEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigAddressEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.WhiteListEntity;
import com.x1-btc-psbt-firmware.cold.model.Coin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataRepository {
    @SuppressLint("StaticFieldLeak")
    private static DataRepository sInstance;

    private final AppDatabase mDb;
    private final MediatorLiveData<List<CoinEntity>> mObservableCoins;
    private final Context context;

    private DataRepository(Context context, final AppDatabase database) {
        mDb = database;
        this.context = context;
        mObservableCoins = new MediatorLiveData<>();
        mObservableCoins.addSource(mDb.coinDao().loadAllCoins(), coins -> {
            if (mDb.getDatabaseCreated().getValue() != null) {
                mObservableCoins.postValue(filterByBelongTo(coins));
            }
        });
    }

    public String getBelongTo() {
        return Utilities.getCurrentBelongTo(context);
    }

    public static DataRepository getInstance(Context context, final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(context, database);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<CoinEntity>> loadCoins() {
        return mObservableCoins;
    }

    public LiveData<List<CoinEntity>> reloadCoins() {
        MediatorLiveData<List<CoinEntity>> coins = new MediatorLiveData<>();
        coins.addSource(mDb.coinDao().loadAllCoins(), coinEntities -> {
            if (mDb.getDatabaseCreated().getValue() != null) {
                coins.postValue(filterByBelongTo(coinEntities));
            }
        });
        return coins;
    }

    public List<CoinEntity> loadCoinsSync() {
        return filterByBelongTo(mDb.coinDao().loadAllCoinsSync());
    }

    public void updateCoin(Coin coin) {
        mDb.coinDao().update(new CoinEntity(coin));
    }

    public void insertAddress(List<AddressEntity> addrs) {
        mDb.addressDao().insertAll(addrs);
    }

    public void insertAddress(AddressEntity addrs) {
        mDb.addressDao().insertAddress(addrs);
    }

    public LiveData<CoinEntity> loadCoin(final long id) {
        return mDb.coinDao().loadCoin(id);
    }

    public CoinEntity loadCoinSync(final String coinId) {
        return mDb.coinDao().loadCoinSync(coinId, getBelongTo());
    }

    public LiveData<CoinEntity> loadCoin(final String coinId) {
        return mDb.coinDao().loadCoin(coinId, getBelongTo());
    }

    public LiveData<List<AddressEntity>> loadAddress(String coinId) {
        return mDb.addressDao().loadAddressForCoin(coinId, getBelongTo());
    }

    public LiveData<List<AddressEntity>> loadAllAddress() {
        return mDb.addressDao().loadAllAddress(getBelongTo());
    }

    public List<AddressEntity> loadAddressSync(String coinId) {
        return mDb.addressDao().loadAddressSync(coinId, getBelongTo());
    }

    public AddressEntity loadAddressBypath(String path) {
        return mDb.addressDao().loadAddress(path.toUpperCase(), getBelongTo());
    }

    public void updateAddress(AddressEntity addressEntity) {
        mDb.addressDao().update(addressEntity);
    }

    public LiveData<List<TxEntity>> loadTxs(String coinId) {
        return mDb.txDao().loadTxs(coinId);
    }

    public List<TxEntity> loadAllTxsSync(String coinId) {
        return mDb.txDao().loadTxsSync(coinId);
    }

    public List<TxEntity> loadAllTxSync(String coinId) {
        return mDb.txDao().loadTxsSync(coinId);
    }

    public LiveData<TxEntity> loadTx(String txId) {
        return mDb.txDao().load(txId);
    }

    public LiveData<List<CasaSignature>> loadCasaSignatures() {
        return mDb.casaDao().loadSignatures();
    }

    public List<CasaSignature> loadCasaSignaturesSync() {
        return mDb.casaDao().loadTxsSync();
    }

    public LiveData<CasaSignature> loadCasaSignature(String id) {
        return mDb.casaDao().load(Long.parseLong(id));
    }

    public LiveData<List<CasaSignature>> loadCasaTxs(String belongTo) {
        return mDb.casaDao().loadCasaTxs(belongTo);
    }

    public TxEntity loadTxSync(String txId) {
        return mDb.txDao().loadSync(txId);
    }

    public void insertTx(TxEntity tx) {
        mDb.txDao().insert(tx);
    }

    public Long insertCasaSignature(CasaSignature casaSignature) {
        return mDb.casaDao().removeAndInsert(casaSignature);
    }

    public void insertCoins(List<CoinEntity> coins) {
        mDb.runInTransaction(() -> mDb.coinDao().insertAll(coins));
    }

    public long insertCoin(CoinEntity coin) {
        return mDb.coinDao().insert(coin);
    }

    public void clearDb() {
        mDb.clearAllTables();
    }

    private List<CoinEntity> filterByBelongTo(List<CoinEntity> coins) {
        String belongTo = Utilities.getCurrentBelongTo(context);
        return coins.isEmpty() ? Collections.emptyList()
                :
                coins.stream()
                        .filter(coin -> belongTo.equals(coin.getBelongTo()))
                        .collect(Collectors.toList());
    }


    public LiveData<List<WhiteListEntity>> loadWhiteList() {
        return mDb.whiteListDao().load();
    }

    public void insertWhiteList(WhiteListEntity entity) {
        mDb.whiteListDao().insert(entity);
    }

    public void deleteWhiteList(WhiteListEntity entity) {
        mDb.whiteListDao().delete(entity);
    }

    public WhiteListEntity queryWhiteList(String address) {
        return mDb.whiteListDao().queryAddress(address, Utilities.getCurrentBelongTo(context));
    }

    public void insertAccount(AccountEntity account) {
        mDb.accountDao().add(account);
    }

    public void updateAccount(AccountEntity account) {
        mDb.accountDao().update(account);
    }

    public List<AccountEntity> loadAccountsForCoin(CoinEntity coin) {
        return mDb.accountDao().loadForCoin(coin.getId());
    }

    public AccountEntity loadAccountsByXpub(long id, String xpub) {
        return mDb.accountDao().loadAccountByXpub(id, xpub);
    }

    public AccountEntity loadAccountsByPath(long id, String path) {
        return mDb.accountDao().loadAccountByPath(id, path);
    }

    public CoinEntity loadCoinEntityByCoinCode(String coinCode) {
        String coinId = Coins.coinIdFromCoinCode(coinCode);
        return loadCoinSync(coinId);
    }

    public void deleteHiddenVaultData() {
        mDb.coinDao().deleteHidden();
        mDb.txDao().deleteHidden();
        mDb.addressDao().deleteHidden();
        mDb.whiteListDao().deleteHidden();
        mDb.casaDao().deleteHidden();
    }

    public long addMultisigWallet(MultiSigWalletEntity entity) {
        return mDb.multiSigWalletDao().add(entity);
    }

    public LiveData<List<MultiSigWalletEntity>> loadAllMultiSigWallet(String mode) {
        String xfp = new GetMasterFingerprintCallable().call();
        return mDb.multiSigWalletDao().loadAll(xfp, mode);
    }

    public List<MultiSigWalletEntity> loadAllMultiSigWalletSync(String mode) {
        String netmode = Utilities.isMainNet(context) ? "main" : "testnet";
        String xfp = new GetMasterFingerprintCallable().call();
        return mDb.multiSigWalletDao().loadAllSync(xfp, mode)
                .stream().filter(w -> w.getNetwork().equals(netmode))
                .collect(Collectors.toList());
    }

    public LiveData<List<MultiSigAddressEntity>> loadAllMultiSigAddress() {
        return mDb.multiSigAddressDao().loadAll();
    }

    public MultiSigAddressEntity loadAllMultiSigAddress(String walletfp, String path) {
        return mDb.multiSigAddressDao().loadAddressByPath(walletfp, path);
    }

    public List<MultiSigAddressEntity> loadAllMultiSigAddressSync(String xfp) {
        return mDb.multiSigAddressDao().loadAllMultiSigAddressSync(xfp);
    }

    public MultiSigWalletEntity loadMultisigWallet(String fingerprint) {
        return mDb.multiSigWalletDao().loadWallet(fingerprint);
    }

    public List<MultiSigAddressEntity> loadAddressForWalletSync(String walletId) {
        return mDb.multiSigAddressDao().loadAllMultiSigAddressSync(walletId);
    }

    public LiveData<List<MultiSigAddressEntity>> loadAddressForWallet(String walletId) {
        return mDb.multiSigAddressDao().loadAllMultiSigAddress(walletId);
    }


    public void insertMultisigAddress(List<MultiSigAddressEntity> entities) {
        mDb.multiSigAddressDao().insert(entities);
    }

    public LiveData<List<TxEntity>> loadMultisigTxs(String walletFingerprint) {
        return mDb.txDao().loadMultisigTxs(walletFingerprint);
    }

    public void updateWallet(MultiSigWalletEntity entity) {
        mDb.multiSigWalletDao().update(entity);
    }

    public void deleteMultisigWallet(String walletFingerPrint) {
        mDb.multiSigWalletDao().delete(walletFingerPrint);
    }

    public void deleteTxs(String walletFingerPrint) {
        mDb.txDao().deleteTxs(walletFingerPrint);
    }
}
