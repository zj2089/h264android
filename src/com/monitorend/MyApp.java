package com.monitorend;

import java.net.Socket;

import android.app.Application;

public class MyApp extends Application {

	private Socket mSocket;
	private String mCenterServerIp;
	private String mCurCaptureEnd;
	
	public void setSocket(Socket socket) {
		mSocket = socket;
	}
	
	public Socket getSocket() {
		return mSocket;
	}
	
	public void setCenterServerIp(String ip) {
		mCenterServerIp = ip;
	}
	
	public String getCenterServerIp() {
		return mCenterServerIp;
	}
	
	public void setCurCaptureEnd(String captureEnd) {
		mCurCaptureEnd = captureEnd;
	}
	
	public String getCurCaputureEnd() {
		return mCurCaptureEnd;
	}
	
}
