package com.ipcamera.demo.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class CustomAudioRecorder {
	
	private AudioRecordResult audioResult = null;
	private Thread recordThread = null;
	private boolean bRecordThreadRuning = false;
	private int m_in_buf_size = 0;
	private AudioRecord m_in_rec = null;	
	private byte[] m_in_bytes = null;

	public interface AudioRecordResult{
		abstract public void AudioRecordData(byte[] data, int len);
	}
	
	public CustomAudioRecorder(AudioRecordResult result){
		audioResult = result;		
		initRecorder();
	}
	
	public void StartRecord(){
		synchronized (this) {
			if (bRecordThreadRuning) {
				return ;
			}
			
			bRecordThreadRuning = true;
			recordThread = new Thread(new RecordThread());
			recordThread.start();
		}
	}
	
	public void StopRecord(){
		synchronized (this) {
			if (!bRecordThreadRuning) {
				return;
			}
			
			bRecordThreadRuning = false;
			try {
				recordThread.join();
			} catch (Exception e) {
				// TODO: handle exception
			}			
		}
	}
	public void releaseRecord(){
		Log.d("tag","releaseRecord");
		m_in_rec.release();
		m_in_rec=null;
	}
	class RecordThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
//			if (!initRecorder()) {
//				Log.i("info", "!!!!!!!!!!!!!!!!!");
//				return;
//			}
			Log.i("info", "startRecording-------------");
			m_in_rec.startRecording() ;
			while(bRecordThreadRuning){
				int nRet = m_in_rec.read(m_in_bytes, 0, m_in_buf_size) ; 
				Log.i("info", "1111");
				if (nRet == 0) {
					Log.i("info", "2222");
					return;
				}
				
				if (audioResult != null) {
					Log.i("info", "3333");
					audioResult.AudioRecordData(m_in_bytes, nRet);
				}
			}		
			m_in_rec.stop();
		}
		
	}

	public boolean initRecorder() {
		// TODO Auto-generated method stub
		Log.i("info", "initRecorder()");
		m_in_buf_size =  AudioRecord.getMinBufferSize(8000,  AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);  
		m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,  AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,  
		m_in_buf_size) ;
		if (m_in_rec == null) {
			Log.i("info", "444");
			return false;
		}
		m_in_bytes = new byte [m_in_buf_size] ;
		return true;
	}
	
	
}