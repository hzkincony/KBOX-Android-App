package com.kincony.KControl.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.ipcamera.demo.BridgeService;
import com.ipcamera.demo.utils.ContentCommon;
import com.ipcamera.demo.utils.VuidUtils;
import com.kincony.KControl.R;
import com.kincony.KControl.net.data.DeviceType;
import com.kincony.KControl.net.data.IPAddress;
import com.kincony.KControl.net.data.ProtocolType;
import com.kincony.KControl.net.data.database.KBoxDatabase;
import com.kincony.KControl.ui.adapter.ScanDeviceAdapter;
import com.kincony.KControl.ui.base.BaseActivity;
import com.kincony.KControl.utils.ThreadUtils;
import com.kincony.KControl.utils.ToastUtils;
import com.kincony.KControl.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import vstc2.nativecaller.NativeCaller;

public class ScanDeviceActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ScanDeviceAdapter scanDeviceAdapter;
    private List<IPAddress> ipAddressList;

    @Override
    public int getLayoutId() {
        return R.layout.activity_scan_device;
    }

    @Override
    public void initView() {
        ipAddressList = new ArrayList();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        recyclerView = findViewById(R.id.recyclerView);

        scanDeviceAdapter = new ScanDeviceAdapter();
        scanDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                IPAddress ipAddress = (IPAddress) adapter.getData().get(position);

                View dialogView = LayoutInflater.from(ScanDeviceActivity.this).inflate(R.layout.dialog_edit_scan_device, null, false);
                TextView deviceId = dialogView.findViewById(R.id.deviceId);
                EditText name = dialogView.findViewById(R.id.userName);
                EditText pwd = dialogView.findViewById(R.id.password);
                deviceId.setText(ipAddress.getDeviceId());
                name.setText(ipAddress.getDeviceUserName());
                pwd.setText(ipAddress.getDevicePassword());

                new AlertDialog.Builder(ScanDeviceActivity.this)
                        .setView(dialogView)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userName = name.getText().toString();
                                String password = pwd.getText().toString();
                                if (TextUtils.isEmpty(userName)) {
                                    ToastUtils.INSTANCE.showToastLong(getString(R.string.user_name_input));
                                    return;
                                }
                                if (TextUtils.isEmpty(password)) {
                                    ToastUtils.INSTANCE.showToastLong(getString(R.string.password_input));
                                    return;
                                }

                                connectCamera(ipAddress, ipAddress.getDeviceId(), userName, password);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(scanDeviceAdapter);
        recyclerView.setHasFixedSize(true);

        BridgeService.setAddCameraInterface(new BridgeService.AddCameraInterface() {
            @Override
            public void callBackSearchResultData(int cameraType, String strMac, String strName, String strDeviceID, String strIpAddr, int port) {
                if (isFinishing() || TextUtils.isEmpty(strDeviceID) || TextUtils.isEmpty(strDeviceID.trim())) {
                    return;
                }
                synchronized (recyclerView) {
                    strDeviceID = strDeviceID.trim();
                    IPAddress addressByDeviceId = KBoxDatabase.getInstance(ScanDeviceActivity.this).getAddressDao().getAddressByDeviceId(strDeviceID);
                    if (addressByDeviceId != null) return;
                    for (int i = 0; i < ipAddressList.size(); i++) {
                        if (strDeviceID.equals(ipAddressList.get(i).getDeviceId())) {
                            return;
                        }
                    }
                    IPAddress ipAddress = new IPAddress("", 0, DeviceType.CAMERA.getValue(), ProtocolType.CAMERA.getValue(), null, null, strDeviceID, null, null);
                    ipAddressList.add(ipAddress);
                    ThreadUtils.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            scanDeviceAdapter.setList(ipAddressList);
                        }
                    });
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanCamera();
            }
        });

        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(true);
            scanCamera();
        });

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 100; i++) {
//                    IPAddress ipAddress = new IPAddress("", 0, DeviceType.CAMERA.getValue(), ProtocolType.CAMERA.getValue(), null, null, "VC0294133OEZK", "IPCAM", "hificat882");
//                    if (!scanDeviceAdapter.getData().contains(ipAddress)) {
//                        scanDeviceAdapter.addData(ipAddress);
//                    }
//                }


//                IPAddress ipAddress2 = new IPAddress("", 0, DeviceType.CAMERA.getValue(), ProtocolType.CAMERA.getValue(), null, null, "VSTG463233XEUFT", "admin", "888888");
//                scanDeviceAdapter.addData(ipAddress2);
            }
        }, 1000);

    }

    private void disconnectCamera(String deviceId) {
        if (!isConnecting) return;

        isConnecting = false;

        ThreadUtils.network().execute(new Runnable() {
            @Override
            public void run() {
                NativeCaller.StopPPPP(deviceId);
            }
        });
    }

    private boolean isConnecting;

    private void connectCamera(IPAddress ipAddress, String deviceId, String userName, String password) {
        if (isConnecting) return;

        isConnecting = true;

        showProgressDialog(getString(R.string.connecting));

        BridgeService.setIpcamClientInterface(new BridgeService.IpcamClientInterface() {
            @Override
            public void BSMsgNotifyData(String did, int type, int param) {
                if (type == ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS
                        || type == ContentCommon.PPPP_MSG_VSNET_NOTIFY_TYPE_VUIDSTATUS) {
                    switch (param) {
                        case ContentCommon.PPPP_STATUS_ON_LINE:
                            // connect success
                            disconnectCamera(deviceId);

                            ThreadUtils.mainThread().execute(() -> {
                                if (isFinishing()) {
                                    return;
                                }

                                showProgressDialog(getString(R.string.connect_success));

                                recyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog(0);

                                        ipAddress.setDeviceUserName(userName);
                                        ipAddress.setDevicePassword(password);

                                        Intent result = new Intent();
                                        result.putExtra("device_result", Tools.INSTANCE.getGson().toJson(ipAddress));
                                        setResult(RESULT_OK, result);
                                        finish();
                                    }
                                }, 1000);

                            });
                            break;
                        case ContentCommon.PPPP_STATUS_INITIALING:
                        case ContentCommon.PPPP_STATUS_CONNECTING:
                            break;
                        case ContentCommon.PPPP_STATUS_DISCONNECT:
                            ThreadUtils.mainThread().execute(() -> {
                                if (isFinishing()) {
                                    return;
                                }
                                isConnecting = false;
                                dismissProgressDialog(0);
                            });
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_FAILED:
                        case ContentCommon.PPPP_STATUS_INVALID_ID:
                        case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE:
                        case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT:
                        case ContentCommon.PPPP_STATUS_CONNECT_ERRER:
                        case ContentCommon.PPPP_STATUS_INVALID_VUID:
                        case ContentCommon.PPPP_STATUS_ALLOT_VUID:
                            disconnectCamera(deviceId);

                            ThreadUtils.mainThread().execute(() -> {
                                if (isFinishing()) {
                                    return;
                                }
                                isConnecting = false;
                                showProgressDialog(getString(R.string.connect_fail));
                                dismissProgressDialog(1000);
                            });
                    }
                }
            }

            @Override
            public void BSSnapshotNotify(String did, byte[] bImage, int len) {

            }

            @Override
            public void callBackUserParams(String did, String user1, String pwd1, String user2, String pwd2, String user3, String pwd3) {

            }

            @Override
            public void CameraStatus(String did, int status) {

            }
        });

        ThreadUtils.io().execute(new Runnable() {
            @Override
            public void run() {

                NativeCaller.Init();

                if (VuidUtils.isVuid(deviceId)) {//vuid
                    int status = NativeCaller.StartVUID("0", password, 1, "", "", 0, deviceId, 0);
                    if (status == -2) {
                        ThreadUtils.mainThread().execute(() -> {
                            showProgressDialog(getString(R.string.connect_fail));
                            dismissProgressDialog(1000);
                        });
                    }
                } else {//uid
                    if (deviceId.toLowerCase().startsWith("vsta")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EFGFFBBOKAIEGHJAEDHJFEEOHMNGDCNJCDFKAKHLEBJHKEKMCAFCDLLLHAOCJPPMBHMNOMCJKGJEBGGHJHIOMFBDNPKNFEGCEGCBGCALMFOHBCGMFK", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstd")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "HZLXSXIALKHYEIEJHUASLMHWEESUEKAUIHPHSWAOSTEMENSQPDLRLNPAPEPGEPERIBLQLKHXELEHHULOEGIAEEHYEIEK-$$", 1);
                    } else if (deviceId.toLowerCase().startsWith("vstf")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "HZLXEJIALKHYATPCHULNSVLMEELSHWIHPFIBAOHXIDICSQEHENEKPAARSTELERPDLNEPLKEILPHUHXHZEJEEEHEGEM-$$", 1);
                    } else if (deviceId.toLowerCase().startsWith("vste")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBAKKIOGNJHEGHMFEEDGLNOHJMPHAFPBEDLADILKEKPDLBDDNPOHKKCIFKJBNNNKLCPPPNDBFDL", 0);
                    } else if (deviceId.toLowerCase().startsWith("pisr")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EFGFFBBOKAIEGHJAEDHJFEEOHMNGDCNJCDFKAKHLEBJHKEKMCAFCDLLLHAOCJPPMBHMNOMCJKGJEBGGHJHIOMFBDNPKNFEGCEGCBGCALMFOHBCGMFK", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstg")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBOKCIGGFJPECHIFNEBGJNLHOMIHEFJBADPAGJELNKJDKANCBPJGHLAIALAADMDKPDGOENEBECCIK:vstarcam2018", 0);
                    } else if (deviceId.toLowerCase().startsWith("vsth")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBLKGJIGEJLEKGOFMEDHAMHHJNAGGFABMCOBGJOLHLJDFAFCPPHGILKIKLMANNHKEDKOINIBNCPJOMK:vstarcam2018", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstb") || deviceId.toLowerCase().startsWith("vstc")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "ADCBBFAOPPJAHGJGBBGLFLAGDBJJHNJGGMBFBKHIBBNKOKLDHOBHCBOEHOKJJJKJBPMFLGCPPJMJAPDOIPNL", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstj")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBLKGJIGEJNEOHEFBEIGANCHHMBHIFEAHDEAMJCKCKJDJAFDDPPHLKJIHLMBENHKDCHPHNJBODA:vstarcam2019", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstk")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EBGDEJBJKGJFGJJBEFHPFCEKHGNMHNNMHMFFBICPAJJNLDLLDHACCNONGLLPJGLKANMJLDDHODMEBOCIJEMA:vstarcam2019", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstm")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EBGEEOBOKHJNHGJGEAGAEPEPHDMGHINBGIECBBCBBJIKLKLCCDBBCFODHLKLJJKPBOMELECKPNMNAICEJCNNJH:vstarcam2019", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstn")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBBKBIFGAIAFGHDFLFJGJNIGEMOHFFPAMDMAAIIKBKNCDBDDMOGHLKCJCKFBFMPLMCBPEMG:vstarcam2019", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstl")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBLKGJIGEJIEIGNFPEEHGNMHPNBGOFIBECEBLJDLMLGDKAPCNPFGOLLJFLJAOMKLBDFOGMAAFCJJPNFJP:vstarcam2019", 0);
                    } else if (deviceId.toLowerCase().startsWith("vstp")) {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "EEGDFHBLKGJIGEJLEIGJFLENHLNBHCNMGAFGBNCOAIJMLKKODNALCCPKGBLHJLLHAHMBKNDFOGNGBDCIJFMB:vstarcam2019", 0);
                    } else {
                        NativeCaller.StartPPPPExt(deviceId, userName, password, 1, "", "", 0);
                    }
                }
            }
        });

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnecting) {//连接超时处理
                    showProgressDialog(getString(R.string.connect_fail));
                    disconnectCamera(deviceId);
                    dismissProgressDialog(1000);
                }
            }
        }, 10000);
    }

    private void scanCamera() {
        scanDeviceAdapter.setList(new ArrayList<>());

        ThreadUtils.network().execute(new Runnable() {
            @Override
            public void run() {
                NativeCaller.StartSearch();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NativeCaller.StopSearch();

                if (isFinishing()) {
                    return;
                }

                ThreadUtils.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        if (scanDeviceAdapter.getData().size() == 0) {
                            ToastUtils.INSTANCE.showToastLong(getString(R.string.scan_result_null));
                        } else {
                            ToastUtils.INSTANCE.showToastLong(getString(R.string.scan_success));
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent();
        intent.setClass(this, BridgeService.class);
        startService(intent);
        ThreadUtils.network().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NativeCaller.PPPPInitial("ADCBBFAOPPJAHGJGBBGLFLAGDBJJHNJGGMBFBKHIBBNKOKLDHOBHCBOEHOKJJJKJBPMFLGCPPJMJAPDOIPNL");
                    NativeCaller.PPPPNetworkDetect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.setClass(this, BridgeService.class);
        stopService(intent);
    }
}
