package com.x1-btc-psbt-firmware.cold.ui.fragment.multisigs.casa;

import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showExportResult;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.x1-btc-psbt-firmware.cold.viewmodel.GlobalViewModel.writeToSdcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import com.x1-btc-psbt-firmware.coinlib.accounts.MultiSig;
import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.CasaGuideBinding;
import com.x1-btc-psbt-firmware.cold.databinding.ExportSdcardModalBinding;
import com.x1-btc-psbt-firmware.cold.databinding.MultisigCasaExportXpubBinding;
import com.x1-btc-psbt-firmware.cold.ui.modal.ModalDialog;
import com.x1-btc-psbt-firmware.cold.update.utils.Storage;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.CryptoHDKey;

public class CasaExportXPubFragment extends CasaBaseFragment<MultisigCasaExportXpubBinding> {
    @Override
    protected int setView() {
        return R.layout.multisig_casa_export_xpub;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        CryptoHDKey cryptoHDKey = casaMultiSigViewModel.exportCryptoHDKey();
        UR ur = cryptoHDKey.toUR();
        mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
        mBinding.qrcodeLayout.qrcode.setData(ur.toString());
        mBinding.qrcodeLayout.frame.setLayoutParams(new LinearLayout.LayoutParams(320, 320));
        mBinding.done.setOnClickListener(v -> {
            Bundle bundle = getArguments();
            if (bundle == null) {
                navigateUp();
                return;
            }
            int time = Utilities.getCasaSetUpVisitedTime(mActivity);
            if (bundle.containsKey("isGuide") && bundle.getBoolean("isGuide", false)) {
                Utilities.setCasaSetUpVisitedTime(mActivity, ++time);
                navigate(R.id.action_to_casaMultisigFragment);
            } else {
                navigateUp();
            }
        });
        mBinding.info.setOnClickListener(v -> showExportGuide());
        mBinding.path.setText("(Path: " + MultiSig.CASA.getPath() + ")");
        mBinding.xpub.setText(casaMultiSigViewModel.getXPub(MultiSig.CASA));
        mBinding.exportToSdcard.setOnClickListener(v -> exportXpub());
    }

    private void exportXpub() {
        Storage storage = Storage.createByEnvironment();
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog modalDialog = ModalDialog.newInstance();
            ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.export_sdcard_modal, null, false);
            binding.title.setText(R.string.export_xpub_text_file);
            binding.fileName.setText(getFileName());
            binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
            binding.confirm.setOnClickListener(vv -> {
                modalDialog.dismiss();
                boolean result = writeToSdcard(storage, getX1-BTC-PSBT-FirmwareXPubFileContent(casaMultiSigViewModel.getXPub(MultiSig.CASA), casaMultiSigViewModel.getXfp()), getFileName());
                showExportResult(mActivity, null, result);
            });
            modalDialog.setBinding(binding);
            modalDialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private String getFileName() {
        return "x1-btc-psbt-firmware.txt";
    }

    private String getX1-BTC-PSBT-FirmwareXPubFileContent(String xpub, String mfp) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("# X1-BTC-PSBT-Firmware Extended Public Key File\n");
        stringBuffer.append("## For wallet with master key fingerprint: ").append(mfp).append("\n");
        stringBuffer.append("## ## IMPORTANT WARNING\n\n");
        stringBuffer.append("Do **not** deposit to any address in this file unless you have a working\n");
        stringBuffer.append("wallet system that is ready to handle the funds at that address!\n");
        stringBuffer.append("## Top-level, 'master' extended public key ('m/'):\n").append(xpub);
        return stringBuffer.toString();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
    }

    private void showExportGuide() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CasaGuideBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.casa_guide,
                null, false);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }
}
