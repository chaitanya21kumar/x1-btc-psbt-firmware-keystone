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

package com.x1-btc-psbt-firmware.cold.encryptioncore.job;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.x1-btc-psbt-firmware.cold.encryptioncore.base.Packet;
import com.x1-btc-psbt-firmware.cold.encryptioncore.interfaces.Callback;
import com.x1-btc-psbt-firmware.cold.encryptioncore.interfaces.Packer;
import com.x1-btc-psbt-firmware.cold.encryptioncore.interfaces.SerialManagerProxy;
import com.x1-btc-psbt-firmware.cold.encryptioncore.interfaces.SerialPortProxy;
import com.x1-btc-psbt-firmware.cold.encryptioncore.utils.Preconditions;

import java.io.IOException;
import java.util.concurrent.Callable;

class Job implements Runnable {
    private final SerialManagerProxy mManager;
    private final Packet mPacket;
    private final Callback mCallback;
    private final Packer mPacker;

    Job(@NonNull SerialManagerProxy manager, @NonNull Packer packer,
        @NonNull Packet packet, @NonNull Callback callback) {
        mManager = manager;
        mPacker = packer;
        mPacket = packet;
        mCallback = callback;
    }

    @NonNull
    @VisibleForTesting
    static SerialPortProxy openPort(@NonNull SerialManagerProxy manager) throws IOException {
        final String[] portNumbers = manager.getSerialPorts();
        Preconditions.checkArgument(portNumbers != null && portNumbers.length > 0, "port not found");

        final SerialPortProxy port = manager.openSerialPort(portNumbers[0]);
        Preconditions.checkNotNull(port, String.format("can not open port %s", portNumbers[0]));

        return port;
    }

    @Override
    public void run() {
        mManager.acquireWakeLock();
        final int maxRetryTimes = mPacket.getRetryTimes();

        for (int i = 0; i <= maxRetryTimes; ++i) {
            Packet result = null;
            Exception error = null;

            try {
                result = portCommunicate();
            } catch (Exception e) {
                error = e;
            }

            if (result != null) {
                mCallback.onSuccess(result);
                mManager.releaseWakeLock();
                return;
            } else if (i == maxRetryTimes) {
                mCallback.onFail(error);
                mManager.releaseWakeLock();
                return;
            }
        }

        mManager.releaseWakeLock();
        throw new IllegalStateException("this line should not be executed");
    }

    @NonNull
    private Packet portCommunicate() throws Exception {
        SerialPortProxy port = null;

        try {
            port = openPort(mManager);
            final Callable<Packet> callable = new Workshop(port, mPacker, mPacket);
            return callable.call();
        } finally {
            if (port != null) {
                try {
                    port.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
