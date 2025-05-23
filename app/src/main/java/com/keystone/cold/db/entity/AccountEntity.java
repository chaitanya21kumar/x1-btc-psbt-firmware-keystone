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

package com.x1-btc-psbt-firmware.cold.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "accounts",
        foreignKeys = @ForeignKey(entity = CoinEntity.class,
                parentColumns = "id",
                childColumns = "coinId", onDelete = CASCADE),
        indices = {@Index(value = {"coinId"})})

public class AccountEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    private String hdPath;
    private String exPub;
    private int addressLength;
    private boolean isMultiSign;
    private long coinId;
    private String addition;

    public void setAddition(String json) {
        this.addition = json;
    }

    public String getAddition() {
        return addition;
    }

    public AccountEntity(String hdPath, String exPub, int addressLength, boolean isMultiSign, long coinId) {
        this.hdPath = hdPath;
        this.exPub = exPub;
        this.addressLength = addressLength;
        this.isMultiSign = isMultiSign;
        this.coinId = coinId;
    }

    @Ignore
    public AccountEntity() {

    }

    @NonNull
    public String getHdPath() {
        return hdPath;
    }

    public void setHdPath(@NonNull String hdPath) {
        this.hdPath = hdPath;
    }

    public String getExPub() {
        return exPub;
    }

    public void setExPub(String exPub) {
        this.exPub = exPub;
    }

    public int getAddressLength() {
        return addressLength;
    }

    public void setAddressLength(int addressLength) {
        this.addressLength = addressLength;
    }

    public boolean isMultiSign() {
        return isMultiSign;
    }

    public void setMultiSign(boolean multiSign) {
        isMultiSign = multiSign;
    }

    public Long getCoinId() {
        return coinId;
    }

    public void setCoinId(Long coinId) {
        this.coinId = coinId;
    }

    @Override
    public String toString() {
        return "AccountEntity{" +
                "id=" + id +
                ", hdPath='" + hdPath + '\'' +
                ", exPub='" + exPub + '\'' +
                ", addressLength=" + addressLength +
                ", isMultiSign=" + isMultiSign +
                ", coinId=" + coinId +
                '}';
    }
}
