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

package com.x1-btc-psbt-firmware.coinlib.v8;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.x1-btc-psbt-firmware.coinlib.coins.AbsTx;
import com.x1-btc-psbt-firmware.coinlib.coins.SignTxResult;
import com.x1-btc-psbt-firmware.coinlib.interfaces.Coin;
import com.x1-btc-psbt-firmware.coinlib.interfaces.SignCallback;
import com.x1-btc-psbt-firmware.coinlib.interfaces.Signer;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;

import org.bitcoinj.core.Sha256Hash;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

public class CoinImpl implements Coin {

    private static final String FUNCTION_SIGN = "sign";

    private final String coinCode;
    protected final V8 v8;
    protected final V8Object coin;
    private V8Function signTxFunction;
    private V8Function signMessageFunction;
    private V8Function generateAddressFunction;

    public CoinImpl(String coinCode) {
        super();
        this.coinCode = coinCode;
        this.v8 = ScriptLoader.sInstance.loadByCoinCode("BTC");
        if (coinCode.equals(Coins.XTN.coinCode())) {
            this.coin = v8.executeObjectScript("new BTC(\"testNet\")");
        } else {
            this.coin = v8.executeObjectScript("new BTC()");
        }
        v8.registerResource(coin);
    }

    /**
     * sign a tx
     *
     * @param txData  the txData
     * @param signers signers,utxo coins transaction may need multi for 1 transaction
     * @return SignTxResult
     */
    protected SignTxResult signTxImpl(V8Object txData, String signFunc, Signer... signers) {
        if (this.signTxFunction == null) {
            this.signTxFunction = (V8Function) coin.get(signFunc);
        }

        v8.registerResource(txData);
        V8Array params = new V8Array(v8);
        v8.registerResource(params);
        params.push(txData);

        if (signers.length > 1) {
            V8Array signProviders = new V8Array(v8);
            v8.registerResource(signProviders);
            Stream.of(signers).forEach(signer -> signProviders.push(createSignerProvider(signer)));
            params.push(signProviders);
        } else if (signers.length == 1) {
            if (Coins.supportMultiSigner(coinCode)) {
                V8Array signProviders = new V8Array(v8);
                signProviders.push(createSignerProvider(signers[0]));
                params.push(signProviders);
            } else {
                V8Object signProvider = createSignerProvider(signers[0]);
                params.push(signProvider);
            }

        } else {
            return null;
        }
        params.push(false);
        SignTxResult result;
        try {
            V8Object res = (V8Object) signTxFunction.call(coin, params);
            result = new SignTxResult(res.getString("txId"), res.getString("txHex"));
            v8.registerResource(res);
        } catch (V8ScriptExecutionException e) {
            e.printStackTrace();
            return null;
        } finally {
            v8.release(false);
        }
        return result;
    }

    /**
     * sign message with signer
     *
     * @param msgHex    message hex to be sign
     * @param signer signer
     * @return signed message in format R + S + recId (if has)
     */
    private String signMessageImpl(String msgHex, Signer signer) {
        String signature = signer.sign(Hex.toHexString(Objects.requireNonNull(constructMessageHash(msgHex))));
        if (TextUtils.isEmpty(signature)) return null;
        byte[] r = Hex.decode(signature.substring(0, 64));
        byte[] s = Hex.decode(signature.substring(64, 128));
        int recId = Integer.parseInt(signature.substring(128), 16);
        //we use compressed public key.
        return Hex.toHexString(new byte[]{(byte) (recId + 31)}) + Hex.toHexString(r) + Hex.toHexString(s);
    }

    private byte[] constructMessageHash(String messageHex) {
        try {
            byte[] messageBytes = Hex.decode(messageHex);
            byte[] prefix = "\u0018Bitcoin Signed Message:\n".getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(prefix);
            baos.write(messageBytes.length);
            baos.write(messageBytes);
            byte[] buffer = baos.toByteArray();
            return Sha256Hash.hashTwice(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected V8Object createSignerProvider(Signer signer) {
        V8Object signProvider = new V8Object(v8);
        v8.registerResource(signProvider);
        V8Function sign = new V8Function(v8, new SignProviderCallback(signer));
        v8.registerResource(sign);
        signProvider.add(FUNCTION_SIGN, sign);
        if (signer.getPublicKey() != null) {
            signProvider.add("publicKey", signer.getPublicKey());
        }
        return signProvider;
    }

    /**
     * generate an address from public key
     *
     * @param publicKey used to generate address
     * @return address
     */
    private String generateAddressImpl(String publicKey) {
        if (this.generateAddressFunction == null) {
            this.generateAddressFunction = (V8Function) coin.get("generateAddress");
        }

        V8Array params = new V8Array(v8);
        params.push(publicKey);
        addOption(params);
        return (String) generateAddressFunction.call(coin, params);
    }

    private void addOption(V8Array params) {
        params.push("P2WSH_P2SH");
    }

    class SignProviderCallback implements JavaCallback {

        private final Signer signer;

        private SignProviderCallback(Signer signer) {
            this.signer = signer;
        }

        @Override
        public V8Object invoke(V8Object v8Object, V8Array v8Array) {
            String data = v8Array.getString(0);

            String signedStr = signer.sign(data);

            V8Object res = new V8Object(v8);
            if (TextUtils.isEmpty(signedStr)) {
                return res;
            }
            res.add("r", signedStr.substring(0, 64));
            res.add("s", signedStr.substring(64, 128));

            int recId = 0;
            try {
                recId = Integer.parseInt(signedStr.substring(128), 16);
            } catch (Exception ignore) {
            }
            res.add("recId", recId);
            return res;
        }
    }

    /**
     * convert JSONObject to a V8Object
     *
     * @param object metaData
     * @return V8Object
     */
    protected V8Object constructTxData(JSONObject object) {
        V8Array parameters = new V8Array(v8).push(object.toString());
        V8Object json = v8.getObject("JSON");
        v8.registerResource(parameters);
        v8.registerResource(json);
        return json.executeObjectFunction("parse", parameters);
    }

    @Override
    public String coinCode() {
        return coinCode;
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        V8Object txData = constructTxData(tx.getMetaData());
        SignTxResult res = signTxImpl(txData, "generateTransactionSync", signers);
        if (res != null && res.isValid()) {
            callback.onSuccess(res.txId, res.txHex);
        } else {
            callback.onFail();
        }
    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        return signMessageImpl(message, signer);
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return generateAddressImpl(publicKey);
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        return true;
    }

}

