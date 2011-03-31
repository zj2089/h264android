package h264.com;

class RTPPacket {
	
	// the type of NAL in the current RTP packet 
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
	 * 
	 */
}