package com.monitorend;

import java.util.Comparator;

class RTPPacket implements Comparator<RTPPacket> {
	
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
	public int mType;	// the origin NALU type
	
	/*
	 * NAL pay load
	 */
	public byte[] mPayload;
	
	@Override
	public int compare(RTPPacket p1, RTPPacket p2) {

		return p1.mSeqNo - p2.mSeqNo;	
		
	}

	@Override
	public boolean equals(Object o) {
		
		RTPPacket p = (RTPPacket)o;
		return this.mSeqNo == p.mSeqNo;
	}
	
}