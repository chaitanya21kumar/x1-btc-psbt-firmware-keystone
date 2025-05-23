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

package com.x1-btc-psbt-firmware.cold;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;

import static android.content.Context.MODE_PRIVATE;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.FingerprintPreferenceFragment.FINGERPRINT_UNLOCK;
import static com.x1-btc-psbt-firmware.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_MULTI_SIG_MODE;
import static com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel.VAULT_CREATE_STEP;
import static com.x1-btc-psbt-firmware.cold.viewmodel.SetupVaultViewModel.VAULT_CREATE_STEP_WELCOME;

public class Utilities {
    private static final String PREFERENCE_SECRET = "secret";
    private static final String PREFERENCE_KEY_PATTERN = "pattern";
    private static final String PREFERENCE_KEY_VAULT_CREATED = "vault_created";
    public static final String PREFERENCE_KEY_PASSWORD_SET = "password_set";
    private static final String PREFERENCE_KEY_LANGUAGE_SET = "language_set";
    private static final String PREFERENCE_KEY_VAULT_ID = "vault_id";

    public static final String SHARED_PREFERENCES_KEY = "com.x1-btc-psbt-firmware.cold.prefs";

    public static final String IS_SETUP_VAULT = "is_setup_vault";
    private static final String PREFERENCE_KEY_BELONG_TO = "belong_to_v2";
    private static final String PREFERENCE_KEY_LEGACY_BELONG_TO = "belong_to";
    private static final String PREFERENCE_KEY_PWD_RETRY = "pwd_retry_times";
    private static final String PREFERENCE_KEY_PATTERN_RETRY = "pattern_retry_times";
    private static final String FINGERPRINT_CLICKED = "fingerprint_clicked";
    private static final String PATTERN_LOCK_CLICKED = "pattern_lock_clicked";
    private static final String FINGERPRINT_PASSWORD = "fingerprint_password";
    private static final String ATTACK_DETECTED = "attack_detected";
    private static final String INPUT_SETTINGS_CLEARED = "input_settings_cleared";
    public static final String NET_MDOE = "network_mode";
    public static final String CASA_GUIDE_VISITED_TIME = "casa_guide_visited_time";

    public static void alert(AppCompatActivity activity,
                             @Nullable String title, @NonNull String message,
                             String buttonText, Runnable action) {
        ModalDialog.showCommonModal(activity,
                title,
                message,
                buttonText,
                action);
    }

