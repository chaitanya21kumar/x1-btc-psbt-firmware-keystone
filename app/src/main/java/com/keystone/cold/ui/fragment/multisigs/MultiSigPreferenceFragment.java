package com.x1-btc-psbt-firmware.cold.ui.fragment.multisigs;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.x1-btc-psbt-firmware.cold.R;
import com.x1-btc-psbt-firmware.cold.Utilities;
import com.x1-btc-psbt-firmware.cold.databinding.MultisigModePreferenceBinding;
import com.x1-btc-psbt-firmware.cold.databinding.SelectWalletModeBinding;
import com.x1-btc-psbt-firmware.cold.ui.MainActivity;
import com.x1-btc-psbt-firmware.cold.ui.common.BaseBindingAdapter;
import com.x1-btc-psbt-firmware.cold.ui.fragment.BaseFragment;
import com.x1-btc-psbt-firmware.cold.viewmodel.multisigs.MultiSigMode;

import java.util.ArrayList;
import java.util.List;

public class MultiSigPreferenceFragment extends BaseFragment<MultisigModePreferenceBinding> {
    public static final String TAG = "MultisigEntry";
    private Adapter adapter;
    protected CharSequence[] values;
    protected String value;
    protected CharSequence[] entries;
    protected CharSequence[] subTitles;
    private List<Pair<String, String>> displayItems;

    @Override
    protected int setView() {
        return R.layout.multisig_mode_preference;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        adapter = new Adapter(mActivity);
        mBinding.list.setAdapter(adapter);
        mBinding.confirm.setOnClickListener(v -> {
            if (value.equals(MultiSigMode.CASA.getModeId())){
                Bundle bundle = new Bundle();
                bundle.putBoolean("isGuide", true);
                navigate(R.id.action_to_casaGuidePageOneFragment, bundle);
            } else if (value.equals(MultiSigMode.CARAVAN.getModeId())) {
                navigate(R.id.action_to_caravanMultisigFragment);
            } else if (value.equals(MultiSigMode.LEGACY.getModeId())) {
                navigate(R.id.action_to_legacyMultisigFragment);
            }
        });
        entries = getResources().getStringArray(getEntries());
        values = getResources().getStringArray(getValues());
        subTitles = getResources().getStringArray(getSubTitles());
        value = Utilities.getMultiSigMode(mActivity);
        displayItems = new ArrayList<>();
        for (int i = 0; i < entries.length; i++) {
            displayItems.add(Pair.create(values[i].toString(), entries[i].toString()));
        }
        adapter.setItems(displayItems);
    }

    protected int getEntries() {
        return R.array.multisig_mode_list;
    }

    protected int getValues() {
        return R.array.multisig_mode_list_values;
    }

    protected int getSubTitles() {
        return R.array.multisig_mode_list_sub_titles;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }


    protected class Adapter extends BaseBindingAdapter<Pair<String, String>, SelectWalletModeBinding> implements MultiSigModeCallback {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.select_wallet_mode;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SelectWalletModeBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.title.setText(displayItems.get(position).second);
            binding.title.setTypeface(binding.title.getTypeface(), Typeface.BOLD);
            if (subTitles == null) {
                binding.subTitle.setVisibility(View.GONE);
            } else {
                binding.subTitle.setText(subTitles[position]);
                binding.subTitle.setVisibility(View.VISIBLE);
            }
            binding.setModeId(displayItems.get(position).first);
            binding.setCallback(this);
            binding.setChecked(displayItems.get(position).first.equals(value));
        }

        @Override
        protected void onBindItem(SelectWalletModeBinding binding, Pair<String, String> item) {
        }

        @Override
        public void onSelect(String modeId) {
            String old = value;
            value = modeId;
            if (!old.equals(value)) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public interface MultiSigModeCallback {
        void onSelect(String modeId);
    }
}
