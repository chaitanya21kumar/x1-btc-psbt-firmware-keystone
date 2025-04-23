package com.x1-btc-psbt-firmware.cold.viewmodel.multisigs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.cold.AppExecutors;
import com.x1-btc-psbt-firmware.cold.DataRepository;
import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.db.entity.CasaSignature;
import com.x1-btc-psbt-firmware.cold.update.utils.Storage;
import com.x1-btc-psbt-firmware.cold.util.URRegistryHelper;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;

import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CasaMultiSigViewModel extends ViewModelBase {
    private final DataRepository repo;
    private static final Pattern unsignedMessageFilePattern = Pattern.compile("^\\d{8}-hc-[0-9a-fA-F]{8}.txt$");

    public CasaMultiSigViewModel(@NonNull Application application) {
        super(application);
        repo = ((MainApplication) application).getRepository();
    }

    public LiveData<List<CasaSignature>> loadCasaTxs(String belongto) {
        return repo.loadCasaTxs(belongto);
    }

    public List<CasaSignature> allCasaSignaturesSync() {
        return repo.loadCasaSignaturesSync();
    }

    public CryptoHDKey exportCryptoHDKey() {
        return URRegistryHelper.generateCryptoHDKey(MultiSig.CASA, Hex.decode(getXfp()), getXPub(MultiSig.CASA));
    }

    private boolean isUnsignedMessage(String fileName) {
        Matcher matcher = unsignedMessageFilePattern.matcher(fileName);
        return matcher.matches();
    }

    public LiveData<List<String>> loadUnsignedMessage() {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> fileList = new ArrayList<>();
            Storage storage = Storage.createByEnvironment();
            if (storage != null) {
                File[] files = storage.getExternalDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (isUnsignedMessage(f.getName())) {
                            fileList.add(f.getName());
                        }
                    }
                    for (File f : files) {
                        if (f.getName().endsWith(".txt") && !isUnsignedMessage(f.getName()) && !f.getName().startsWith(".")) {
                            fileList.add(f.getName());
                        }
                    }
                }
            }
            result.postValue(fileList);
        });
        return result;
    }
}
