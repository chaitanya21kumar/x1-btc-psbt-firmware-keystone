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

package com.x1-btc-psbt-firmware.cold.protocol.builder;

import com.x1-btc-psbt-firmware.cold.protobuf.PayloadProtoc;
import com.x1-btc-psbt-firmware.cold.protocol.EncodeConfig;

public class SignTxResultBuilder extends BaseBuilder {

    public SignTxResultBuilder() {
        this(EncodeConfig.DEFAULT);
    }

    public SignTxResultBuilder(EncodeConfig config) {
        super(PayloadProtoc.Payload.Type.TYPE_SIGN_TX_RESULT, config);
    }

    @Override
    public String build() {
        payload.setSignTxResult(signTxResult);
        return super.build();
    }

    public SignTxResultBuilder setSignId(String signId) {
        signTxResult.setSignId(signId);
        return this;
    }

    public SignTxResultBuilder setTxId(String txId) {
        signTxResult.setTxId(txId);
        return this;
    }

    public SignTxResultBuilder setRawTx(String rawTx) {
        signTxResult.setRawTx(rawTx);
        return this;
    }
}
