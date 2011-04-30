package com.monitorend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import android.util.Log;

class ReceiveNaluThread extends Thread {
	
	private boolean mIsFinished;

//	private MulticastSocket mClientDatagram = null;
	private DatagramSocket mClientDatagram = null;
	
	String tag = "ReceiveNalu";
	
	private DatagramPacketQueue mDatagramPacketQueue;

	public ReceiveNaluThread(/*String multicastAddr, */DatagramPacketQueue datagramPacketQueue) {

		mDatagramPacketQueue = datagramPacketQueue;
		mIsFinished = false;
		
//		/**
//		 * use multicast to receive data
//		 */
//		try {
//			mClientDatagram = new MulticastSocket(ClientConfig.PORT_MONITOR_END_RECEIVE_RTP);
//			InetAddress multicastGroup = InetAddress.getByName(multicastAddr);
//			Log.d(tag, "multiAddr: " + multicastAddr);
//			mClientDatagram.joinGroup(multicastGroup);
//		} catch (SocketException e) {
//			Log.d(tag, "SocketException in RTPClientThread");
//			e.printStackTrace();
//		} catch (IOException e) {
//			Log.d("RTP", "IOException in RTPClientThread");
//			e.printStackTrace();
//		}
		
		/**
		 * use UDP to receive data
		 */
		try {
			mClientDatagram = new DatagramSocket(10000);
		} catch (SocketException e) {
			
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		while (!mIsFinished) {

			// to receive the RTP packet
			byte[] rtpPacket = new byte[ClientConfig.RTP_PACKET_MAX_SIZE];
			DatagramPacket rtpDatagram = new DatagramPacket(rtpPacket,
					rtpPacket.length);
			
//			Log.d(tag, "start RTP receiving");
			try {
				mClientDatagram.receive(rtpDatagram);
			} catch (IOException e) {

				Log.d("RTP", e.getMessage());
				e.printStackTrace();
			}
//			Log.d(tag, "one packet received!!");
			
			mDatagramPacketQueue.add(rtpDatagram);

		}

	}
	
	// force finishing the thread
	public void setFinished() {
		mIsFinished = true;
		if( null != mClientDatagram ) {
			mClientDatagram.close();
		}
	}
}