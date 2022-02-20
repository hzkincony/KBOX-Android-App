package com.ipcamera.demo.utils;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioPlayer {

    // private final static String LOG_TAG = "AudioPlayer" ;

    CustomBuffer audioBuffer = null;
    private boolean bAudioPlaying = false;
    private Thread audioThread = null;
    private AudioTrack m_AudioTrack = null;

    public AudioPlayer(CustomBuffer buffer) {
        // TODO Auto-generated constructor stub
        audioBuffer = buffer;
    }

    public boolean isAudioPlaying() {
        return bAudioPlaying;
    }

    public boolean AudioPlayStart() {
        synchronized (this) {
            if (bAudioPlaying) {
                return true;
            }
            bAudioPlaying = true;
            audioThread = new Thread(new AudioPlayThread());
            audioThread.start();
        }
        return true;
    }

    public void AudioPlayStop() {
        synchronized (this) {
            if (!bAudioPlaying || audioThread == null) {
                return;
            }

            bAudioPlaying = false;
            try {
                audioThread.join();
            } catch (Exception e) {
                // TODO: handle exception
            }
            audioThread = null;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean initAudioDev() {

        int channelConfig;
        int audioFormat = 2;
        int mMinBufSize = 0;

        // channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        mMinBufSize = AudioTrack.getMinBufferSize(8000, channelConfig,
                audioFormat);
        System.out.println("--audio, mMinBufSize=" + mMinBufSize);

        if (mMinBufSize == AudioTrack.ERROR_BAD_VALUE
                || mMinBufSize == AudioTrack.ERROR) {
            System.out.println("初始化失败");
            return false;

        }

        try {
            m_AudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                    channelConfig, audioFormat, mMinBufSize * 2,
                    AudioTrack.MODE_STREAM);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            return false;
        }

        m_AudioTrack.play();
        return true;
    }

    class AudioPlayThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (!initAudioDev()) {
                return;
            }

            while (bAudioPlaying) {
                CustomBufferData data = audioBuffer.RemoveData();
                if (data == null) {
                    try {
                        Thread.sleep(10);
                        continue;
                    } catch (Exception e) {
                        // TODO: handle exception
                        m_AudioTrack.stop();
                        return;
                    }
                }
                // Log.d(LOG_TAG, "length:" + data.head.length);
                m_AudioTrack.write(data.data, 0, data.head.length);
                // Log.d(LOG_TAG, "nRet:" + nRet);
            }

            m_AudioTrack.stop();
            m_AudioTrack.release();
            m_AudioTrack = null;
        }

    }
}