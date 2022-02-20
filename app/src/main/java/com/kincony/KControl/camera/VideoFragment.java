package com.kincony.KControl.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ipcamera.demo.BridgeService;
import com.ipcamera.demo.utils.AudioPlayer;
import com.ipcamera.demo.utils.ContentCommon;
import com.ipcamera.demo.utils.CustomAudioRecorder;
import com.ipcamera.demo.utils.CustomBuffer;
import com.ipcamera.demo.utils.CustomBufferData;
import com.ipcamera.demo.utils.CustomBufferHead;
import com.ipcamera.demo.utils.MyRender;
import com.ipcamera.demo.utils.VuidUtils;
import com.kincony.KControl.R;
import com.kincony.KControl.net.data.DeviceType;
import com.kincony.KControl.net.data.IPAddress;
import com.kincony.KControl.utils.LogUtils;
import com.kincony.KControl.utils.ThreadUtils;
import com.kincony.KControl.utils.Tools;

import vstc2.nativecaller.NativeCaller;

public class VideoFragment extends Fragment {


    public static VideoFragment newInstance(IPAddress ipAddress, boolean isShowControlBtn) {
        if (ipAddress != null && ipAddress.getDeviceType() == DeviceType.CAMERA.getValue() && !TextUtils.isEmpty(ipAddress.getDeviceId())
                && !TextUtils.isEmpty(ipAddress.getDeviceUserName()) && !TextUtils.isEmpty(ipAddress.getDevicePassword())) {
            Bundle args = new Bundle();
            args.putString("ipAddress", Tools.INSTANCE.getGson().toJson(ipAddress));
            args.putBoolean("isShowControlBtn", isShowControlBtn);
            VideoFragment fragment = new VideoFragment();
            fragment.setArguments(args);
            return fragment;
        }
        return null;
    }

    private View rootView;
    private GLSurfaceView glSurfaceView;
    private RelativeLayout rlControl;
    private ImageView ivBack;
    private ImageView ivAudio;
    private ImageView ivTalk;
    private TextView tvVideoQuality;
    private ImageView ivUp;
    private ImageView ivLeft;
    private ImageView ivDown;
    private ImageView ivRight;
    private LinearLayout llProgressBar;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private ImageView ivPlay;

    private IPAddress ipAddress;

    private boolean isConnecting;
    private boolean isConnectingTimeout;
    private boolean isShowControlBtn;

    private PopupWindow videoQualityPop;

    private CustomBuffer audioBuffer;
    private AudioPlayer audioPlayer;
    private boolean isAudioStart;

    private CustomAudioRecorder customAudioRecorder;
    private boolean isTalkStart;

