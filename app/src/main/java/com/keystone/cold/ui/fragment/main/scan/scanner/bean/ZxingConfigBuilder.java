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

package com.x1-btc-psbt-firmware.cold.ui.fragment.main.scan.scanner.bean;

import com.x1-btc-psbt-firmware.cold.R;

public class ZxingConfigBuilder {
    private boolean isFullScreenScan = false;
    private int frameColor = R.color.colorAccent;
    private int frameLineColor = -1;

    public ZxingConfigBuilder setIsFullScreenScan(boolean isFullScreenScan) {
        this.isFullScreenScan = isFullScreenScan;
        return this;
    }

    public ZxingConfigBuilder setFrameColor(int frameColor) {
        this.frameColor = frameColor;
        return this;
    }

    public ZxingConfigBuilder setFrameLineColor(int frameLineColor) {
        this.frameLineColor = frameLineColor;
        return this;
    }

    public ZxingConfig createZxingConfig() {
        return new ZxingConfig(isFullScreenScan, frameColor, frameLineColor);
    }
}