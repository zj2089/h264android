package h264.com;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;

import android.util.Log;

class CRTPClientThread extends Thread {

	VView mView;

	// the RTP buffer
	RTPPacket[] mRtpBuffer;
	int mRtpBufferLen;
	int mBufferUsedPos;

	private DatagramSocket mClientDatagram = null;

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

	public CRTPClientThread(VView view) {

		// The Client doesn't need to specify the server host and port when
		// initializing
		try {
			mClientDatagram = new DatagramSocket(
					ClientConfig.CONFIG_CLIENT_UDP_PORT);
		} catch (SocketException e) {

			e.printStackTrace();
		}

		// specify the server host and port
		try {
			mClientDatagram.connect(InetAddress
					.getByName(ClientConfig.CONFIG_RTP_SERVER_HOST),
					ClientConfig.CONFIG_SERVER_UDP_PORT);
			Log.d("RTP", "UDP connected");
		} catch (UnknownHostException e) {

			Log.d("RTP", "UDP connecting failed");
			e.printStackTrace();
		}

		mView = view;
		AllocRtpBuffer();
	}

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

		mRtpBufferLen = ClientConfig.CONFIG_RTP_BUFFER_SIZE
				/ ClientConfig.CONFIG_RTP_PACKET_SIZE;

		// Log.d("RTP", "" + mRtpBufferLen);

		mRtpBuffer = new RTPPacket[mRtpBufferLen];
		for (int i = 0; i < mRtpBufferLen; i++)
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
		mRtpBuffer[pos].mPayloadLen = rtpPacketLen - 13;

		if (RTPPacket.FUA == mRtpBuffer[pos].mPacketType) {

			mRtpBuffer[pos].mIsFirst = (((rtpPacket[13] & 0x80) >> 7) == 1);
			mRtpBuffer[pos].mIsLast = (((rtpPacket[13] & 0x40) >> 6) == 1);

			// set the origin media type
			mRtpBuffer[pos].mType = (rtpPacket[13] & 0x1f);
		}

		if (RTPPacket.FUA == mRtpBuffer[pos].mPacketType) {

			// set the FU pay load length, excluding the FU header
			mRtpBuffer[pos].mPayloadLen--;

			mRtpBuffer[pos].mPayload = Arrays.copyOfRange(rtpPacket, 14,
					rtpPacketLen);
		} else {
			mRtpBuffer[pos].mPayload = Arrays.copyOfRange(rtpPacket, 13,
					rtpPacketLen);
		}
	}

	public void extractNalFromBuf() throws UnsupportedEncodingException {

		// order the RTP packets by the sequence number
		Comparator<RTPPacket> comparator = new RTPPacket();
		Arrays.sort(mRtpBuffer, comparator);

		for (int i = 0; i < mRtpBuffer.length; i++) {

			Log.d("RTP", "ts:" + mRtpBuffer[i].mTimestamp);
			Log.d("RTP", "seq_no:" + mRtpBuffer[i].mSeqNo);

			if (RTPPacket.SGN == mRtpBuffer[i].mPacketType) {

				Log.d("RTP", "single NAL unit!!!");

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
					Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
					// mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
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
						0, mRtpBuffer[i].mPayloadLen, "ISO-8859-1"));

				byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");

				// decode the NALU and display the picture
				Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
				mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
			} else if (mRtpBuffer[i].mIsFirst) {

				Log.d("RTP", "first FU");

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
						0, mRtpBuffer[i].mPayloadLen, "ISO-8859-1"));
			} else if (mRtpBuffer[i].mIsLast) {

				Log.d("RTP", "last FU");

				if (firstFuFound && mRtpBuffer[i].mTimestamp == timestamp
						&& mRtpBuffer[i].mSeqNo == preNo + 1) {

					tmpNalBuf = tmpNalBuf.concat(new String(
							mRtpBuffer[i].mPayload, 0,
							mRtpBuffer[i].mPayloadLen, "ISO-8859-1"));

					// clear the firstFuFound
					firstFuFound = false;
					lastFuFound = true;

					byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
					Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
					mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
				} else {
					if (firstFuFound) {

						// clear the firstFuFound
						firstFuFound = false;

						byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
						tmpNalu[4] = (byte) (tmpNalu[4] | 0x80);
						Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
						Log.d("RTP", "one packet lose!!");
						// mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
					}
				}

			} else {

				Log.d("RTP", "middle FU");

				if (firstFuFound && mRtpBuffer[i].mTimestamp == timestamp
						&& mRtpBuffer[i].mSeqNo == preNo + 1) {

					tmpNalBuf = tmpNalBuf.concat(new String(
							mRtpBuffer[i].mPayload, 0,
							mRtpBuffer[i].mPayloadLen, "ISO-8859-1"));
					preNo++;
				} else {
					if (firstFuFound) {

						// clear the firstFuFound
						firstFuFound = false;

						byte[] tmpNalu = tmpNalBuf.getBytes("ISO-8859-1");
						tmpNalu[4] = (byte) (tmpNalu[4] | 0x80);
						Log.d("RTP", "decoding NAL len:" + tmpNalu.length);
						Log.d("RTP", "one packet lose!!");
						// mView.decodeNalAndDisplay(tmpNalu, tmpNalu.length);
					}
				}
			}

		} // for

		// clear the buffer
		mBufferUsedPos = 0;

	} // extractNalFromBuf

	@Override
	public void run() {

		// to receive the RTP packet
		byte[] rtpPacket = new byte[ClientConfig.CONFIG_RTP_PACKET_SIZE];
		DatagramPacket rtpDatagram = new DatagramPacket(rtpPacket,
				rtpPacket.length);
		int rtpPacketLen;

		while (true) {

			// Log.d("RTP", "start RTP receiving");
			try {
				mClientDatagram.receive(rtpDatagram);
			} catch (IOException e) {

				Log.d("RTP", e.getMessage());
				e.printStackTrace();
			}
			// Log.d("RTP", "one packet received!!");

			rtpPacketLen = rtpDatagram.getLength();
			rtpPacket = rtpDatagram.getData();

			// Log.d("RTP", "RTP packet len:"+rtpPacketLen);

			fillRtpPacket(mBufferUsedPos, rtpPacket, rtpPacketLen);

			mBufferUsedPos++;

			// Log.d("RTP", "RTP Buf Pos:" + mBufferUsedPos);

			// The RTP buffer is full
			if (mBufferUsedPos == mRtpBufferLen) {
				try {
					extractNalFromBuf();
				} catch (UnsupportedEncodingException e) {

					e.printStackTrace();
				}
			}
		}

	}
}