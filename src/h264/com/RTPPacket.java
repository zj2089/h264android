package h264.com;

import java.util.Comparator;

class RTPPacket implements Comparator {
	
	/*
	 *  the type of NAL in the current RTP packet 
	 */
	static final int SGN = 0; // single NAL unit
	static final int FUA = 1; // FU-A
	
	public int mTimestamp;
	public int mSeqNo;
	public int mPacketType;
	
	/*
	 * only available for FU-A
	 */
	public boolean mIsFirst;
	public boolean mIsLast;
	
	/*
	 * NAL Header
	 */
	public int mF;
	public int mNRI;
	public int mType;
	
	/*
	 * NAL payload
	 */
	public int mPayloadLen;
	public byte[] mPayload = new byte[ClientConfig.CONFIG_RTP_PACKET_SIZE];
	
	int compare(RTPPacket p1, RTPPacket p2) {
		return p1.mSeqNo - p2.mSeqNo;
	}
	
	boolean equals(RTPPacket p) {
		return this.mSeqNo == p.mSeqNo;
	}

	@Override
	public int compare(Object object1, Object object2) {
		// TODO Auto-generated method stub
		RTPPacket p1 = (RTPPacket) object1;
		RTPPacket p2 = (RTPPacket) object2;
		return p1.mSeqNo - p2.mSeqNo;
	}
	
}