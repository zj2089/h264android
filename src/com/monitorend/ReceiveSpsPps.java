package com.monitorend;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.util.Log;

class RecvSpsPpsThread extends Thread {
	
	private WVSSView mView;
	
	// The number of received packets
	public int mRecvPacketNum = 0;
	
	private ReceiveNaluThread mReceiveNaluThread;
	private Socket mSocket;

	private boolean mIsFinished;

	public RecvSpsPpsThread(WVSSView view, ReceiveNaluThread rtpClientThread, Socket socket) {

		mView = view;
		mReceiveNaluThread = rtpClientThread;
		mSocket = socket;
		mIsFinished = false;
	}

	@Override
	public void run() {

		while(!mIsFinished) {

			try {
				InputStream inputStream = mSocket.getInputStream();
				
				byte spsPpsBuf[] = new byte[ClientConfig.SPS_PPS_BUFFER_SIZE];
				int spsPpsLen = inputStream.read();
				
				if( spsPpsLen < 0 )
					break;

				inputStream.read(spsPpsBuf, 0, spsPpsLen);
				
				mRecvPacketNum++;

				mView.decodeNalAndDisplay(spsPpsBuf, spsPpsLen);
				
				// received the PPS and SPS
				if( 2 == mRecvPacketNum ) {
					
					Log.d("pIC", "received the PPS and SPS");
			    	Log.d("pIC", "start receiving NALU");
			    	
			    	// start one thread to process RTP packet
			    	mReceiveNaluThread.start();
			    	
					break;
				}

				Log.d("pIC", "end process input");

			}
			catch(IOException e) {
				e.printStackTrace();
				Log.d("nullpoint", "inputstream is null");
			}
		}
		
		try {
			mSocket.close();
		} catch (IOException e) {
			Log.d("Debug", "IOException in ReceiveSpsPps");
			e.printStackTrace();
		}
	}
	
	public void setFinished() {
		mIsFinished = true;
		try {
			if( null != mSocket )
				mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

