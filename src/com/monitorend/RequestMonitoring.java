package com.monitorend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;


import android.content.Intent;
import android.os.Handler;
import android.util.Log;

class RequestMonitoringThread extends ConnectCenterServerThread {
	
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private List<String> mAvailableCaptureEndList;
	private String mMonitorEndChoice;
	private MainAct mActivity;
	private Intent mIntent;
	
	public RequestMonitoringThread(
			List<String> availableCaptureEndList, 
			MainAct activity,
			String centerServerIpAddress,
			Handler handler,
			Intent intent) {
		
		super(centerServerIpAddress, handler);
		
		mAvailableCaptureEndList = availableCaptureEndList;
		mAvailableCaptureEndList.clear();
		
		mMonitorEndChoice = null;
		mActivity = activity;
		mIntent = intent;
		
		try {
			mInputStream = mSocket.getInputStream();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		try {
			mOutputStream = mSocket.getOutputStream();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
	}
	
	public synchronized void setMonitorEndChoice(String str) {
		
		mMonitorEndChoice = str;
		notifyAll();
	}
	
	public synchronized void getMonitorEndChoice() {
		
//		while( null == mMonitorEndChoice ) {
			try {
				wait();
			} catch (InterruptedException e) {
				Log.d("Conn", "InterruptedException in ConnectCenterServer");
				e.printStackTrace();
			}
//		}
		
	}

	@Override
	public void run() {
		
		/*
		 *  tell my intent to the center server: join monitoring
		 */
		byte[] myIntent = new byte[] {'J', 'O', 'I', 'N'};
		try {
			mOutputStream.write(myIntent);
		} catch (IOException e1) {
			Log.d("Conn", "IOException in RequestMonitoring");
			e1.printStackTrace();
		}
		
		int highbyte = 0;
		int lowbyte = 0;
		
		try {
			highbyte = mInputStream.read();
			lowbyte = mInputStream.read();
		} catch (IOException e) {
			Log.d("Conn", "IOException in RequestMonitoring");
			e.printStackTrace();
		}
		
		showResponse("mTextView2", "getting Capture end information");
		Log.d("Conn", " " + highbyte + " " + lowbyte);
		
		int captureEndInfolen = (highbyte << 8) + lowbyte;
		
		byte[] captureEndInfo = new byte[captureEndInfolen];
		
		try {
			mInputStream.read(captureEndInfo, 0, captureEndInfolen);
		} catch (IOException e) {
			Log.d("Conn", "IOException in RequestMonitoring");
			e.printStackTrace();
		}
		
		/*
		 * process the available capture end name and add them to list
		 */
		ByteArrayInputStream inputStream = new ByteArrayInputStream(captureEndInfo);
		while(true) {
			int captureEndNameLen = inputStream.read();
			if(captureEndNameLen < 0)
				break;
			
			byte[] captureEndName = new byte[captureEndNameLen];
			inputStream.read(captureEndName, 0, captureEndNameLen);
			String strCapName = null;
			try {
				strCapName = new String(captureEndName, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				Log.d("Debug", "UnsupportedEncodingException in RequestMonitoring");
				e.printStackTrace();
			}
			if(strCapName.length()>0)
				mAvailableCaptureEndList.add(strCapName);
		}
		
		Log.d("Debug", "available cap num: " + mAvailableCaptureEndList.size());
		
		showResponse("Button", "update");
		
		getMonitorEndChoice();
		
		if( null == mMonitorEndChoice ) {
			try {
				mSocket.close();
			} catch (IOException e) {
				Log.d("Debug", "IOException in RequestMonitoring");
				e.printStackTrace();
			}
			return;
		}
		
		/*
		 * send the choice of monitor end
		 */
		try {
			byte[] monitorEndChoice = mMonitorEndChoice.getBytes("UTF-16LE");
			mOutputStream.write(monitorEndChoice.length);
			mOutputStream.write(monitorEndChoice);
		} catch (IOException e) {
			Log.d("Conn", "IOException in RequestMonitoring");
			e.printStackTrace();
		}
		
//		/*
//		 * get the multicast address
//		 */
//		String multicastAddress = null;
//		byte[] multicastAddr = null;
//		try {
//			int multicastAddrLen = mInputStream.read();
//			multicastAddr = new byte[multicastAddrLen];
//			mInputStream.read(multicastAddr, 0, multicastAddrLen);
//			
//		} catch (IOException e) {
//			Log.d("Conn", "IOException in ConnectCenterServer");
//			e.printStackTrace();
//		}
//		
//		try {
//			multicastAddress = new String(multicastAddr, "ISO-8859-1");
//		} catch (UnsupportedEncodingException e) {
//			Log.d("Conn", "UnsupportedEncodingException in ConnectCenterServer");
//			e.printStackTrace();
//		}
		
		MyApp mMyApp = (MyApp) mActivity.getApplicationContext();	
		mMyApp.setSocket(mSocket);
//		mIntent.putExtra("multicastAddress", multicastAddress);
		
		mActivity.startActivityForResult(mIntent, MainAct.REQ_CODE_FINISHED);
	}
}
