package com.kincony.KControl.ui.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.kincony.KControl.R;
import com.kincony.KControl.net.data.IPAddress;
import com.kincony.KControl.utils.Tools;

import java.util.ArrayList;

public class ScanDeviceAdapter extends BaseQuickAdapter<IPAddress, BaseViewHolder> {

    public ScanDeviceAdapter() {
        super(R.layout.item_scan_device,new ArrayList<>());
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, IPAddress ipAddress) {
        baseViewHolder.setText(R.id.deviceId, ipAddress.getDeviceId());
        baseViewHolder.setText(R.id.deviceType, Tools.INSTANCE.getDeviceTypeEnum(ipAddress.getDeviceType()).getTypeName());
    }


}
