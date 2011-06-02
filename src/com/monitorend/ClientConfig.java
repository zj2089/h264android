package com.monitorend;	

// Some Constant
public interface ClientConfig {	
	
	// to receive PPS and SPS
	static final int SPS_PPS_BUFFER_SIZE = 50; // 50 bytes
	
	// the max size of one RTP packet
	static final int RTP_PACKET_MAX_SIZE = 1400;
	
	// the number of buffer
	static final int RTP_PACKET_BUFFER_NUM = 6;
	
	/**
	 * the IP address of center server
	 */
	static final String CENTER_SERVER_IP_ADDRESS = "10.0.2.2";
	
	/**
	 * the port of center server that listening the request of monitor end
	 */
	static final int CENTER_SERVER_LISTEN_MONITOR_END_PORT = 8000;
	
	/**
	 * the port monitor end uses to receive RTP packet
	 */
	static final int PORT_MONITOR_END_RECEIVE_RTP = 10000;

	/**
	 * the port monitor end receive SPS & PPS from
	 */
	static final int MONITOR_END_RECV_SPS_PORT = 9000;
	
	/**
	 * the port monitor end sends command to
	 */
	static final int PORT_MONITOR_END_SEND_COMMAND_TO = 11000;
	
	/**
	 * the width of received video
	 */
	static final int VIDEO_WIDTH = 320;
	
	/**
	 * the height of received video
	 */
	static final int VIDEO_HEIGHT = 240;
}
