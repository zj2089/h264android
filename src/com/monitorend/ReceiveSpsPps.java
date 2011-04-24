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
				byte buf[] = new byte[ClientConfig.SPS_PPS_BUFFER_SIZE];
				byte btNalLen[] = new byte[2]; 

				mInputStream.read(btNalLen, 0, 2);
				
				int highBit =  btNalLen[0]<0 ? 256 + btNalLen[0] : btNalLen[0];
				int lowBit = btNalLen[1]<0 ? 256 + btNalLen[1] : btNalLen[1];
				int nalLen = (highBit<<8) + lowBit;

				Log.d("NalLen", "" + nalLen);

				int bufLen = mInputStream.read(buf, 0, nalLen);
				
				if( bufLen > 0 ) {
					mRecvPacketNum++;
					Log.d("pIC", "TCP recv len: " + bufLen);
				}
//				
//				if( mRecvPacketNum % 20 == 0 )
//					continue;

				mView.decodeNalAndDisplay(buf, bufLen);
					
				
				// received the PPS and SPS
				if( 2 == mRecvPacketNum ) {
					
					Log.d("pIC", "received the PPS and SPS");
			    	Log.d("pIC", "start the RTP Thread");
			    	
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

