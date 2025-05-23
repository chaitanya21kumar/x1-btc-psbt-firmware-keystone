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

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.x1-btc-psbt-firmware.cold.db.entity.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(AccountEntity account);

    @Query("SELECT * FROM accounts WHERE coinId=:id")
    List<AccountEntity> loadForCoin(long id);

    @Query("SELECT * FROM accounts WHERE coinId =:id AND exPub=:expub")
    AccountEntity loadAccountByXpub(long id, String expub);

    @Query("SELECT * FROM accounts WHERE coinId =:id AND hdPath=:path")
    AccountEntity loadAccountByPath(long id, String path);

    @Update
    void update(AccountEntity account);
}
