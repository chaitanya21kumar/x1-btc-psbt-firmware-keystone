package com.x1-btc-psbt-firmware.coinlib;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static byte[] encrypt(byte[] sSrc, byte[] keyBytes, byte[] ivBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        return cipher.doFinal(sSrc);
    }


    public static byte[] decrypt(byte[] sSrc, byte[] keyBytes, byte[] ivBytes) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            return cipher.doFinal(sSrc);
        } catch (Exception ex) {
            Log.i(ALGORITHM, "decrypt: " + ex.getMessage());
            return null;
        }
    }




}
