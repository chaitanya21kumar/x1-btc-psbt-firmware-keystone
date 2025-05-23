/*
 *
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
 *
 */

package com.x1-btc-psbt-firmware.cold.db.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Deriver;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "multi_sig_wallet", indices = {@Index("walletFingerPrint")})
public class MultiSigWalletEntity {
    @PrimaryKey
    @NonNull
    private String walletFingerPrint; // generic mode： verifyCode + xfp，other modes： verifyCode + xfp + _ + mode name
    private String walletName;
    @NonNull
    private int threshold;
    @NonNull
    private int total;
    @NonNull
    private String exPubPath;
    @NonNull
    private String exPubs;
    @NonNull
    private String belongTo;
    @NonNull
    private String verifyCode;
    @NonNull
    private String network;
    @NonNull
    private String creator;
    @NonNull
    private String mode;

    private String addition;

    public void setAddition(String json) {
        this.addition = json;
    }

    public String getAddition() {
        return addition;
    }

    public MultiSigWalletEntity(String walletName, int threshold, int total,
                                String exPubPath, String exPubs, String belongTo,
                                String network, String verifyCode, String creator,
                                String mode) {
        this.walletName = walletName;
        this.threshold = threshold;
        this.total = total;
        this.exPubPath = exPubPath;
        this.exPubs = exPubs;
        this.belongTo = belongTo;
        this.network = network;
        this.verifyCode = verifyCode;
        this.creator = creator;
        this.mode = mode;
    }

    @NonNull
    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(@NonNull String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getWalletFingerPrint() {
        return walletFingerPrint;
    }

    public void setWalletFingerPrint(String fingerPrint) {
        this.walletFingerPrint = fingerPrint;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getExPubPath() {
        return exPubPath;
    }

    public void setExPubPath(String expubPath) {
        this.exPubPath = expubPath;
    }

    public String getExPubs() {
        return exPubs;
    }

    public void setExPubs(String xPubs) {
        this.exPubs = xPubs;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    @NonNull
    public String getCreator() {
        return creator;
    }

    public void setCreator(@NonNull String creator) {
        this.creator = creator;
    }

    @NonNull
    public String getMode() {
        return mode;
    }

    public void setMode(@NonNull String mode) {
        this.mode = mode;
    }

    public String deriveAddress(int[] index, boolean isMainnet) {
        Deriver deriver = new Deriver(isMainnet);
        List<String> xpubList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(getExPubs());
            for (int i = 0; i < jsonArray.length(); i++) {
                xpubList.add(jsonArray.getJSONObject(i).getString("xpub"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deriver.deriveMultiSigAddress(getThreshold(),
                xpubList, new int[] {index[0], index[1]},
                MultiSig.ofPath(getExPubPath()).get(0));
    }

    @Override
    public String toString() {
        return "MultiSigWalletEntity{" +
                "WalletFingerPrint=" + walletFingerPrint +
                ", walletName='" + walletName + '\'' +
                ", threshold=" + threshold +
                ", total=" + total +
                ", exPubPath='" + exPubPath + '\'' +
                ", exPubs='" + exPubs + '\'' +
                ", belongTo='" + belongTo + '\'' +
                ", network='" + network + '\'' +
                ", creator='" + creator + '\'' +
                ", mode='" + mode + '\'' +
                '}';
    }
}