    private MyRender myRender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmemt_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            ipAddress = Tools.INSTANCE.getGson().fromJson(getArguments().getString("ipAddress"), IPAddress.class);
            isShowControlBtn = getArguments().getBoolean("isShowControlBtn", false);
        }

        rootView = view;
        glSurfaceView = rootView.findViewById(R.id.glSurfaceView);
        rlControl = rootView.findViewById(R.id.rlControl);
        ivBack = rootView.findViewById(R.id.ivBack);
        ivAudio = rootView.findViewById(R.id.ivAudio);
        ivTalk = rootView.findViewById(R.id.ivTalk);
        tvVideoQuality = rootView.findViewById(R.id.tvVideoQuality);
        ivUp = rootView.findViewById(R.id.ivUp);
        ivLeft = rootView.findViewById(R.id.ivLeft);
        ivDown = rootView.findViewById(R.id.ivDown);
        ivRight = rootView.findViewById(R.id.ivRight);
        llProgressBar = rootView.findViewById(R.id.llProgressBar);
        progressBar = rootView.findViewById(R.id.progressBar);
        tvProgress = rootView.findViewById(R.id.tvProgress);
        ivPlay = rootView.findViewById(R.id.ivPlay);

        ivBack.setOnClickListener(v -> {
            hideVideoQualityPop();
            getActivity().finish();
        });
        ivUp.setOnClickListener(v -> {
            hideVideoQualityPop();
            NativeCaller.PPPPPTZControl(ipAddress.getDeviceId(), ContentCommon.CMD_PTZ_UP);
        });
        ivDown.setOnClickListener(v -> {
            hideVideoQualityPop();
            NativeCaller.PPPPPTZControl(ipAddress.getDeviceId(), ContentCommon.CMD_PTZ_DOWN);
        });
        ivLeft.setOnClickListener(v -> {
            hideVideoQualityPop();
            NativeCaller.PPPPPTZControl(ipAddress.getDeviceId(), ContentCommon.CMD_PTZ_LEFT);
        });
        ivRight.setOnClickListener(v -> {
            hideVideoQualityPop();
            NativeCaller.PPPPPTZControl(ipAddress.getDeviceId(), ContentCommon.CMD_PTZ_RIGHT);
        });
        tvVideoQuality.setOnClickListener(v -> {
            showVideoQualityPop();
        });

        audioBuffer = new CustomBuffer();
        audioPlayer = new AudioPlayer(audioBuffer);
        ivAudio.setOnClickListener(v -> {
            hideVideoQualityPop();
            if (isAudioStart) {
                isAudioStart = false;
                ivAudio.setImageResource(R.drawable.ptz_audio_off);
                audioPlayer.AudioPlayStop();
                audioBuffer.ClearAll();
                NativeCaller.PPPPStopAudio(ipAddress.getDeviceId());
            } else {
                isAudioStart = true;
                ivAudio.setImageResource(R.drawable.ptz_audio_on);
                audioBuffer.ClearAll();
                audioPlayer.AudioPlayStart();
                NativeCaller.PPPPStartAudio(ipAddress.getDeviceId());
            }
        });

        customAudioRecorder = new CustomAudioRecorder((data, len) -> {
            if (isTalkStart && len > 0) {
                NativeCaller.PPPPTalkAudioData(ipAddress.getDeviceId(), data, len);
            }
        });

        ivTalk.setOnClickListener(v -> {
            hideVideoQualityPop();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
                return;
            }

            if (isTalkStart) {
                isTalkStart = false;
                ivTalk.setImageResource(R.drawable.ptz_microphone_off);
                customAudioRecorder.StopRecord();
                NativeCaller.PPPPStopTalk(ipAddress.getDeviceId());
            } else {
                isTalkStart = true;
                ivTalk.setImageResource(R.drawable.ptz_microphone_on);
                customAudioRecorder.StartRecord();
                NativeCaller.PPPPStartTalk(ipAddress.getDeviceId());
            }
        });
        ivPlay.setOnClickListener(v -> {
            hideVideoQualityPop();
            connectCamera(ipAddress.getDeviceId(), ipAddress.getDeviceUserName(), ipAddress.getDevicePassword());
        });
        myRender = new MyRender(glSurfaceView);
        glSurfaceView.setRenderer(myRender);
        glSurfaceView.setOnClickListener(v -> {
            hideVideoQualityPop();
            disconnectCamera(ipAddress.getDeviceId(), null);
        });
        glSurfaceView.setOnLongClickListener(v -> {
            if (getActivity() instanceof VideoActivity) {
                return false;
            }
            disconnectCamera(ipAddress.getDeviceId(), null);
            Intent intent = new Intent(getActivity(), VideoActivity.class);
            intent.putExtra("ipAddress", getArguments().getString("ipAddress"));
            intent.putExtra("isShowControlBtn", getArguments().getBoolean("isShowControlBtn"));
            startActivity(intent);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        LogUtils.INSTANCE.d("VideoFragment onResume " + ipAddress);

        if (ipAddress == null) return;

        if (getActivity() instanceof VideoActivity) {
            connectCamera(ipAddress.getDeviceId(), ipAddress.getDeviceUserName(), ipAddress.getDevicePassword());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LogUtils.INSTANCE.d("VideoFragment onPause " + ipAddress);

        if (ipAddress == null) return;

        disconnectCamera(ipAddress.getDeviceId(), null);
    }

    private void showVideoQualityPop() {
        hideVideoQualityPop();

        View videoQualityPopView = LayoutInflater.from(getActivity()).inflate(R.layout.pop_video_quality, null, false);

        videoQualityPopView.findViewById(R.id.tvHigh).setOnClickListener(v1 -> {
            videoQualityPop.dismiss();
            if (tvVideoQuality.getText().equals(getString(R.string.high))) {
                return;
            }
            tvVideoQuality.setText(R.string.high);
            NativeCaller.PPPPCameraControl(ipAddress.getDeviceId(), 16, 1);
        });
        videoQualityPopView.findViewById(R.id.tvMiddle).setOnClickListener(v12 -> {
            videoQualityPop.dismiss();
            if (tvVideoQuality.getText().equals(getString(R.string.middle))) {
                return;
            }
            tvVideoQuality.setText(R.string.middle);
            NativeCaller.PPPPCameraControl(ipAddress.getDeviceId(), 16, 2);
        });
        videoQualityPopView.findViewById(R.id.tvLow).setOnClickListener(v13 -> {
            videoQualityPop.dismiss();
            if (tvVideoQuality.getText().equals(getString(R.string.low))) {
                return;
            }
            tvVideoQuality.setText(R.string.low);
            NativeCaller.PPPPCameraControl(ipAddress.getDeviceId(), 16, 4);
        });

        videoQualityPop = new PopupWindow(videoQualityPopView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoQualityPop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        videoQualityPop.showAsDropDown(tvVideoQuality);
    }

    private void hideVideoQualityPop() {
        if (videoQualityPop != null && videoQualityPop.isShowing()) {
            videoQualityPop.dismiss();
            videoQualityPop = null;
        }
    }

    private void showTips(String message, boolean loadingProgress) {
        ThreadUtils.mainThread().execute(() -> {
            llProgressBar.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            tvProgress.setText(message);
        });
    }

    private void hideTips() {
        ThreadUtils.mainThread().execute(() -> llProgressBar.setVisibility(View.GONE));
    }

    private void connectCamera(String deviceId, String userName, String password) {
        if (isConnecting) return;

        isConnecting = true;

        isConnectingTimeout = true;

        ivPlay.setVisibility(View.GONE);

        showTips(getString(R.string.connecting), true);

        if (isShowControlBtn) {
            rlControl.setVisibility(View.VISIBLE);
        }

        BridgeService.setIpcamClientInterface(new BridgeService.IpcamClientInterface() {
            @Override
            public void BSMsgNotifyData(String did, int type, int param) {
                if (type == ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS
                        || type == ContentCommon.PPPP_MSG_VSNET_NOTIFY_TYPE_VUIDSTATUS) {
                    switch (param) {
                        case ContentCommon.PPPP_STATUS_ON_LINE:
                            isConnectingTimeout = false;

                            hideTips();

                            NativeCaller.StartPPPPLivestream(ipAddress.getDeviceId(), 10, 1); //确保不能重复start

                            BridgeService.setPlayInterface(new BridgeService.PlayInterface() {
                                @Override
                                public void callBackCameraParamNotify(String did, int resolution, int brightness, int contrast, int hue, int saturation, int flip, int mode) {

                                }

                                @Override
                                public void callBackVideoData(byte[] videobuf, int h264Data, int len, int width, int height) {
                                    if (h264Data == 1) {
                                        // H264
                                        myRender.writeSample(videobuf, width, height);
                                    } else {
                                        // MJPEG
                                    }
                                }

                                @Override
                                public void callBackMessageNotify(String did, int msgType, int param) {

                                }

                                @Override
                                public void callBackAudioData(byte[] pcm, int len) {
                                    if (!audioPlayer.isAudioPlaying()) {
                                        return;
                                    }
                                    CustomBufferHead head = new CustomBufferHead();
                                    CustomBufferData data = new CustomBufferData();
                                    head.length = len;
                                    head.startcode = 0xff00ff;
                                    data.head = head;
                                    data.data = pcm;
                                    audioBuffer.addData(data);
                                }

                                @Override
                                public void callBackH264Data(byte[] h264, int type, int size) {

                                }
                            });
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECTING:
                            showTips(getString(R.string.connecting), true);
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_FAILED:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        case ContentCommon.PPPP_STATUS_DISCONNECT:
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.disconnect));
                            break;
                        case ContentCommon.PPPP_STATUS_INITIALING:
                            showTips(getString(R.string.connecting), false);
                            break;
                        case ContentCommon.PPPP_STATUS_INVALID_ID:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_ERRER:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        case ContentCommon.PPPP_STATUS_INVALID_VUID:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        case ContentCommon.PPPP_STATUS_ALLOT_VUID:
                            showTips(getString(R.string.connect_fail), false);
                            disconnectCamera(ipAddress.getDeviceId(), getString(R.string.connect_fail));
                            break;
                        default:
                            showTips(getString(R.string.connecting), true);
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

        ThreadUtils.io().execute(() -> {
            NativeCaller.Init();

            if (VuidUtils.isVuid(deviceId)) {//vuid
                int status = NativeCaller.StartVUID("0", password, 1, "", "", 0, deviceId, 0);
                if (status == -2) {
                    disconnectCamera(deviceId, getString(R.string.connect_fail));
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
        });

        glSurfaceView.postDelayed(() -> {
            if (isConnectingTimeout) {//连接超时处理
                disconnectCamera(deviceId, getString(R.string.connect_fail));
            }
        }, 10000);
    }

    private void disconnectCamera(String deviceId, String message) {
        if (!isConnecting) return;

        isConnectingTimeout = false;

        isConnecting = false;

        ThreadUtils.mainThread().execute(() -> {
            if (TextUtils.isEmpty(message)) {
                ivPlay.setVisibility(View.VISIBLE);
                hideTips();
            } else {
                showTips(message, false);
            }

            if (isShowControlBtn) {
                rlControl.setVisibility(View.GONE);
            }
        });

        ThreadUtils.network().execute(() -> {
            NativeCaller.StopPPPPLivestream(deviceId);
            NativeCaller.StopPPPP(deviceId);
        });
    }

}
