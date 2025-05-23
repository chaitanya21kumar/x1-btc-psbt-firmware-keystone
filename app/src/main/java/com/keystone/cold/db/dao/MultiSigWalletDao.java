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

package com.x1-btc-psbt-firmware.cold.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;

import java.util.List;

@Dao
public interface MultiSigWalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(MultiSigWalletEntity wallet);

    @Query("SELECT * FROM multi_sig_wallet WHERE belongTo=:xfp AND mode=:mode")
    LiveData<List<MultiSigWalletEntity>> loadAll(String xfp, String mode);

    @Query("SELECT * FROM multi_sig_wallet WHERE belongTo=:xfp AND mode=:mode")
    List<MultiSigWalletEntity> loadAllSync(String xfp, String mode);

    @Update
    int update(MultiSigWalletEntity walletEntity);

    @Query("DELETE FROM multi_sig_wallet WHERE walletFingerPrint=:walletFingerPrint")
    int delete(String walletFingerPrint);

    @Query("SELECT * FROM multi_sig_wallet WHERE walletFingerPrint=:walletFingerPrint")
    MultiSigWalletEntity loadWallet(String walletFingerPrint);
}
