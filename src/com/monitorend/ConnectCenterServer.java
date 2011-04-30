package com.monitorend;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class ConnectCenterServerThread extends Thread {
	
	protected Socket mSocket;
	private Handler mHandler;
	private String mCenterServerIpAddress;
	
	public ConnectCenterServerThread() {
		
	}
	
	public ConnectCenterServerThread(String centerServerIpAddress) {
		mCenterServerIpAddress = centerServerIpAddress;
		
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
			
		} catch (UnknownHostException e) {
			Log.d("Conn", "UnknownHostException in ConnectCenterServer");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ConnectCenterServer");
			e.printStackTrace();
		}
	}
	
	public ConnectCenterServerThread(String centerServerIpAddress, Handler handler) {
		
		mCenterServerIpAddress = centerServerIpAddress;
		mHandler = handler;
		
		try {
			
			showResponse("mTextView2", "connecting to center server");
			
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
	}
	
	protected void showResponse(String sender, String data) {
		Bundle bundle = new Bundle();
		
		bundle.putCharSequence("Sender", sender);
		bundle.putString("Msg", data);
		
		Message msg = new Message();
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}


	@Override
	public void run(){
		
	}
}
