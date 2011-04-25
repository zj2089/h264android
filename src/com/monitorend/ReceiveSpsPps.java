package com.monitorend;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

class RecvSpsPpsThread extends Thread {
	
	private WVSSView mView;
	
	// The number of received packets
	public int mRecvPacketNum = 0;
	
	private ReceiveNaluThread mReceiveNaluThread;
	private InputStream mInputStream;

	private boolean mIsFinish = false;

	public RecvSpsPpsThread(WVSSView view, ReceiveNaluThread rtpClientThread, InputStream inputStream) {

		mView = view;
		mReceiveNaluThread = rtpClientThread;
		mInputStream = inputStream;
	}

	@Override
	public void run() {

		while(!mIsFinish) {

			try {
				byte spsPpsBuf[] = new byte[ClientConfig.SPS_PPS_BUFFER_SIZE];
				int spsPpsLen = mInputStream.read();

				mInputStream.read(spsPpsBuf, 0, spsPpsLen);
				
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
	}
	
	public void setIsFinish(boolean isFinish) {
		this.mIsFinish = isFinish;
	}

}

