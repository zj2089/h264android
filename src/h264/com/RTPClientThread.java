package h264.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Comparator;

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
	byte[] nalu = new byte[20*1024];
	int naluLen;

	// to record the time stamp of some FU
	int timestamp;
	
	// record the previous sequence number of FU
	int preNo;
	
	// to save the NAL buffer temporarily for one FU
	String tmpNalBuf;
	
	public CRTPClientThread(VView view) {
		try {
			mClientDatagram = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		mView = view;
		AllocRtpBuffer();
	}
	


	private int toInt(byte b1, byte b2, byte b3, byte b4) {

		return (int)b1*(1<<24) + (int)b2*65536 + (int)b3*256 + (int)b4;
	}

	private int toInt(byte b1, byte b2) {

		return (int)b1*256 + (int)b2*b2;
	}
	
    private void AllocRtpBuffer() {
    	
    	mRtpBufferLen = ClientConfig.CONFIG_RTP_BUFFER_SIZE / ClientConfig.CONFIG_RTP_PACKET_SIZE;
    	
    	mRtpBuffer = new RTPPacket[mRtpBufferLen];
    	
    	mBufferUsedPos = 0;
    }

	private void fillRtpPacket(int pos, byte[] rtpPacket, int rtpPacketLen)	{

		mRtpBuffer[pos].mTimestamp = toInt(rtpPacket[4], rtpPacket[5], rtpPacket[6], rtpPacket[7]);
		mRtpBuffer[pos].mSeqNo = toInt(rtpPacket[2], rtpPacket[3]);
		mRtpBuffer[pos].mPacketType = ((rtpPacket[12] & 0x1f)==28 ? RTPPacket.FUA : RTPPacket.SGN);


		if( RTPPacket.FUA == mRtpBuffer[pos].mPacketType ) {

			mRtpBuffer[pos].mIsFirst = (((rtpPacket[13] & 0x80)>>7)==1);
			mRtpBuffer[pos].mIsLast = (((rtpPacket[13] & 0x40)>>6)==1);
		}

		mRtpBuffer[pos].mF = (rtpPacket[12]&0x80)>>7;
		mRtpBuffer[pos].mNRI = (rtpPacket[12]&0x60)>>5;
		mRtpBuffer[pos].mType = (rtpPacket[12]&0x1f);

		mRtpBuffer[pos].mPayloadLen = rtpPacketLen - 13;
		if( RTPPacket.FUA == mRtpBuffer[pos].mPacketType ) {

			mRtpBuffer[pos].mPayloadLen--;
			mRtpBuffer[pos].mPayload = Arrays.copyOfRange(rtpPacket, 14, rtpPacketLen);
		}
		else {
			mRtpBuffer[pos].mPayload = Arrays.copyOfRange(rtpPacket, 13, rtpPacketLen);
		}
	}

	public void extractNalFromBuf() {

		// order the RTP packets by the sequence number
		Comparator<RTPPacket> comparator = new RTPPacket();
		Arrays.sort(mRtpBuffer, comparator);
		
		for(int i=0; i<mRtpBuffer.length; i++) {

			if( RTPPacket.SGN == mRtpBuffer[i].mPacketType ) {
				
				// the previous NALU is not complete,not finding the last FU
				if(!lastFuFound) {
					
					/*
					 * process the current NAL buffer
					 */
					nalu = tmpNalBuf.getBytes();
					nalu[4] = (byte) (nalu[4]|0x80);
					mView.decodeNalAndDisplay(nalu, naluLen);
				}

				// include the size of start code(0x00000001) and the header of NALU
				naluLen = mRtpBuffer[i].mPayloadLen + 5;
				nalu[0] = 0;
				nalu[1] = 0;
				nalu[2] = 0;
				nalu[3] = 1;
				nalu[4] = 0;
				nalu[4] = (byte) (nalu[4]|(mRtpBuffer[i].mF<<7));
				nalu[4] = (byte) (nalu[4]|(mRtpBuffer[i].mNRI<<5));
				nalu[4] = (byte) (nalu[4]|mRtpBuffer[i].mType);

				String str1 = new String(nalu, 0, naluLen);
				String str2 = new String(mRtpBuffer[i].mPayload, 0, mRtpBuffer[i].mPayloadLen);

				nalu = str1.concat(str2).getBytes();

				// decode the NALU and display the picture
				mView.decodeNalAndDisplay(nalu, naluLen);
			}
			else {				
			
				if(mRtpBuffer[i].mIsFirst) {

					firstFuFound = true;
					lastFuFound = false;
					
					naluLen = 5;
					
					timestamp = mRtpBuffer[i].mTimestamp;
					preNo = mRtpBuffer[i].mSeqNo;
					
					byte[] tmpHeader = new byte[5];
					tmpHeader[0] = 0;
					tmpHeader[1] = 0;
					tmpHeader[2] = 0;
					tmpHeader[3] = 1;
					
					// set the NALU header
					tmpHeader[4] = 0;
					tmpHeader[4] = (byte) (tmpHeader[4]|(mRtpBuffer[i].mF<<7));
					tmpHeader[4] = (byte) (tmpHeader[4]|(mRtpBuffer[i].mNRI<<5));
					tmpHeader[4] = (byte) (tmpHeader[4]|mRtpBuffer[i].mType);
					
					tmpNalBuf = new String(tmpHeader, 0, 5);
					tmpNalBuf.concat(new String(mRtpBuffer[i].mPayload, 0, mRtpBuffer[i].mPayloadLen));
					naluLen += mRtpBuffer[i].mPayloadLen;
					
					i++;					
					while(mRtpBuffer[i].mTimestamp==timestamp && i<mRtpBuffer.length) {
						
						// the sequence NO is continuous
						if( mRtpBuffer[i].mSeqNo == preNo+1 ) {
							
							preNo = mRtpBuffer[i].mSeqNo;
							tmpNalBuf.concat(new String(mRtpBuffer[i].mPayload, 0, mRtpBuffer[i].mPayloadLen));
							naluLen += mRtpBuffer[i].mPayloadLen;
							
							if(mRtpBuffer[i].mIsLast) {
								
								// clear the firstFuFound
								firstFuFound = false;
								lastFuFound = true;
								
								nalu = tmpNalBuf.getBytes();
								mView.decodeNalAndDisplay(nalu, naluLen);
							}
						}
						i++;
					}
					i--;
				}
				else {
					if(firstFuFound) {
						
						while(mRtpBuffer[i].mTimestamp==timestamp && i<mRtpBuffer.length) {
							
							// the sequence NO is continuous
							if( mRtpBuffer[i].mSeqNo == preNo+1 ) {
								
								preNo = mRtpBuffer[i].mSeqNo;
								tmpNalBuf.concat(new String(mRtpBuffer[i].mPayload, 0, mRtpBuffer[i].mPayloadLen));
								naluLen += mRtpBuffer[i].mPayloadLen;
								
								if(mRtpBuffer[i].mIsLast) {
									
									// clear the firstFuFound
									firstFuFound = false;
									lastFuFound = true;
									
									nalu = tmpNalBuf.getBytes();
									mView.decodeNalAndDisplay(nalu, naluLen);
								}
							}
							i++;
						}
						i--;
					}
				}
			}
		} // for
		
		mBufferUsedPos = 0;

	} // extractNalFromBuf

	@Override
	public void run() {

		// to receive the RTP packet
		byte[] rtpPacket = new byte[ClientConfig.CONFIG_RTP_PACKET_SIZE];
		DatagramPacket rtpDatagram = new DatagramPacket(rtpPacket, rtpPacket.length);
		int rtpPacketLen;

		while (true) {

			try {
				mClientDatagram.receive(rtpDatagram);
			}
			catch (IOException e) {
				
				e.printStackTrace();
			}

			rtpPacketLen = rtpDatagram.getLength();
			rtpPacket = rtpDatagram.getData();

			fillRtpPacket(mBufferUsedPos, rtpPacket, rtpPacketLen);

			mBufferUsedPos++;

			// The RTP buffer is full
			if(mBufferUsedPos==mRtpBufferLen) {
				extractNalFromBuf();
			}

			break;
		}

		super.run();
	}
}