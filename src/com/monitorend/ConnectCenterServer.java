package com.monitorend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class ConnectCenterServerThread extends Thread {
	
	private Socket mSocket;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private List<String> mAvailableCaptureEndList;
	private String mMonitorEndChoice;
	private MainAct mActivity;
	private String mCenterServerIpAddress;
	private Handler mHandler;
	private Intent mIntent;
	
	public ConnectCenterServerThread(
			List<String> availableCaptureEndList, 
			MainAct activity,
			String centerServerIpAddress,
			Handler handler,
			Intent intent) {
		
		mAvailableCaptureEndList = availableCaptureEndList;
		mAvailableCaptureEndList.clear();
		
		mMonitorEndChoice = null;
		mActivity = activity;
		mCenterServerIpAddress = centerServerIpAddress;
		mHandler = handler;
		mIntent = intent;
		
		showResponse("mTextView2", "connecting center server...");
		
		try {
			
			/*
			 * Creates a new streaming socket connected to the target host 
			 * specified by the parameters dstName and dstPort. 
			 */
			mSocket = new Socket(
					/*ClientConfig.CENTER_SERVER_IP_ADDRESS*/mCenterServerIpAddress,
					ClientConfig.CENTER_SERVER_LISTEN_MONITOR_END_PORT/*,
					InetAddress.getLocalHost(),
					ClientConfig.MONITOR_END_RECV_SPS_PORT*/
					);
			
			showResponse("mTextView2", "connected to center server");
			
		} catch (UnknownHostException e) {
			Log.d("Conn", "UnknownHostException in ConnectCenterServer");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}

		Log.d("Conn", "socket create successfully!");
		
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
		
		while( null == mMonitorEndChoice ) {
			try {
				wait();
			} catch (InterruptedException e) {
				Log.d("Conn", "InterruptedException in ConnectCenterServer");
				e.printStackTrace();
			}
		}
		
	}
	
	private void showResponse(String sender, String data) {
		Bundle bundle = new Bundle();
		
		bundle.putCharSequence("Sender", sender);
		bundle.putString("Msg", data);
		
		Message msg = new Message();
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	@Override
	public void run() {
		
		int highbyte = 0;
		int lowbyte = 0;
		
		try {
			highbyte = mInputStream.read();
			lowbyte = mInputStream.read();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		showResponse("mTextView2", "getting Capture end information");
		Log.d("Conn", " " + highbyte + " " + lowbyte);
		
		int captureEndInfolen = (highbyte << 8) + lowbyte;
		
		byte[] captureEndInfo = new byte[captureEndInfolen];
		
		try {
			mInputStream.read(captureEndInfo, 0, captureEndInfolen);
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(captureEndInfo);
		while(true) {
			int captureEndNameLen = inputStream.read();
			if(captureEndNameLen < 0)
				break;
			
			byte[] captureEndName = new byte[captureEndNameLen];
			inputStream.read(captureEndName, 0, captureEndNameLen);
			String strCapName = null;
			try {
				strCapName = new String(captureEndName, "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
			mAvailableCaptureEndList.add(strCapName);
		}
		
		showResponse("Button", "update");
		
		getMonitorEndChoice();
		
		/*
		 * send the choice of monitor end
		 */
		try {
			byte[] monitorEndChoice = mMonitorEndChoice.getBytes("ISO-8859-1");
			mOutputStream.write(monitorEndChoice.length);
			mOutputStream.write(monitorEndChoice);
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		/*
		 * get the multicast address
		 */
		String multicastAddress = null;
		byte[] multicastAddr = null;
		try {
			int multicastAddrLen = mInputStream.read();
			multicastAddr = new byte[multicastAddrLen];
			mInputStream.read(multicastAddr, 0, multicastAddrLen);
			
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		try {
			multicastAddress = new String(multicastAddr, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			Log.d("Conn", "UnsupportedEncodingException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		MyApp mMyApp = (MyApp) mActivity.getApplicationContext();	
		mMyApp.setSocket(mSocket);
		mIntent.putExtra("multicastAddress", multicastAddress);
		
		mActivity.startActivity(mIntent);
	}
}
