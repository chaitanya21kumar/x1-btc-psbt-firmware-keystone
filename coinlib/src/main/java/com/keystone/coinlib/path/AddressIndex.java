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

package com.x1-btc-psbt-firmware.coinlib.path;

import androidx.annotation.NonNull;

import java.util.Locale;

public final class AddressIndex {
    private final Change change;
    private final int addressIndex;

    private final String string;

    AddressIndex(final Change change, final int addressIndex) {
        this.change = change;
        this.addressIndex = addressIndex;
        string = String.format(Locale.US, "%s/%d", change, addressIndex);
    }

    public int getValue() {
        return addressIndex;
    }

    public Change getParent() {
        return change;
    }

    @NonNull
    @Override
    public String toString() {
        return string;
    }
}