package com.monitorend;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.util.Log;

class QuitMonitoringThread extends ConnectCenterServerThread {
	
	private OutputStream mOutputStream;
	private String mCurCaptureEnd;
	
	public QuitMonitoringThread(
			String centerServerIpAddress,
			String curCaptureEnd) {
		
		super(centerServerIpAddress);
		
		mCurCaptureEnd = curCaptureEnd;
		
		try {
			mOutputStream = mSocket.getOutputStream();
		} catch (IOException e) {
			Log.d("Conn", "IOException in QuitMonitoring");
			e.printStackTrace();
		}
	}


	@Override
	public void run() {

		/*
		 *  tell my intent to the center server: quit monitoring
		 */
		byte[] myIntent = new byte[] {'Q', 'U', 'I', 'T'};
		try {
			mOutputStream.write(myIntent);
		} catch (IOException e1) {
			Log.d("Conn", "IOException in QuitMonitoring");
			e1.printStackTrace();
		}
		
		byte[] curCaptureEnd = null;
		try {
			curCaptureEnd = mCurCaptureEnd.getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e1) {
			Log.d("Debug", "UnsupportedEncodingException in QuitMonitoring");
			e1.printStackTrace();
		}
		try {
			mOutputStream.write(curCaptureEnd.length);
			mOutputStream.write(curCaptureEnd);
		} catch (IOException e1) {
			Log.d("Conn", "IOException in QuitMonitoring");
			e1.printStackTrace();
		}
		
		try {
			mSocket.close();
		} catch (IOException e) {
			Log.d("Conn", "IOException in QuitMonitoring");
			e.printStackTrace();
		}
	}
}
