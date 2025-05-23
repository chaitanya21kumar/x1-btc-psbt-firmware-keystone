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

package com.x1-btc-psbt-firmware.cold.config;

public class FeatureFlags extends BaseFeatureFlags {
    private FeatureFlags() {
    }

    public static final boolean ENABLE_WHITE_LIST = false;
    public static final boolean ENABLE_SHARDING_MNEMONIC = false;
    public static final boolean ENABLE_SYSTEM_UPDATE = true;
    public static final boolean ENABLE_SERIAL_UPDATE = true;
}
