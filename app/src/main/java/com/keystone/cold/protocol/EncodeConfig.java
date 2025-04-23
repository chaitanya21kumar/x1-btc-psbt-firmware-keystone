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

package com.x1-btc-psbt-firmware.cold.protocol;

public class EncodeConfig {
    public boolean compress = true;
    public Encoding encoding = Encoding.BASE64;
    public Format format = Format.PROTOBUF;

    public EncodeConfig() {

    }

    public EncodeConfig(boolean compress, Encoding encoding, Format format) {
        this.compress = compress;
        this.encoding = encoding;
        this.format = format;
    }

    public static final EncodeConfig DEFAULT
            = new EncodeConfig(true, Encoding.Hex, Format.PROTOBUF);

    public enum Encoding {
        BASE64,
        Hex,
    }

    public enum Format {
        JSON,
        PROTOBUF
    }
}
