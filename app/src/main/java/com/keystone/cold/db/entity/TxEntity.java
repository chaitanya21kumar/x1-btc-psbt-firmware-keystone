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

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.x1-btc-psbt-firmware.cold.model.Tx;

import java.util.Objects;

@Entity(tableName = "txs", indices = {@Index("txId")})

public class TxEntity implements Tx, FilterableItem {

    @PrimaryKey
    @NonNull
    private String txId;
    private String coinId;
    private String coinCode;
    private String amount;
    private String from;
    private String to;
    private String fee;
    private String signedHex;
    private long timeStamp;
    private String memo;
    private String signId;
    private String belongTo;
    private String signStatus;

    private String addition;

    public void setAddition(String json) {
        this.addition = json;
    }

    public String getAddition() {
        return addition;
    }

    @Override
    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    @NonNull
    @Override
    public String getTxId() {
        return txId;
    }

    public void setTxId(@NonNull String txId) {
        this.txId = txId;
    }

    @Override
    public String getCoinId() {
        return coinId;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public String getCoinCode() {
        return coinCode;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    @Override
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    @Override
    public String getSignedHex() {
        return signedHex;
    }

    public void setSignedHex(String signedHex) {
        this.signedHex = signedHex;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Override
    public String getMemo() {
        return memo;
    }

    public void setSignId(String signId) {
        this.signId = signId;
    }

    @Override
    public String getSignId() {
        return signId;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @NonNull
    @Override
    public String toString() {
        return "TxEntity{" +
                "txId='" + txId + '\'' +
                ", coinId='" + coinId + '\'' +
                ", coinCode='" + coinCode + '\'' +
                ", amount='" + amount + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", fee='" + fee + '\'' +
                ", signedHex='" + signedHex + '\'' +
                ", timeStamp=" + timeStamp +
                ", memo='" + memo + '\'' +
                ", signId='" + signId + '\'' +
                ", belongTo='" + belongTo + '\'' +
                ", signStatus='" + signStatus + '\'' +
                '}';
    }

    @Override
    public boolean filter(String s) {
        if (TextUtils.isEmpty(s)) {
            return true;
        }
        s = s.toLowerCase();
        return from.toLowerCase().contains(s)
                || to.toLowerCase().contains(s)
                || txId.toLowerCase().contains(s)
                || memo.toLowerCase().contains(s);
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TxEntity txEntity = (TxEntity) o;
        return Objects.equals(from, txEntity.from) &&
                Objects.equals(to, txEntity.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
