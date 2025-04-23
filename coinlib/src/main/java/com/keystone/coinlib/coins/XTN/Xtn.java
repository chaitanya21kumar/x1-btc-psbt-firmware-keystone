/*
 *
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
 *
 */

package com.x1-btc-psbt-firmware.coinlib.coins.XTN;

import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Btc;
import com.x1-btc-psbt-firmware.coinlib.exception.InvalidTransactionException;
import com.x1-btc-psbt-firmware.coinlib.interfaces.Coin;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;

import org.json.JSONException;
import org.json.JSONObject;

public class Xtn extends Btc {
    public Xtn(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.XTN.coinCode();
    }

    public static class Tx extends Btc.Tx {

        public Tx(JSONObject signTxObject, String coinCode) throws JSONException,
                InvalidTransactionException {
            super(signTxObject, coinCode);
        }
    }
}
