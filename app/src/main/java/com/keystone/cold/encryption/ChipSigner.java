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

package com.x1-btc-psbt-firmware.cold.encryption;

import androidx.annotation.Nullable;

import com.x1-btc-psbt-firmware.coinlib.interfaces.Signer;
import com.x1-btc-psbt-firmware.cold.callables.SignTxCallable;

import java.util.Objects;

public class ChipSigner extends Signer {

    private final String privKeyPath;
    private final String authToken;

    public ChipSigner(String path, String authToken) {
        this(path, authToken, null);
    }

    public ChipSigner(String path, String authToken, @Nullable String publicKey) {
        super(publicKey);
        this.privKeyPath = Objects.requireNonNull(path);
        this.authToken = authToken;
    }

    @Override
    public String sign(String data) {
        SignTxCallable callable = new SignTxCallable(privKeyPath, data, authToken);
        return callable.call();
    }
}
