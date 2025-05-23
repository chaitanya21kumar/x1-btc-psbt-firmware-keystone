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

import org.json.JSONArray;

import java.util.List;

public interface UtxoTx {
    List<ChangeAddressInfo> getChangeAddressInfo();

    JSONArray getInputs();
    JSONArray getOutputs();

    class ChangeAddressInfo {
        public final String address;
        public final String hdPath;
        public final long value;

        public ChangeAddressInfo(String address, String hdPath, long value) {
            this.address = address;
            this.hdPath = hdPath;
            this.value = value;
        }
    }
}
