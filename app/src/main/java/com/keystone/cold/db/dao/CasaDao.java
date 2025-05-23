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
import androidx.room.Transaction;

import com.x1-btc-psbt-firmware.cold.db.entity.CasaSignature;

import java.util.List;

@Dao
public interface CasaDao {
    @Query("SELECT * FROM casa_signature ORDER BY id DESC")
    LiveData<List<CasaSignature>> loadSignatures();

    @Query("SELECT * FROM casa_signature ORDER BY id DESC")
    List<CasaSignature> loadTxsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(CasaSignature tx);

    @Query("DELETE FROM casa_signature WHERE txId = :txId")
    int deleteTx(String txId);

    @Query("SELECT * FROM casa_signature WHERE id = :id")
    LiveData<CasaSignature> load(long id);

    @Query("SELECT * FROM casa_signature WHERE txId = :txId")
    CasaSignature loadSync(String txId);

    @Query("SELECT * FROM casa_signature WHERE belongTo = :belongTo")
    LiveData<List<CasaSignature>> loadCasaTxs(String belongTo);

    @Query("DELETE FROM casa_signature WHERE belongTo = 'hidden'")
    int deleteHidden();

    /**
     * Remove duplicate entries in the datcasa_signature table,abase, and then insert new ones
     *
     * @param casaSignature casaSignature
     * @return id
     */
    @Transaction
    default Long removeAndInsert(CasaSignature casaSignature) {
        String txId = casaSignature.getTxId();
        CasaSignature loadSyncbTx = loadSync(txId);
        if (loadSyncbTx != null) {
            deleteTx(txId);
        }
        return insert(casaSignature);
    }
}
