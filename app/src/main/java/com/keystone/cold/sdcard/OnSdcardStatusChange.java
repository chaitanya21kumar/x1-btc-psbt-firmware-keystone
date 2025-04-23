package com.x1-btc-psbt-firmware.cold.sdcard;

public interface OnSdcardStatusChange {
    String id();
    void onInsert();
    void onRemove();
}
