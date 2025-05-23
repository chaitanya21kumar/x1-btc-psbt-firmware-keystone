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

package com.x1-btc-psbt-firmware.coinlib.coins.BTC;

import androidx.annotation.NonNull;

import com.x1-btc-psbt-firmware.coinlib.coins.AbsCoin;
import com.x1-btc-psbt-firmware.coinlib.coins.AbsTx;
import com.x1-btc-psbt-firmware.coinlib.coins.SignPsbtResult;
import com.x1-btc-psbt-firmware.coinlib.coins.SignTxResult;
import com.x1-btc-psbt-firmware.coinlib.exception.InvalidTransactionException;
import com.x1-btc-psbt-firmware.coinlib.interfaces.Coin;
import com.x1-btc-psbt-firmware.coinlib.interfaces.SignCallback;
import com.x1-btc-psbt-firmware.coinlib.interfaces.SignPsbtCallback;
import com.x1-btc-psbt-firmware.coinlib.interfaces.Signer;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class Btc extends AbsCoin {
    public Btc(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.BTC.coinCode();
    }

    public void generateOmniTx(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        SignTxResult result = ((BtcImpl) impl).generateOmniTx(tx, signers);
        if (result != null && result.isValid()) {
            callback.onSuccess(result.txId, result.txHex);
        } else {
            callback.onFail();
        }
    }

    public JSONObject parsePsbt(@NonNull String psbt) {
        return ((BtcImpl)impl).parsePsbt(psbt);
    }

    public void signPsbt(@NonNull String psbt, SignPsbtCallback callback, boolean finalize, Signer... signers) {
        if (signers == null) {
            callback.onFail();
            return;
        }
        SignPsbtResult result = ((BtcImpl)impl).signPsbt(psbt, finalize, signers);
        if (result != null && result.isValid()) {
            callback.onSuccess(result.txId, result.psbtB64);
        } else {
            callback.onFail();
        }
    }

    public void signPsbt(@NonNull String psbt, SignPsbtCallback callback, Signer... signers) {
        signPsbt(psbt,callback,true, signers);
    }

    public static class Tx extends AbsTx implements UtxoTx {

        protected long inputAmount;
        private long outputAmount;
        private static final int DUST_AMOUNT = 546;
        private static final int OMNI_USDT_PROPERTYID = 31;
        private List<ChangeAddressInfo> changeAddressInfo;

        public Tx(JSONObject signTxObject, String coinCode) throws JSONException, InvalidTransactionException {
            super(signTxObject, coinCode);
        }

        @Override
        protected void checkHdPath() {
        }

        @Override
        public List<ChangeAddressInfo> getChangeAddressInfo() {
            return changeAddressInfo;
        }

        @Override
        public JSONArray getInputs() {
            try {
                return metaData.getJSONArray("inputs");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public JSONArray getOutputs() {
            try {
                return metaData.getJSONArray("outputs");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected JSONObject extractMetaData(JSONObject signTxObject, String coinCode)
                throws JSONException {
            if (signTxObject.has("btcTx")) {
                return signTxObject.getJSONObject("btcTx");
            } else if (signTxObject.has("omniTx")) {
                return signTxObject.getJSONObject("omniTx");
            }
            return super.extractMetaData(signTxObject, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException, InvalidTransactionException {
            if (metaData.optLong("omniAmount") != 0) {
                isToken = true;
                parseInput();
                txType = "OMNI";
                int propertyId = metaData.optInt("propertyId", OMNI_USDT_PROPERTYID);
                if (OMNI_USDT_PROPERTYID == propertyId) {
                    txType = "OMNI_USDT";
                    tokenName = "USDT";
                }
                fee = satoshiToBtc(metaData.getLong("fee"));
                if (inputAmount < fee + DUST_AMOUNT) {
                    throw new InvalidTransactionException("invalid omni tx");
                }
                amount = metaData.optLong("omniAmount") / Math.pow(10, decimal);
                to = metaData.getString("to");
            } else {
                parseInput();
                parseOutPut();
                amount = calculateDisplayAmount();
                memo = metaData.optString("memo");
                fee = calculateDisplayFee();
            }
        }

        @Override
        public double getAmount() {
            if (isToken) {
                return getAmountWithoutFee();
            } else {
                return super.getAmount();
            }
        }

        private void parseOutPut() throws JSONException {
            JSONArray outputs = metaData.getJSONArray("outputs");
            JSONArray outputsClone = new JSONArray(outputs.toString());
            StringBuilder destinations = new StringBuilder();
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                outputAmount += output.getLong("value");
                if (output.optBoolean("isChange") && output.has("changeAddressPath")) {

                    if (changeAddressInfo == null) {
                        changeAddressInfo = new ArrayList<>();
                    }
                    changeAddressInfo.add(new ChangeAddressInfo(
                            output.getString("address"),
                            output.getString("changeAddressPath"),
                            output.getLong("value")));
                }

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                String amount = nf.format(satoshiToBtc(output.getLong("value")));
                outputsClone.getJSONObject(i).put("value", amount + " " + coinCode);
                destinations.append(output.get("address")).append(SEPARATOR);
            }
            if (outputsClone.length() == 1) {
                to = destinations.deleteCharAt(destinations.length() - 1).toString();
            } else {
                to = outputsClone.toString();
            }
        }

        protected void parseInput() throws JSONException, InvalidTransactionException {
            isMultisig = metaData.optBoolean("multisig");
            JSONArray inputs = metaData.getJSONArray("inputs");
            StringBuilder paths = new StringBuilder();
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject input = inputs.getJSONObject(i);
                String path = input.getString("ownerKeyPath");
                input.put("bip32Derivation",new JSONArray());
                if (!isMultisig) {
                    checkHdPath(path, false);
                } else {
                }
                paths.append(path).append(SEPARATOR);
                int index = input.optInt("index");
                if (index == 0) {
                    input.put("index", 0);
                }
                inputAmount += input.getJSONObject("utxo").getLong("value");
            }
            hdPath = paths.deleteCharAt(paths.length() - 1).toString();
        }

        private double calculateDisplayFee() throws InvalidTransactionException {
            if (inputAmount <= outputAmount) {
                throw new InvalidTransactionException("invalid btc tx: inputAmount must greater than outputAmount");
            }
            return satoshiToBtc(inputAmount - outputAmount);
        }

        private double calculateDisplayAmount() {
            long changeAmount = 0;
            if (changeAddressInfo != null) {
                for (ChangeAddressInfo info : changeAddressInfo) {
                    changeAmount += info.value;
                }
            }
            return satoshiToBtc(outputAmount - changeAmount);
        }

        private double satoshiToBtc(long sat) {
            return sat / Math.pow(10, decimal);
        }
    }
    public enum AddressType {
        P2PKH,
        P2SH_P2WPKH,
        P2WPKH
    }

}
