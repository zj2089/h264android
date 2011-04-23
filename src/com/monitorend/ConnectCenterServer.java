package com.monitorend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

class ConnectCenterServerThread extends Thread {
	
	private Socket mSocket;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private RtpClientThread mRtpClientThread;
	private WVSSView mView;
	
	public ConnectCenterServerThread(WVSSView view) {
		
		mView = view;
		
		try {
			
			/*
			 * Creates a new streaming socket connected to the target host 
			 * specified by the parameters dstName and dstPort. 
			 */
			mSocket = new Socket(
					ClientConfig.CENTER_SERVER_IP_ADDRESS,
					ClientConfig.CENTER_SERVER_PORT
					);
			
		} catch (UnknownHostException e) {
			Log.d("Conn", "UnknownHostException in ConnectCenterServer");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
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

	@Override
	public void run() {
		
		int hightbyte = 0;
		int lowbyte = 0;
		
		try {
			hightbyte = mInputStream.read();
			lowbyte = mInputStream.read();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		int captureEndInfolen = (hightbyte << 8) + lowbyte;
		
		byte[] captureEndInfo = new byte[65536];
		
		try {
			mInputStream.read(captureEndInfo, 0, captureEndInfolen);
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
		
		/*
		 * send the choice of monitor end
		 */
		String captureEndName = "testt";
		try {
			mOutputStream.write(5);
			mOutputStream.write(captureEndName.getBytes("ISO-8859-1"));
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
		
		mRtpClientThread = new RtpClientThread(mView, multicastAddress);
		
		new RecvSpsPpsThread(mView, mRtpClientThread).start();
		
	}
}
