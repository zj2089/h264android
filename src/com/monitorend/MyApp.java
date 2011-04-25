package com.monitorend;

import java.net.Socket;

import android.app.Application;

public class MyApp extends Application {

	private Socket mSocket;
	
	public void setSocket(Socket socket) {
		mSocket = socket;
	}
	
	public Socket getSocket() {
		return mSocket;
	}
}
