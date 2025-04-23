package com.x1-btc-psbt-firmware.cold.viewmodel;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.x1-btc-psbt-firmware.coinlib.exception.CoinNotFindException;
import com.x1-btc-psbt-firmware.coinlib.exception.InvalidTransactionException;
import com.x1-btc-psbt-firmware.coinlib.utils.Coins;
import com.x1-btc-psbt-firmware.cold.MainApplication;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.callables.GetMasterFingerprintCallable;
import com.x1-btc-psbt-firmware.cold.protocol.ZipUtil;
import com.x1-btc-psbt-firmware.cold.protocol.parser.ProtoParser;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.UnknowQrCodeException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.WatchWalletNotMatchException;
import com.x1-btc-psbt-firmware.cold.viewmodel.exceptions.XfpNotMatchException;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import static com.x1-btc-psbt-firmware.cold.ui.fragment.main.TxConfirmFragment.KEY_TX_DATA;

public class X1-BTC-PSBT-FirmwareTxViewModel extends AndroidViewModel {

    private Application application;

    public X1-BTC-PSBT-FirmwareTxViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public JSONObject decodeAsProtobuf(String hex) {
        hex = ZipUtil.unzip(hex);
        return new ProtoParser(Hex.decode(hex)).parseToJson();
    }

    public Bundle decodeAsBundle(JSONObject object)
            throws InvalidTransactionException,
            CoinNotFindException,
            JSONException,
            XfpNotMatchException,
            UnknowQrCodeException, WatchWalletNotMatchException {
        WatchWallet wallet = WatchWallet.getWatchWallet(MainApplication.getApplication());
        if (wallet != WatchWallet.KEYSTONE) {
            throw new WatchWalletNotMatchException("not support bc32 qrcode in current wallet mode");
        }
        String type = object.getString("type");
        switch (type) {
            case "TYPE_SIGN_TX":
                return handleSignKeyStoneTx(object);
            default:
                throw new UnknowQrCodeException("unknow qrcode type " + type);
        }
    }

    private Bundle handleSignKeyStoneTx(JSONObject object)
            throws InvalidTransactionException, CoinNotFindException, XfpNotMatchException {
        checkXfp(object);
        try {
            String coinCode = object.getJSONObject("signTx")
                    .getString("coinCode");

            boolean isMainNet = Utilities.isMainNet(application);
            if (!Coins.isCoinSupported(coinCode)
                    || object.getJSONObject("signTx").has("omniTx")
                    || !isMainNet) {
                throw new CoinNotFindException("not support " + coinCode);
            }
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TX_DATA, object.getJSONObject("signTx").toString());
            return bundle;
        } catch (JSONException e) {
            throw new InvalidTransactionException("invalid transaction");
        }
    }

    private void checkXfp(JSONObject obj) throws XfpNotMatchException {
        String xfp = new GetMasterFingerprintCallable().call();
        if (!obj.optString("xfp").equals(xfp)) {
            throw new XfpNotMatchException("xfp not match");
        }
    }
}
