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

package com.x1-btc-psbt-firmware.cold.callables;

import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.encryption.interfaces.CONSTANTS;
import com.x1-btc-psbt-firmware.cold.encryptioncore.base.Packet;
import com.x1-btc-psbt-firmware.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;

public class GetMasterFingerprintCallable implements Callable<String> {

    private final boolean isMainWallet;
    public GetMasterFingerprintCallable() {
        isMainWallet = Utilities.getCurrentBelongTo(MainApplication.getApplication()).equals("main");
    }
    @Override
    public String call() {
        try {
            final Callable<Packet> callable = new BlockingCallable(
                    new Packet.Builder(CONSTANTS.METHODS.GET_MASTER_FINGERPRINT)
                            .addBytePayload(CONSTANTS.TAGS.WALLET_FLAG, isMainWallet? 0 : 0x50).build());
            final Packet result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.MASTER_FINGERPRINT);

            if (payload != null) {
                return payload.toHex().toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
