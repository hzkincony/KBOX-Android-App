package com.ricky.HardDecode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecodeHevcFrame {
    private MediaCodec _mediaCodec = null;
    private static final String log_tag = "DecodeHevcFrame";
    public int _FrameWidth = 0;
    public int _FrameHeight = 0;
    private boolean _IsRun = false;

    //private      boolean _bDiscard    = false;

    public Boolean initCode() {
        if (Build.VERSION.SDK_INT < 23) {
            //android 6.0以下就不要用硬解码接口了
            return false;
        }

        try {
            int numCodecs = MediaCodecList.getCodecCount();
            int googleSdec = 0;
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                String[] types = codecInfo.getSupportedTypes();
                for (int j = 0; j < types.length; j++) {
                    if (types[j].equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                        if (codecInfo.isEncoder() == false) {
                            if (codecInfo.getName().equals("OMX.google.hevc.decoder")) {
                                googleSdec = 1;
                                // _mediaCodec = MediaCodec.createByCodecName("OMX.google.hevc.decoder");
                                //  Log.i(log_tag,"OMX.google.hevc.decoder");
                            } else if (codecInfo.getName().equals("OMX.qcom.video.decoder.hevc")) {
                                if (Build.VERSION.SDK_INT > 28) {
                                    _mediaCodec = MediaCodec.createByCodecName("OMX.qcom.video.decoder.hevc");
                                    Log.i(log_tag, codecInfo.getName());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }

                if (_mediaCodec != null) {
                    break;
                }
            }

            if (_mediaCodec == null && googleSdec == 1) {
                _mediaCodec = MediaCodec.createByCodecName("OMX.google.hevc.decoder");
                Log.i(log_tag, "OMX.google.hevc.decoder");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_mediaCodec == null) {
            return false;
        } else {
            return true;
        }
    }

    public void release() {
        if (_mediaCodec != null) {
            _mediaCodec.stop();
            _mediaCodec.release();
            _mediaCodec = null;
        }
    }

    public byte[] CallBack_H265FrameData(byte[] h265Data, int h265DataLen, int IsIDR, int videoWidth, int videoHeight) {
        if (_mediaCodec == null)
            return null;

        if (IsIDR == 1) {
            if (_FrameWidth == videoWidth && _FrameHeight == videoHeight) {
                return null;
            }
            _IsRun = false;
            _mediaCodec.stop();
//            if(_FrameWidth > 0) {
//                _bDiscard = true;
//            }

            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, videoWidth, videoHeight);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(h265Data));
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
            try {
                _mediaCodec.configure(format, null, null, 0);
            } catch (IllegalArgumentException ei) {
                //surface已经释放（或非法），或format不可接受（e.g. 丢失了强制秘钥），或flags设置不合适(e.g. encoder时忽略了CONFIGURE_FLAG_ENCODE)
                Log.e(log_tag, "mMC configure IllegalArgumentException");
            } catch (IllegalStateException e) {
                //不在未初始化状态
                Log.e(log_tag, "mMC configure IllegalStateException");
            } catch (MediaCodec.CryptoException e) {
                //DRM错误
                Log.e(log_tag, "mMC configure MediaCodec.CryptoException");
            }

            _IsRun = true;
            _mediaCodec.start();
            Log.d(log_tag, "new sps pps " + String.format("%d %d", videoWidth, videoHeight));
            return null;
        } else {
            if (_IsRun == false)
                return null;

            byte[] decData = new byte[h265DataLen];
            System.arraycopy(h265Data, 0, decData, 0, h265DataLen);
            return Decoding(decData, h265DataLen, videoWidth, videoHeight);
        }
    }


    private byte[] Decoding(byte[] h265Data, int h265DataLen, int nWidth, int nHeight) {
        ByteBuffer[] inputBuffers = _mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = _mediaCodec.getOutputBuffers();

        int inIndex = 0;
        while ((inIndex = _mediaCodec.dequeueInputBuffer(1)) < 0) ;

        if (inIndex >= 0) {
            ByteBuffer buffer = inputBuffers[inIndex];//取出解码器输入队列缓冲区最后一个buffer
            buffer.clear();
            buffer.put(h265Data);//向最后一个缓冲区inIndex中放入一帧数据。
            _mediaCodec.queueInputBuffer(inIndex, 0, h265DataLen, 0, 0);//


            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int outIndex = _mediaCodec.dequeueOutputBuffer(info, 500000);//出队列缓冲区的信息,1秒超时

            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(log_tag, "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = _mediaCodec.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    //Log.d(log_tag, "New format " + _mediaCodec.getOutputFormat());
                    MediaFormat format = _mediaCodec.getOutputFormat();
                    _FrameWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                    _FrameHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    if (_FrameWidth != nWidth || _FrameHeight != nHeight) {
                        Log.e(log_tag, "New format " + _mediaCodec.getOutputFormat());
                    } else {
                        Log.i(log_tag, "New format " + _mediaCodec.getOutputFormat());
                    }
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.e(log_tag, "dequeueOutputBuffer timed out!");
                    break;
                default://outIndex>0
                    ByteBuffer outbuffer = outputBuffers[outIndex];
                    //Log.e("CallBack_HardFrameData", "We can't use this buffer but render it due to the API limit, " + outbuffer + ", out Size=" + info.size);
                    byte[] outData = new byte[info.size];
                    outbuffer.get(outData);
                    _mediaCodec.releaseOutputBuffer(outIndex, false);
                    return outData;
            }
        }
        //Log.d(log_tag,"Decoding end:" + String.format("%d",aaaaa));
        return null;
    }


    /*private void Decoding(byte[] h265Data, int h265DataLen)
    {
        aaaaa = aaaaa +1;
        Log.d(log_tag,"Decoding bet:" + String.format("%d",aaaaa));
        int inputbufferindex = 0;
        try {
            while ((inputbufferindex = _mediaCodec.dequeueInputBuffer(1)) < 0);
        }
        catch (IllegalStateException ex) {
            //如果codec不在Executing状态，或者codec处于异步模式。
            Log.e(log_tag, "dequeueInputBuffer throw IllegalStateException");
            return;
        }

        if (inputbufferindex >= 0) {
            ByteBuffer buffer;
            //取出解码器输入队列缓冲区最后一个buffer
            if (Build.VERSION.SDK_INT >= 21) {
                buffer = _mediaCodec.getInputBuffer(inputbufferindex);
            } else {
                buffer = _mediaCodec.getInputBuffers()[inputbufferindex];
            }

            buffer.clear();
            if (buffer != null) {
                buffer.put(h265Data, 0, h265DataLen);//向最后一个缓冲区inputbufferindex中放入一帧数据。
                try {
                    _mediaCodec.queueInputBuffer(inputbufferindex, 0, h265DataLen, 0, 0);
                }
                catch (IllegalStateException e) {
                    //如果没有在Executing状态
                    Log.e(log_tag,"mMC queueInputBuffer IllegalStateException");
                }
                catch (MediaCodec.CryptoException e) {
                    //如果cryto对象已经在configure（MediaFormat, Surface, MediaCryto, int）中指定。
                    Log.e(log_tag,"mMC queueInputBuffer MediaCodec.CryptoException");
                }
            }//end if (buffer != null)


            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] outputBuffers = _mediaCodec.getOutputBuffers();
            int outputBufferIndex = _mediaCodec.dequeueOutputBuffer(bufferInfo, 1000000);//出队列缓冲区的信息,1秒超时

            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(log_tag,"INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = _mediaCodec.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(log_tag,"New format "+ _mediaCodec.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(log_tag,"dequeueOutputBuffer timed out!");
                    break;
                default://outIndex>0
                    ByteBuffer outbuffer ;
                    if (Build.VERSION.SDK_INT >= 21) {
                        outbuffer = _mediaCodec.getOutputBuffer(outputBufferIndex);
                    } else {
                        outbuffer = outputBuffers[outputBufferIndex];
                    }
                    Log.d(log_tag,"We can't use this buffer but render it due to the API limit, "
                                    + outbuffer + ", out Size=" + bufferInfo.size + ",Thread=" + Thread.currentThread().getId());


                    byte[] outData = new byte[bufferInfo.size];
                   // outbuffer.get(outData);
                    //saveFileToYUV_JPG_Bitmap(outData);

                    _mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    break;
            }

        }//end if (inputbufferindex >= 0)
        Log.d(log_tag,"Decoding end:" + String.format("%d",aaaaa));
    }*/

}
