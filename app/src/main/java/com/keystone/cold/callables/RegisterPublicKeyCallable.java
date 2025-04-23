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

import com.x1-btc-psbt-firmware.cold.encryption.interfaces.CONSTANTS;
import com.x1-btc-psbt-firmware.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

import static com.x1-btc-psbt-firmware.cold.encryption.interfaces.BASECONSTANTS.TAGS.CURRENT_PASSWORD;
import static com.x1-btc-psbt-firmware.cold.encryption.interfaces.BASECONSTANTS.TAGS.PUBLIC_KEY;

public class RegisterPublicKeyCallable implements Callable<Boolean> {

    private final byte[] publicKey;
    private final String password;

    public RegisterPublicKeyCallable(byte[] publicKey, String password) {
        this.publicKey = publicKey;
        this.password = password;
    }

    @Override
    public Boolean call() {
        try {
            final Callable callable = new BlockingCallable(
                    new Packet.Builder(CONSTANTS.METHODS.REGISTER_PUBLIC_KEY)
                            .addBytesPayload(PUBLIC_KEY, publicKey)
                            .addHexPayload(CURRENT_PASSWORD, password)
                            .build()
            );
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
