package com.monitorend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

class SendCommandThread extends Thread {

	private Socket mSocket;
	private String mSender;
	private String mReceiver;
	private String mCenterServerIp;
	private String mCommandType;
	private String mArgument1;
	private String mArgument2;
	private String mArgument3;
	
	public SendCommandThread(
			String centerServerIp,
			String sender,
			String receiver,
			String commandType,
			String argument1,
			String argument2,
			String argument3) {
		
		mCenterServerIp = centerServerIp;
		mSender = sender;
		mReceiver = receiver;
		mCommandType = commandType;
		mArgument1 = argument1;
		mArgument2 = argument2;
		mArgument3 = argument3;
		
		// connect to center server
		try {
			mSocket = new Socket(mCenterServerIp, ClientConfig.PORT_MONITOR_END_SEND_COMMAND_TO);
		} catch (UnknownHostException e) {
			Log.d("Debug", "UnknownHostException in SendCommand");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Debug", "IOException in SendCommand");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		try {
			OutputStream outputStream = mSocket.getOutputStream();
			
			// send sender
			byte[] sender = mSender.getBytes("ISO-8859-1");
			outputStream.write(sender.length);
			outputStream.write(sender);
			
			// send receiver
			byte[] receiver = mReceiver.getBytes("UTF-16LE");
			outputStream.write(receiver.length);
			outputStream.write(receiver);
			
			// send command type
			byte[] commandtype = mCommandType.getBytes("ISO-8859-1");
			outputStream.write(commandtype.length);
			outputStream.write(commandtype);
			
			// send argument1
			byte[] argument1 = mArgument1.getBytes("ISO-8859-1");
			outputStream.write(argument1.length);
			outputStream.write(argument1);
			
			// send argument1
			byte[] argument2 = mArgument2.getBytes("ISO-8859-1");
			outputStream.write(argument2.length);
			outputStream.write(argument2);
			
			// send argument1
			byte[] argument3 = mArgument3.getBytes("ISO-8859-1");
			outputStream.write(argument3.length);
			outputStream.write(argument3);
			
		} catch (IOException e) {
			Log.d("Debug", "IOException in SendCommand");
			e.printStackTrace();
		} finally {
			
			try {
				mSocket.close();
			} catch (IOException e) {
				Log.d("Debug", "IOException in SendCommand");
				e.printStackTrace();
			}
		}
	}
}
