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

package com.x1-btc-psbt-firmware.cold.ui.fragment.main;

import java.text.NumberFormat;

public class TransactionItem {
    final int id;
    final String amount;
    final String address;
    private final String coinCode;
    final String changePath;

    public TransactionItem(int id, long amount, String address, String coinCode) {
        this(id,amount,address,coinCode,null);
    }

    public TransactionItem(int id, long amount, String address, String coinCode, String changePath) {
        this.id = id;
        this.address = address;
        this.coinCode = coinCode;
        this.changePath = changePath;
        this.amount = formatSatoshi(amount);
    }

    private String formatSatoshi(long satoshi) {
        double value = satoshi / Math.pow(10, 8);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        return nf.format(value) + " BTC";
    }

    public int getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getAddress() {
        return address;
    }

    public enum ItemType {
        INPUT,
        OUTPUT,
        FROM,
        TO,
    }
}
