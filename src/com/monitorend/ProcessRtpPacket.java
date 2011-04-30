package com.monitorend;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;

import java.util.Arrays;
import java.util.Comparator;

import android.util.Log;

class ProcessRtpPacketThread extends Thread {

	private boolean mIsFinished;
	WVSSView mView;

	// the RTP buffer
	RTPPacket[] mRtpBuffer;
	int mRtpBufferNum;
	int mBufferUsedPos;

	// the first FU of some NALU appears
	boolean firstFuFound = false;

	// the last FU of some NALU appears
	boolean lastFuFound = false;

	// one FU of some NALU appears
	boolean fuFound = false;

	// one NALU
	byte[] tmpHeader = new byte[5];

	// to record the time stamp of some FU
	int timestamp;

	// record the previous sequence number of FU
	int preNo;

	// to save the NAL buffer temporarily for one FU
	String tmpNalBuf;
	
	String tag_lostpacket = "lostpacket";
	String tag_IDR = "IDR";
	
	private DatagramPacketQueue mDatagramPacketQueue;
	
	
	private int toInt(byte b1, byte b2, byte b3, byte b4) {

		int i1 = b1 < 0 ? (256 + b1) : b1;
		int i2 = b2 < 0 ? (256 + b2) : b2;
		int i3 = b3 < 0 ? (256 + b3) : b3;
		int i4 = b4 < 0 ? (256 + b4) : b4;
		return (i1 << 24) + (i2 << 16) + (i3 << 8) + i4;
	}

	private int toInt(byte b1, byte b2) {

		int i1 = b1 < 0 ? (256 + b1) : b1;
		int i2 = b2 < 0 ? (256 + b2) : b2;
		return (i1 << 8) + i2;
	}
	
	private void AllocRtpBuffer() {

		mRtpBufferNum = ClientConfig.RTP_PACKET_BUFFER_NUM;

		mRtpBuffer = new RTPPacket[mRtpBufferNum];
		for (int i = 0; i < mRtpBufferNum; i++)
			mRtpBuffer[i] = new RTPPacket();
		mBufferUsedPos = 0;
	}
	
	private void fillRtpPacket(int pos, byte[] rtpPacket, int rtpPacketLen) {

		int tmp = toInt(rtpPacket[4], rtpPacket[5], rtpPacket[6], rtpPacket[7]);
		mRtpBuffer[pos].mTimestamp = tmp;
		mRtpBuffer[pos].mSeqNo = toInt(rtpPacket[2], rtpPacket[3]);
		mRtpBuffer[pos].mPacketType = ((rtpPacket[12] & 0x1f) == 28 ? RTPPacket.FUA
				: RTPPacket.SGN);

		mRtpBuffer[pos].mF = (rtpPacket[12] & 0x80) >> 7;
		mRtpBuffer[pos].mNRI = (rtpPacket[12] & 0x60) >> 5;
		mRtpBuffer[pos].mType = (rtpPacket[12] & 0x1f);

		if (RTPPacket.FUA == mRtpBuffer[pos].mPacketType) {

			mRtpBuffer[pos].mIsFirst = (((rtpPacket[13] & 0x80) >> 7) == 1);
			mRtpBuffer[pos].mIsLast = (((rtpPacket[13] & 0x40) >> 6) == 1);

			// set the origin media type
			mRtpBuffer[pos].mType = (rtpPacket[13] & 0x1f);
		}

		int offset = RTPPacket.FUA == mRtpBuffer[pos].mPacketType ? 14 : 13;
		int payloadLen = rtpPacketLen - offset;
		mRtpBuffer[pos].mPayload = new byte[payloadLen];
		System.arraycopy(rtpPacket, offset, mRtpBuffer[pos].mPayload, 0,
				payloadLen);
	}
	
