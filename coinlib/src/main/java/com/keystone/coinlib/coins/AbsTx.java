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

package com.x1-btc-psbt-firmware.coinlib.coins;

import com.x1-btc-psbt-firmware.coinlib.exception.InvalidPathException;
import com.x1-btc-psbt-firmware.coinlib.exception.InvalidTransactionException;
import com.x1-btc-psbt-firmware.coinlib.path.Account;
import com.x1-btc-psbt-firmware.coinlib.path.AddressIndex;
import com.x1-btc-psbt-firmware.coinlib.path.Change;
import com.x1-btc-psbt-firmware.coinlib.path.CoinPath;
import com.x1-btc-psbt-firmware.coinlib.path.CoinType;
import com.x1-btc-psbt-firmware.coinlib.utils.Arith;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public abstract class AbsTx {

    protected String txId;
    protected String from;
    protected String to;
    protected double amount;
    protected double fee;
    protected String memo;
    protected int decimal;
    protected JSONObject metaData;
    protected final String coinCode;
    protected String hdPath;
    protected String txType;
    protected String tokenName;
    protected boolean isToken;
    protected boolean isMultisig;

    public static final String SEPARATOR = ",";

    public AbsTx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
        this.coinCode = coinCode;
        this.decimal = object.getInt("decimal");
        this.metaData = extractMetaData(object, coinCode);
        this.hdPath = object.optString("hdPath");
        checkHdPath();
        this.txType = coinCode;
        if (metaData == null) {
            throw new InvalidTransactionException("invalid sign tx metaData");
        }
        parseMetaData();
    }

    protected void checkHdPath() throws InvalidTransactionException {
        checkHdPath(hdPath, false);
    }

    protected void checkHdPath(String hdPath, boolean allHardend) throws InvalidTransactionException {
        try {
            AddressIndex address = CoinPath.parsePath(hdPath, allHardend);
            Change change = address.getParent();

            Account account = change.getParent();
            if (account.getValue() != 0) {
                throw new InvalidTransactionException("invalid hdPath, error account value");
            }

            CoinType coinType = account.getParent();
            if (!coinCode.equals(Coins.coinCodeOfIndex(coinType.getValue()))) {
                throw new InvalidTransactionException("invalid hdPath, error coinIndex");
            }
        } catch (InvalidPathException e) {
            e.printStackTrace();
            throw new InvalidTransactionException("invalid hdPath");
        }
    }

    public static AbsTx newInstance(JSONObject object) throws JSONException {

        String coinCode = object.getString("coinCode");
        try {
            Class<?> clazz = Class.forName(CoinReflect.getCoinClassByCoinCode(coinCode) + "$Tx");
            return (AbsTx) clazz.getDeclaredConstructor(JSONObject.class, String.class)
                    .newInstance(object, coinCode);
        } catch (ClassNotFoundException
                | InstantiationException
                | InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getTxType() {
        return txType;
    }

    public String getTxId() {
        return txId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return Arith.add(amount, fee);
    }

    protected double getAmountWithoutFee() {
        return amount;
    }

    public double getFee() {
        return fee;
    }

    public String getMemo() {
        return memo;
    }

    public JSONObject getMetaData() {
        return metaData;
    }

    protected abstract void parseMetaData() throws JSONException, InvalidTransactionException;

    protected JSONObject extractMetaData(JSONObject signTxObject, String coinCode) throws JSONException {
        return signTxObject.getJSONObject(coinCode.toLowerCase() + "Tx");
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public String getCoinCode() {
        return coinCode;
    }

    public String getHdPath() {
        return hdPath;
    }

    public String getUnit() {
        if (isToken) {
            return tokenName;
        } else {
            return coinCode;
        }
    }

    public boolean isMultisig() {
        return isMultisig;
    }
}