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

package com.x1-btc-psbt-firmware.cold.viewmodel.multisigs;

import static com.x1-btc-psbt-firmware.coinlib.Util.getExpubFingerprint;
import static com.x1-btc-psbt-firmware.coinlib.accounts.Account.MULTI_P2SH;
import static com.x1-btc-psbt-firmware.coinlib.accounts.Account.MULTI_P2SH_P2WSH;
import static com.x1-btc-psbt-firmware.coinlib.accounts.Account.MULTI_P2SH_P2WSH_TEST;
import static com.x1-btc-psbt-firmware.coinlib.accounts.Account.MULTI_P2SH_TEST;
import static com.x1-btc-psbt-firmware.coinlib.accounts.Account.MULTI_P2WSH;
import static com.x1-btc-psbt-firmware.coinlib.accounts.Account.MULTI_P2WSH_TEST;

import android.app.Application;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.coinlib.ExtendPubkeyFormat;
import com.x1-btc-psbt-firmware.coinlib.accounts.Account;
import com.x1-btc-psbt-firmware.coinlib.accounts.ExtendedPublicKeyVersion;
import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.coinlib.coins.BTC.Deriver;
import com.x1-btc-psbt-firmware.coinlib.utils.B58;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.DataRepository;
import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.callables.GetExtendedPublicKeyCallable;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigAddressEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.MultiSigWalletEntity;
import com.x1-btc-psbt-firmware.cold.db.entity.TxEntity;
import com.x1-btc-psbt-firmware.cold.update.utils.FileUtils;
import com.x1-btc-psbt-firmware.cold.update.utils.Storage;
import com.x1-btc-psbt-firmware.cold.util.HashUtil;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.InvalidMultisigPathException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.XfpNotMatchException;
import com.sparrowwallet.hummingbird.registry.CryptoAccount;
import com.sparrowwallet.hummingbird.registry.CryptoCoinInfo;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;
import com.sparrowwallet.hummingbird.registry.CryptoKeypath;
import com.sparrowwallet.hummingbird.registry.CryptoOutput;
import com.sparrowwallet.hummingbird.registry.PathComponent;
import com.sparrowwallet.hummingbird.registry.ScriptExpression;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LegacyMultiSigViewModel extends ViewModelBase {
    private final MutableLiveData<Boolean> addComplete = new MutableLiveData<>();
    protected final DataRepository repo;
    protected String mode;

    public LegacyMultiSigViewModel(@NonNull Application application) {
        super(application);
        mode = "generic";
        repo = ((MainApplication) application).getRepository();
    }

    public static JSONObject decodeColdCardWalletFile(String content) {
        /*
        # Coldcard Multisig setup file (created on 5271C071)
        #
        Name: CC-2-of-3
        Policy: 2 of 3
        Derivation: m/48'/0'/0'/2'
        Format: P2WSH

        748CC6AA: xpub6F6iZVTmc3KMgAUkV9JRNaouxYYwChRswPN1ut7nTfecn6VPRYLXFgXar1gvPUX27QH1zaVECqVEUoA2qMULZu5TjyKrjcWcLTQ6LkhrZAj
        C2202A77: xpub6EiTGcKqBQy2uTat1QQPhYQWt8LGmZStNqKDoikedkB72sUqgF9fXLUYEyPthqLSb6VP4akUAsy19MV5LL8SvqdzvcABYUpKw45jA1KZMhm
        5271C071: xpub6EWksRHwPbDmXWkjQeA6wbCmXZeDPXieMob9hhbtJjmrmk647bWkh7om5rk2eoeDKcKG6NmD8nT7UZAFxXQMjTnhENTwTEovQw3MDQ8jJ16
         */

        content = content.replaceAll("P2WSH-P2SH", MULTI_P2SH_P2WSH.getScript());
        JSONObject object = new JSONObject();
        JSONArray xpubs = new JSONArray();
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}");
        boolean isTest = false;
        int total = 0;
        int threshold = 0;
        String path = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    if (line.contains("Coldcard")) {
                        object.put("Creator", "Coldcard");
                    } else if (line.contains("X1-BTC-PSBT-Firmware")) {
                        object.put("Creator", "X1-BTC-PSBT-Firmware");
                    }
                }
                String[] splits = line.split(": ");
                if (splits.length != 2) continue;
                String label = splits[0];
                String value = splits[1];
                if (label.equals("Name")) {
                    object.put(label, value);
                } else if (label.equals("Policy")) {
                    String[] policy = value.split(" of ");
                    if (policy.length == 2) {
                        threshold = Integer.parseInt(policy[0]);
                        total = Integer.parseInt(policy[1]);
                    }
                    object.put(label, value);
                } else if (label.equals("Derivation")) {
                    path = value;
                } else if (label.equals("Format")) {
                    object.put(label, value);
                } else if (pattern.matcher(label).matches()) {
                    JSONObject xpub = new JSONObject();
                    if (ExtendPubkeyFormat.isValidXpub(value)) {
                        isTest = value.startsWith("tpub") || value.startsWith("Upub") || value.startsWith("Vpub");
                        object.put("isTest", isTest);
                        xpub.put("xfp", label);
                        xpub.put("xpub", ExtendedPublicKeyVersion.convertXPubVersion(value, MultiSig.ofPath(path).get(0).getXPubVersion()));
                        xpubs.put(xpub);
                    }
                }
            }

            if (!isValidMultisigPolicy(total, threshold) || xpubs.length() != total) {
                Log.w("Multisig", "invalid wallet policy");
                return null;
            }

            String format = object.getString("Format");

            boolean validDerivation = false;
            for (Account account : Account.values()) {
                if (account.isMainNet() == !isTest && account.getScript().equals(format)) {
                    validDerivation = true;
                    object.put("Derivation", account.getPath());
                    break;
                }
            }

            if (!validDerivation) {
                Log.w("Multisig", "invalid wallet validDerivation");
                return null;
            }

            object.put("Xpubs", xpubs);
        } catch (IOException | JSONException | NumberFormatException e) {
            e.printStackTrace();
            Log.w("Multisig", "invalid wallet ", e);
            return null;
        }
        if (content.split("Derivation").length > 2) {
            object = decodeMultDerivationWalletFile(content, object);
        }
        return object;
    }

    /*
    # Coldcard Multisig setup file (exported from unchained-wallets)
    # https://github.com/unchained-capital/unchained-wallets
    # v1.0.0
    #
    Name: My Multisig Wallet
    Policy: 2 of 3
    Format: P2WSH-P2SH

    Derivation: m/48'/0'/0'/1'/12/32/5
    748cc6aa: xpub6KMfgiWkVW33LfMbZoGjk6M3CvdZtrzkn38RP2SjbGGU9E85JTXDaX6Jn6bXVqnmq2EnRzWTZxeF3AZ1ZLcssM4DT9GY5RSuJBt1GRx3xm2
    Derivation: m/48'/0'/0'/1'/5/6/7
    5271c071: xpub6LfFMiP3hcgrKeTrho9MgKj2zdKGPsd6ufJzrsQNaHSFZ7uj8e1vnSwibBVQ33VfXYJM5zn9G7E9VrMkFPVcdRtH3Brg9ndHLJs8v2QtwHa
    Derivation: m/48'/0'/0'/1'/110/8/9
    c2202a77: xpub6LZnaHgbbxyZpChT4w9V5NC91qaZC9rrPoebgH3qGjZmcDKvPjLivfZSKLu5R1PjEpboNsznNwtqBifixCuKTfPxDZVNVN9mnjfTBpafqQf
    */
    private static JSONObject decodeMultDerivationWalletFile(String content, JSONObject object) {
        try {
            JSONArray xpubs = object.getJSONArray("Xpubs");
            String[] strings = content.split("Derivation: ");
            if (xpubs.length() + 1 != strings.length) return null;
            for (int i = 0; i < xpubs.length(); i++) {
                String path = strings[i+1].replace('h', '\'').split("\n")[0].trim();
                xpubs.getJSONObject(i).put("path", path);
                String xpub = xpubs.getJSONObject(i).getString("xpub");
                if (object.getBoolean("isTest")) {
                    xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.tpub);
                } else {
                    xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.xpub);
                }
                xpubs.getJSONObject(i).put("xpub", xpub);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w("Multisig", "invalid caravan wallet ", e);
        }
        return object;
    }

    private static boolean isValidMultisigPolicy(int total, int threshold) {
        return total <= 15 && total >= 2 && threshold <= total || threshold >= 1;
    }

    public LiveData<List<MultiSigWalletEntity>> getAllMultiSigWallet() {
        return repo.loadAllMultiSigWallet(mode);
    }

    public LiveData<List<MultiSigAddressEntity>> getMultiSigAddress(String walletFingerprint) {
        return repo.loadAddressForWallet(walletFingerprint);
    }

    public LiveData<List<TxEntity>> loadTxs(String walletFingerprint) {
        return repo.loadMultisigTxs(walletFingerprint);
    }

    public MultiSigWalletEntity getCurrentWalletSync() {
        return repo.loadMultisigWallet(getDefaultMultisigWallet());
    }

    public LiveData<MultiSigWalletEntity> getCurrentWallet() {
        String netmode = Utilities.isMainNet(getApplication()) ? "main" : "testnet";
        MutableLiveData<MultiSigWalletEntity> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            String defaultMultisgWalletFp = getDefaultMultisigWallet();
            if (!TextUtils.isEmpty(defaultMultisgWalletFp)) {
                MultiSigWalletEntity wallet = repo.loadMultisigWallet(defaultMultisgWalletFp);
                if (wallet != null && wallet.getNetwork().equals(netmode)) {
                    result.postValue(wallet);
                } else {
                    List<MultiSigWalletEntity> list = repo.loadAllMultiSigWalletSync(mode);
                    if (!list.isEmpty()) {
                        result.postValue(list.get(0));
                        setDefaultMultisigWallet(list.get(0).getWalletFingerPrint());
                    } else {
                        result.postValue(null);
                    }
                }
            } else {
                List<MultiSigWalletEntity> list = repo.loadAllMultiSigWalletSync(mode);
                if (!list.isEmpty()) {
                    result.postValue(list.get(0));
                    setDefaultMultisigWallet(list.get(0).getWalletFingerPrint());
                } else {
                    result.postValue(null);
                }
            }
        });
        return result;
    }

    public LiveData<JSONObject> exportWalletToCaravan(String walletFingerprint) {
        MutableLiveData<JSONObject> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            if (wallet != null) {
                try {
                    String creator = wallet.getCreator();
                    String path = wallet.getExPubPath();
                    int threshold = wallet.getThreshold();
                    int total = wallet.getTotal();
                    boolean isTest = "testnet".equals(wallet.getNetwork());
                    JSONObject object = new JSONObject();
                    object.put("name", wallet.getWalletName());
                    object.put("addressType", MultiSig.ofPath(path, !isTest).get(0).getScript());
                    object.put("network", isTest ? "testnet" : "mainnet");

                    JSONObject client = new JSONObject();
                    client.put("type", "public");
                    object.put("client", client);

                    JSONObject quorum = new JSONObject();
                    quorum.put("requiredSigners", threshold);
                    quorum.put("totalSigners", total);
                    object.put("quorum", quorum);

                    JSONArray extendedPublicKeys = new JSONArray();
                    JSONArray xpubArray = new JSONArray(wallet.getExPubs());
                    for (int i = 0; i < xpubArray.length(); i++) {
                        JSONObject xpubKey = new JSONObject();
                        xpubKey.put("name", "Extended Public Key " + (i + 1));
                        xpubKey.put("bip32Path", xpubArray.getJSONObject(i).optString("path", path));
                        String xpub = xpubArray.getJSONObject(i).getString("xpub");
                        if (isTest) {
                            xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.tpub);
                        } else {
                            xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.xpub);
                        }
                        xpubKey.put("xpub", xpub);
                        xpubKey.put("xfp", xpubArray.getJSONObject(i).optString("xfp"));
                        xpubKey.put("method", "Caravan".equals(creator) ? xpubArray.getJSONObject(i).optString("method") : "text");
                        extendedPublicKeys.put(xpubKey);
                    }
                    object.put("extendedPublicKeys", extendedPublicKeys);
                    object.put("startingAddressIndex", 0);
                    result.postValue(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    public LiveData<String> exportWalletToCosigner(String walletFingerprint) {
        MutableLiveData<String> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            if (wallet != null) {
                try {
                    StringBuilder builder = new StringBuilder();
                    String path = wallet.getExPubPath();
                    String expubs = wallet.getExPubs();
                    boolean isHasPath = false;
                    if (expubs != null) {
                        isHasPath = wallet.getExPubs().contains("path");
                    }
                    int threshold = wallet.getThreshold();
                    int total = wallet.getTotal();
                    builder.append(String.format("# X1-BTC-PSBT-Firmware Multisig setup file (created on %s)", getXfp())).append("\n")
                            .append("#").append("\n")
                            .append("Name: ").append(wallet.getWalletName()).append("\n")
                            .append(String.format("Policy: %d of %d", threshold, total)).append("\n");
                    if (!isHasPath) {
                        builder.append("Derivation: ").append(path).append("\n");
                    }
                    builder.append("Format: ").append(MultiSig.ofPath(path).get(0).getScript()).append("\n\n");
                    JSONArray xpubs = new JSONArray(wallet.getExPubs());
                    for (int i = 0; i < xpubs.length(); i++) {
                        JSONObject xpub = xpubs.getJSONObject(i);
                        if (isHasPath) {
                            builder.append("Derivation: ").append(xpub.optString("path")).append("\n");
                        }
                        String xPub = xpub.getString("xpub");
                        if (TextUtils.equals(mode, "caravan")) {
                            xPub = ExtendPubkeyFormat.convertExtendPubkey(xPub, Utilities.isMainNet(MainApplication.getApplication())
                                    ? ExtendPubkeyFormat.xpub : ExtendPubkeyFormat.tpub);
                        }
                        builder.append(xpub.getString("xfp")).append(": ").append(xPub).append("\n");
                    }
                    result.postValue(builder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return result;
    }

    public LiveData<MultiSigWalletEntity> getWalletEntity(String walletFingerprint) {
        MutableLiveData<MultiSigWalletEntity> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            if (wallet != null) {
                result.postValue(wallet);
            }
        });
        return result;
    }

    public String getExportXpubInfo(Account account) {
        JSONObject object = new JSONObject();
        try {
            object.put("xfp", getXfp());
            object.put("xpub", getXPub(account));
            object.put("path", account.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    public CryptoAccount getCryptoAccount(Account account) {
        return new CryptoAccount(Hex.decode(getXfp()), Arrays.asList(getCryptoOutput(account)));
    }

    public CryptoOutput getCryptoOutput(Account account) {
        String masterFingerprint = getXfp();
        String xpub = getXPub(account);
        byte[] xpubBytes = new B58().decodeAndCheck(xpub);
        byte[] parentFp = Arrays.copyOfRange(xpubBytes, 5, 9);
        byte[] key = Arrays.copyOfRange(xpubBytes, 45, 78);
        byte[] chainCode = Arrays.copyOfRange(xpubBytes, 13, 45);
        int depth = xpubBytes[4];
        List<ScriptExpression> scriptExpressions = new ArrayList<>();
        CryptoKeypath origin;
        int network = 0;
        switch (account) {
            case MULTI_P2SH_TEST:
                network = 1;
                scriptExpressions.add(ScriptExpression.SCRIPT_HASH);
                origin = new CryptoKeypath(Arrays.asList(new PathComponent(45, true)), Hex.decode(masterFingerprint), depth);
                break;
            case MULTI_P2SH_P2WSH_TEST:
                network = 1;
                scriptExpressions.add(ScriptExpression.SCRIPT_HASH);
                scriptExpressions.add(ScriptExpression.WITNESS_SCRIPT_HASH);
                origin = new CryptoKeypath(Arrays.asList(new PathComponent(48, true),
                        new PathComponent(1, true),
                        new PathComponent(0, true),
                        new PathComponent(1, true)
                ), Hex.decode(masterFingerprint), depth);
                break;
            case MULTI_P2WSH_TEST:
                network = 1;
                scriptExpressions.add(ScriptExpression.WITNESS_SCRIPT_HASH);
                origin = new CryptoKeypath(Arrays.asList(new PathComponent(48, true),
                        new PathComponent(1, true),
                        new PathComponent(0, true),
                        new PathComponent(2, true)
                ), Hex.decode(masterFingerprint), depth);
                break;
            case MULTI_P2SH:
                scriptExpressions.add(ScriptExpression.SCRIPT_HASH);
                origin = new CryptoKeypath(Arrays.asList(new PathComponent(45, true)), Hex.decode(masterFingerprint), depth);
                break;
            case MULTI_P2SH_P2WSH:
                scriptExpressions.add(ScriptExpression.SCRIPT_HASH);
                scriptExpressions.add(ScriptExpression.WITNESS_SCRIPT_HASH);
                origin = new CryptoKeypath(Arrays.asList(new PathComponent(48, true),
                        new PathComponent(0, true),
                        new PathComponent(0, true),
                        new PathComponent(1, true)
                ), Hex.decode(masterFingerprint), depth);
                break;
            case MULTI_P2WSH:
                scriptExpressions.add(ScriptExpression.WITNESS_SCRIPT_HASH);
                origin = new CryptoKeypath(Arrays.asList(new PathComponent(48, true),
                        new PathComponent(0, true),
                        new PathComponent(0, true),
                        new PathComponent(2, true)
                ), Hex.decode(masterFingerprint), depth);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + account);
        }
        CryptoCoinInfo coinInfo = new CryptoCoinInfo(1, network);


        CryptoHDKey hdKey = new CryptoHDKey(false, key, chainCode, coinInfo, origin, null, parentFp);
        return new CryptoOutput(scriptExpressions, hdKey);
    }

    public String getExportAllXpubInfo() {
        JSONObject object = new JSONObject();
        try {
            Account[] accounts = Utilities.isMainNet(getApplication()) ?
                    new Account[]{MULTI_P2WSH, MULTI_P2SH_P2WSH, MULTI_P2SH} :
                    new Account[]{MULTI_P2WSH_TEST, MULTI_P2SH_P2WSH_TEST, MULTI_P2SH_TEST};
            for (Account value : accounts) {
                String format = value.getScript().toLowerCase().replace("-", "_");
                object.put(format + "_deriv", value.getPath());
                object.put(format, getXPub(value));
            }
            object.put("xfp", getXfp().toUpperCase());
            return object.toString(2).replace("\\", "");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getExportXpubFileName(Account account) {
        return getXfp() + "_" + account.getScript() + ".json";
    }

    public String getExportAllXpubFileName() {
        return "ccxp-" + getXfp() + ".json";
    }

    public String getAddressTypeString(Account account) {
        int id = R.string.multi_sig_account_segwit;

        if (account == MULTI_P2SH_P2WSH || account == MULTI_P2SH_P2WSH_TEST) {
            id = R.string.multi_sig_account_p2sh;
        } else if (account == MULTI_P2SH || account == MULTI_P2SH_TEST) {
            id = R.string.multi_sig_account_legacy;
        }

        return getApplication().getString(id);
    }

    public LiveData<MultiSigWalletEntity> createMultisigWallet(int threshold,
                                                               Account account,
                                                               String name,
                                                               JSONArray xpubsInfo, String creator)
            throws XfpNotMatchException, InvalidMultisigPathException {
        MutableLiveData<MultiSigWalletEntity> result = new MutableLiveData<>();
        int total = xpubsInfo.length();
        List<String> xpubs = getXpubs(account, xpubsInfo);
        MultiSigWalletEntity wallet = createMultiSigWalletEntity(threshold, account, name, xpubsInfo, creator, total, xpubs);
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean exist = repo.loadMultisigWallet(wallet.getWalletFingerPrint()) != null;
            if (!exist) {
                repo.addMultisigWallet(wallet);
                new AddAddressTask(wallet.getWalletFingerPrint(), repo, null, 0).execute(1);
                new AddAddressTask(wallet.getWalletFingerPrint(), repo, () -> result.postValue(wallet), 1).execute(1);
            } else {
                repo.updateWallet(wallet);
                result.postValue(wallet);
            }
            setDefaultMultisigWallet(wallet.getWalletFingerPrint());
        });
        return result;
    }

    @NonNull
    protected MultiSigWalletEntity createMultiSigWalletEntity(int threshold, Account account, String name, JSONArray xpubsInfo, String creator, int total, List<String> xpubs) {
        String verifyCode = calculateWalletVerifyCode(threshold, xpubs, account.getPath());
        String walletFingerprint = verifyCode + getXfp();
        String walletName = !TextUtils.isEmpty(name) ? name : "KT_" + verifyCode + "_" + threshold + "-" + total;
        MultiSigWalletEntity wallet = new MultiSigWalletEntity(
                walletName,
                threshold,
                total,
                account.getPath(),
                xpubsInfo.toString(),
                getXfp(),
                Utilities.isMainNet(getApplication()) ? "main" : "testnet", verifyCode, creator, mode);
        wallet.setWalletFingerPrint(walletFingerprint);
        return wallet;
    }

    @NonNull
    protected List<String> getXpubs(Account account, JSONArray xpubsInfo) throws XfpNotMatchException, InvalidMultisigPathException {
        boolean xfpMatch = false;
        List<String> xpubs = new ArrayList<>();
        for (int i = 0; i < xpubsInfo.length(); i++) {
            JSONObject obj;
            try {
                obj = xpubsInfo.getJSONObject(i);
                String xfp = obj.getString("xfp");
                String xpub = ExtendedPublicKeyVersion.convertXPubVersion(obj.getString("xpub"), account.getXPubVersion());
                String path = obj.optString("path");
                if (!path.isEmpty()) {
                    String[] strings = path.split("/");
                    if (strings.length == 1) {
                        throw new InvalidMultisigPathException("bip32Path not exist");
                    } else if (strings.length > 9) {
                        throw new InvalidMultisigPathException("maximum support depth of 8 layers");
                    }
                    String xPub = new GetExtendedPublicKeyCallable(path).call();
                    if ((xfp.equalsIgnoreCase(getXfp()) || xfp.equalsIgnoreCase(getExpubFingerprint(xPub)))
                            && ExtendPubkeyFormat.isEqualIgnorePrefix(xPub, xpub)) {
                        xfpMatch = true;
                    }
                } else {
                    if ((xfp.equalsIgnoreCase(getXfp()) || xfp.equalsIgnoreCase(getExpubFingerprint(getXPub(account))))
                            && ExtendPubkeyFormat.isEqualIgnorePrefix(getXPub(account), xpub)) {
                        xfpMatch = true;
                    }
                }
                xpubs.add(xpub);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!xfpMatch) {
            throw new XfpNotMatchException("xfp not match");
        }
        return xpubs;
    }

    public void setDefaultMultisigWallet(String walletFingerprint) {
        Utilities.setDefaultMultisigWallet(getApplication(), getXfp(), walletFingerprint);
    }

    protected String getDefaultMultisigWallet() {
        return Utilities.getDefaultMultisigWallet(getApplication(), getXfp());
    }

    public void addAddress(String walletFingerprint, int number, int changeIndex) {
        addComplete.postValue(Boolean.FALSE);
        new AddAddressTask(walletFingerprint, repo, () -> addComplete.setValue(Boolean.TRUE), changeIndex).execute(number);
    }

    public String calculateWalletVerifyCode(int threshold, List<String> xpubs, String path) {
        String info = xpubs.stream()
                .map(s -> ExtendPubkeyFormat.convertExtendPubkey(s, ExtendPubkeyFormat.xpub))
                .sorted()
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("") + threshold + "of" + xpubs.size() + path;
        return Hex.toHexString(Objects.requireNonNull(HashUtil.sha256(info))).substring(0, 8).toUpperCase();
    }

    public List<MultiSigAddressEntity> filterChangeAddress(List<MultiSigAddressEntity> entities) {
        return entities.stream()
                .filter(entity -> entity.getChangeIndex() == 1)
                .collect(Collectors.toList());
    }

    public List<MultiSigAddressEntity> filterReceiveAddress(List<MultiSigAddressEntity> entities) {
        return entities.stream()
                .filter(entity -> entity.getChangeIndex() == 0)
                .collect(Collectors.toList());
    }

    public LiveData<Boolean> getObservableAddState() {
        return addComplete;
    }

    public void deleteWallet(String walletFingerPrint) {
        repo.deleteMultisigWallet(walletFingerPrint);
        repo.deleteTxs(walletFingerPrint);
    }

    public LiveData<Map<String, JSONObject>> loadWalletFile() {
        MutableLiveData<Map<String, JSONObject>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            Map<String, JSONObject> fileList = new HashMap<>();
            Storage storage = Storage.createByEnvironment();
            if (storage != null && storage.getExternalDir() != null) {
                File[] files = storage.getExternalDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".txt")) {
                            JSONObject object = decodeColdCardWalletFile(FileUtils.readString(f));
                            if (object != null) {
                                fileList.put(f.getName(), object);
                            }
                        } else if (f.getName().endsWith(".json")) {
                            String fileContent = FileUtils.readString(f);
                            JSONObject object = decodeCaravanWalletFile(fileContent);
                            if (object != null) {
                                fileList.put(f.getName(), object);
                            }
                        }
                    }
                }
            }
            result.postValue(fileList);
        });
        return result;
    }

    public void updateWallet(MultiSigWalletEntity entity) {
        AppExecutors.getInstance().diskIO().execute(() -> repo.updateWallet(entity));
    }

    public static JSONObject decodeCaravanWalletFile(String content) {
        JSONObject result = new JSONObject();
        int total;
        int threshold;
        String format;
        boolean isTest;
        try {
            content = content.replaceAll("P2WSH-P2SH", MULTI_P2SH_P2WSH.getScript());
            JSONObject object = new JSONObject(content);

            //Creator
            result.put("Creator", "Caravan");

            //Name
            result.put("Name", object.getString("name"));

            //Policy
            JSONObject quorum = object.getJSONObject("quorum");
            threshold = quorum.getInt("requiredSigners");
            total = quorum.getInt("totalSigners");
            result.put("Policy", threshold + " of " + total);

            //Format
            format = object.getString("addressType");
            result.put("Format", format);

            //isTest
            isTest = "testnet".equals(object.getString("network"));
            result.put("isTest", isTest);

            //Derivation
            Account account = MultiSig.ofScript(format, !isTest).get(0);
            if (account == null) {
                Log.w("Multisig", "invalid format");
                return null;
            }
            result.put("Derivation", account.getPath());

            //Xpubs
            JSONArray xpubs = object.getJSONArray("extendedPublicKeys");
            JSONArray xpubsArray = new JSONArray();
            for (int i = 0; i < xpubs.length(); i++) {
                JSONObject xpubJson = new JSONObject();
                String xpub = xpubs.getJSONObject(i).getString("xpub");
                String path = xpubs.getJSONObject(i).getString("bip32Path");
                String xfp = xpubs.getJSONObject(i).getString("xfp");
                String method = xpubs.getJSONObject(i).getString("method");
                if (ExtendPubkeyFormat.isValidXpub(xpub)) {
                    if (xpub.startsWith("tpub") || xpub.startsWith("Upub") || xpub.startsWith("Vpub")) {
                        object.put("isTest", true);
                    }
                    if (TextUtils.isEmpty(xfp)) {
                        xfp = getExpubFingerprint(xpub);
                    }
                    xpubJson.put("path", path);
                    xpubJson.put("xfp", xfp);
                    xpubJson.put("xpub", ExtendedPublicKeyVersion.convertXPubVersion(xpub, account.getXPubVersion()));
                    xpubJson.put("method", method);
                    xpubsArray.put(xpubJson);
                }
            }
            if (!isValidMultisigPolicy(total, threshold) || xpubs.length() != total) {
                Log.w("Multisig", "invalid wallet Policy");
                return null;
            }
            result.put("Xpubs", xpubsArray);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Multisig", "invalid wallet ", e);
        }
        return null;
    }

    public static class AddAddressTask extends AsyncTask<Integer, Void, Void> {
        private final String walletFingerprint;
        private final DataRepository repo;
        private final Runnable onComplete;
        private final int changeIndex;

        public AddAddressTask(String walletFingerprint,
                              DataRepository repo,
                              Runnable onComplete,
                              int changeIndex) {
            this.walletFingerprint = walletFingerprint;
            this.repo = repo;
            this.onComplete = onComplete;
            this.changeIndex = changeIndex;
        }

        @Override
        protected Void doInBackground(Integer... count) {
            boolean isMainNet = Utilities.isMainNet(MainApplication.getApplication());
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            List<MultiSigAddressEntity> address = repo.loadAddressForWalletSync(walletFingerprint);
            Optional<MultiSigAddressEntity> optional = address.stream()
                    .filter(addressEntity -> addressEntity.getPath()
                            .startsWith(wallet.getExPubPath() + "/" + changeIndex))
                    .max((o1, o2) -> o1.getIndex() - o2.getIndex());
            int index = -1;
            if (optional.isPresent()) {
                index = optional.get().getIndex();
            }
            List<MultiSigAddressEntity> entities = new ArrayList<>();
            int addressCount = index + 1;
            List<String> xpubList = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(wallet.getExPubs());
                for (int i = 0; i < jsonArray.length(); i++) {
                    xpubList.add(jsonArray.getJSONObject(i).getString("xpub"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Deriver deriver = new Deriver(isMainNet);
            for (int i = 0; i < count[0]; i++) {
                MultiSigAddressEntity multisigAddress = new MultiSigAddressEntity();
                String addr = deriver.deriveMultiSigAddress(wallet.getThreshold(),
                        xpubList, new int[]{changeIndex, addressCount + i},
                        MultiSig.ofPath(wallet.getExPubPath()).get(0));
                multisigAddress.setPath(wallet.getExPubPath() + "/" + changeIndex + "/" + (addressCount + i));
                multisigAddress.setAddress(addr);
                multisigAddress.setIndex(i + addressCount);
                multisigAddress.setName("BTC-" + (i + addressCount));
                multisigAddress.setWalletFingerPrint(walletFingerprint);
                multisigAddress.setChangeIndex(changeIndex);
                entities.add(multisigAddress);
            }
            repo.insertMultisigAddress(entities);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
}