	public void extractNalFromBuf() throws UnsupportedEncodingException {

		// order the RTP packets by the sequence number
		Comparator<RTPPacket> comparator = new RTPPacket();
		Arrays.sort(mRtpBuffer, comparator);

		for (int i = 0; i < mRtpBuffer.length; i++) {
			
			Log.d(tag_lostpacket, "seq no: " + mRtpBuffer[i].mSeqNo);

			if (RTPPacket.SGN == mRtpBuffer[i].mPacketType) {
				
				// the previous NALU is not complete,not finding the last FU
				if (firstFuFound) {

					// clear the firstFuFound
					firstFuFound = false;
					lastFuFound = true;

					/*
					 * process the current NAL buffer
					 */
					// Log.d("RTP", "last FU not found!!");
					byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
					tmpNalu[4] = (byte) (tmpNalu[4] | 0x80);
					Log.d("RTP", "one packet lose!!");
					mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
				}

				// set the start code(0x00000001)
				tmpHeader[0] = 0;
				tmpHeader[1] = 0;
				tmpHeader[2] = 0;
				tmpHeader[3] = 1;

				// set the NALU header
				tmpHeader[4] = 0;
				tmpHeader[4] = (byte) (tmpHeader[4] | (mRtpBuffer[i].mF << 7));
				tmpHeader[4] = (byte) (tmpHeader[4] | (mRtpBuffer[i].mNRI << 5));
				tmpHeader[4] = (byte) (tmpHeader[4] | mRtpBuffer[i].mType);

				tmpNalBuf = new String(tmpHeader, 0, 5, "ISO-8859-1");
				tmpNalBuf = tmpNalBuf.concat(new String(mRtpBuffer[i].mPayload,
						"ISO-8859-1"));

				byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
				
				if( 5 == (tmpHeader[4] & 0x1f)) {
					Log.d(tag_IDR, tag_IDR);
				}

				// decode the NALU and display the picture
				Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
				mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
			} else if (mRtpBuffer[i].mIsFirst) {

				firstFuFound = true;
				lastFuFound = false;

				timestamp = mRtpBuffer[i].mTimestamp;
				preNo = mRtpBuffer[i].mSeqNo;

				// set the start code(0x00000001)
				tmpHeader[0] = 0;
				tmpHeader[1] = 0;
				tmpHeader[2] = 0;
				tmpHeader[3] = 1;

				// set the NALU header
				tmpHeader[4] = 0;
				tmpHeader[4] = (byte) (tmpHeader[4] | (mRtpBuffer[i].mF << 7));
				tmpHeader[4] = (byte) (tmpHeader[4] | (mRtpBuffer[i].mNRI << 5));
				tmpHeader[4] = (byte) (tmpHeader[4] | mRtpBuffer[i].mType);

				tmpNalBuf = new String(tmpHeader, 0, 5, "ISO-8859-1");
				tmpNalBuf = tmpNalBuf.concat(new String(mRtpBuffer[i].mPayload,
						"ISO-8859-1"));
			} else if (mRtpBuffer[i].mIsLast) {

				if (firstFuFound && mRtpBuffer[i].mTimestamp == timestamp
						&& mRtpBuffer[i].mSeqNo == preNo + 1) {

					tmpNalBuf = tmpNalBuf.concat(new String(
							mRtpBuffer[i].mPayload, "ISO-8859-1"));

					// clear the firstFuFound
					firstFuFound = false;
					lastFuFound = true;
					
					if( 5 == (tmpHeader[4] & 0x1f)) {
						Log.d(tag_IDR, tag_IDR);
					}

					byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
					Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
					mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
				} else {
					if (firstFuFound) {

						// clear the firstFuFound
						firstFuFound = false;

						byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
						tmpNalu[4] = (byte) (tmpNalu[4] | 0x80);
						
						if( 5 == (tmpHeader[4] & 0x1f)) {
							Log.d(tag_IDR, tag_IDR);
						}
						
						Log.d("RTP", "one packet lose!!");
						mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
					}
				}

			} else {

				if (firstFuFound && mRtpBuffer[i].mTimestamp == timestamp
						&& mRtpBuffer[i].mSeqNo == preNo + 1) {

					tmpNalBuf = tmpNalBuf.concat(new String(
							mRtpBuffer[i].mPayload, "ISO-8859-1"));
					preNo++;
				} else {
					if (firstFuFound) {

						// clear the firstFuFound
						firstFuFound = false;

						byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
						tmpNalu[4] = (byte) (tmpNalu[4] | 0x80);
						
						if( 5 == (tmpHeader[4] & 0x1f)) {
							Log.d(tag_IDR, tag_IDR);
						}
						
						Log.d("RTP", "one packet lose!!");
						mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
					}
				}
			}

		} // for

		// clear the buffer
		mBufferUsedPos = 0;

	} // extractNalFromBuf
	
	public ProcessRtpPacketThread(WVSSView view, DatagramPacketQueue datagramPacketQueue) {
		
		mView = view;
		mDatagramPacketQueue = datagramPacketQueue;
		
		mIsFinished = false;
		
		AllocRtpBuffer();
	}
	
	@Override
	public void run() {
		
		while(!mIsFinished) {
			DatagramPacket rtpDatagram = mDatagramPacketQueue.take();
			
			if( null == rtpDatagram )
				break;
			
			int rtpPacketLen = rtpDatagram.getLength();
			byte[] rtpPacket = rtpDatagram.getData();

			fillRtpPacket(mBufferUsedPos, rtpPacket, rtpPacketLen);

			mBufferUsedPos++;
			
			// The RTP buffer is full
			if (mBufferUsedPos == mRtpBufferNum) {
				try {
					extractNalFromBuf();
				} catch (UnsupportedEncodingException e) {

					e.printStackTrace();
				}
			}
		}
	}
	
	public void setFinished() {
		mIsFinished = true;
		mDatagramPacketQueue.notifyAll();
	}
}