    public static boolean hasVaultCreated(Context activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_KEY_VAULT_CREATED, false);
    }

    public static void setVaultCreated(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_VAULT_CREATED, true).apply();
    }

    public static boolean hasPasswordSet(Context activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_KEY_PASSWORD_SET, false);
    }

    public static void markPasswordSet(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_PASSWORD_SET, true).apply();
    }

    public static void clearPasswordSet(Activity context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_PASSWORD_SET, false).apply();
    }

    public static void setLanguageSet(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PREFERENCE_KEY_LANGUAGE_SET, true).apply();
    }

    public static boolean hasLanguageSet(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PREFERENCE_KEY_LANGUAGE_SET, false);
    }

    public static boolean isPatternUnlock(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return !TextUtils.isEmpty(sp.getString(PREFERENCE_KEY_PATTERN, ""));
    }

    public static boolean verifyPatternUnlock(Activity activity, String patternSha1) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return patternSha1.equals(sp.getString(PREFERENCE_KEY_PATTERN, ""));
    }

    public static void setPattern(Activity activity, String s) {
        SharedPreferences sp = activity.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_PATTERN, s).apply();
    }

    public static void clearPatternUnlock(FragmentActivity activity) {
        setPattern(activity, "");
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
    }

    public static String getVaultId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_VAULT_ID, "");
    }

    public static void setVaultId(Context context, String id) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_VAULT_ID, id).apply();
    }

    public static void setCurrentBelongTo(Context context, String s) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putString(PREFERENCE_KEY_BELONG_TO, s).apply();
    }

    public static String getCurrentBelongTo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_BELONG_TO, "main");
    }

    public static void setPasswordRetryTimes(Context context, int times) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putInt(PREFERENCE_KEY_PWD_RETRY, times).apply();
    }

    public static int getPasswordRetryTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getInt(PREFERENCE_KEY_PWD_RETRY, 0);
    }

    public static void setPatternRetryTimes(Context context, int times) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putInt(PREFERENCE_KEY_PATTERN_RETRY, times).apply();
    }

    public static int getPatternRetryTimes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getInt(PREFERENCE_KEY_PATTERN_RETRY, 0);
    }

    public static boolean isFingerprintUnlockEnable(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(FINGERPRINT_UNLOCK, false);
    }

    public static void setFingerprintUnlockEnable(Context context, boolean enable) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(FINGERPRINT_UNLOCK, enable).apply();
    }

    public static boolean hasUserClickFingerprint(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(FINGERPRINT_CLICKED, false);
    }

    public static void setUserClickFingerprint(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(FINGERPRINT_CLICKED, true).apply();
    }

    public static boolean hasUserClickPatternLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(PATTERN_LOCK_CLICKED, false);
    }

    public static void setUserClickPatternLock(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(PATTERN_LOCK_CLICKED, true).apply();
    }

    public static String getFingerprintPassword(Context context) {
        return Settings.System.getString(context.getContentResolver(), FINGERPRINT_PASSWORD);
    }

    public static boolean setFingerprintPassword(Context context, String pwd) {
        return Settings.System.putString(context.getContentResolver(), FINGERPRINT_PASSWORD, pwd);
    }

    public static void setAttackDetected(Context context, boolean attacked) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        sp.edit().putBoolean(ATTACK_DETECTED, attacked).apply();
    }

    static boolean isAttackDetected(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
        return sp.getBoolean(ATTACK_DETECTED, false);
    }


    public static boolean isMainNet(Context context) {
        return getPrefs(context).getString(NET_MDOE, "mainnet").equals("mainnet");
    }

    public static Coins.Coin currentCoin(Context context) {
        return isMainNet(context) ? Coins.BTC : Coins.XTN;
    }

    public static void setDefaultMultisigWallet(Context context, String xfp, String walletFingerprint) {
        getPrefs(context).edit().putString(xfp + "_default", walletFingerprint).apply();
    }

    public static String getDefaultMultisigWallet(Context context, String xfp) {
        return getPrefs(context).getString(xfp + "_default", "");
    }

    public static void setDefaultCaravanMultisigWallet(Context context, String xfp, String walletFingerprint) {
        getPrefs(context).edit().putString(xfp + "_caravan", walletFingerprint).apply();
    }

    public static String getDefaultCaravanMultisigWallet(Context context, String xfp) {
        return getPrefs(context).getString(xfp + "_caravan", "");
    }

    public static boolean hasMultiSigMode(Context context) {
        return getPrefs(context).contains(SETTING_MULTI_SIG_MODE);
    }

    public static String getMultiSigMode(Context context) {
        return getPrefs(context).getString(SETTING_MULTI_SIG_MODE, MultiSigMode.CASA.getModeId());
    }

    public static void setMultiSigMode(Context context, String modeId) {
        getPrefs(context).edit().putString(SETTING_MULTI_SIG_MODE, modeId).apply();
    }

    public static int getCasaSetUpVisitedTime(Context context) {
        return getPrefs(context).getInt(CASA_GUIDE_VISITED_TIME, 0);
    }

    public static void setCasaSetUpVisitedTime(Context context, int time) {
        getPrefs(context).edit().putInt(CASA_GUIDE_VISITED_TIME, time).apply();
    }

    public static void setVaultCreateStep(Context context, int step) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        sp.edit().putInt(VAULT_CREATE_STEP, step).apply();
    }

    public static Integer getVaultCreateStep(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        return sp.getInt(VAULT_CREATE_STEP, VAULT_CREATE_STEP_WELCOME);
    }
}
