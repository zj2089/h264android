package com.monitorend;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

class RecvSpsPpsThread extends Thread {
	
	private WVSSView mView;
	
	// use to receive SPS & PPS
	private Socket mSocket;
	
	// The number of received packets
	public int mRecvPacketNum = 0;
	
	private RtpClientThread mRtpClientThread;
	private InputStream mInputStream;

	private boolean mIsFinish = false;

	public RecvSpsPpsThread(WVSSView view, RtpClientThread rtpClientThread) {

		mView = view;
		mRtpClientThread = rtpClientThread;

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
			Log.d("Conn", "UnknownHostException in ReceiveSpsPps");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ReceiveSpsPps");
			e.printStackTrace();
		}
		
		try {
			mInputStream = mSocket.getInputStream();
		} catch (IOException e) {
			Log.d("Conn", "IOException in ReceiveSpsPps");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while(!mIsFinish) {

			try {
				byte buf[] = new byte[ClientConfig.CONFIG_BUFFER_SIZE];
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
			    	mRtpClientThread.start();
			    	
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

