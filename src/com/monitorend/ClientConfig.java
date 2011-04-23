package com.monitorend;	

// Some Constant
public interface ClientConfig {
	
	// i.e., "127.0.0.1" on the development machine
	static final String CONFIG_RTP_SERVER_HOST = "10.0.2.2";
	
	/*
	 * RTP port(UDP port)
	 * PC end acts as server
	 * android end acts as client
	 */
	static final int CONFIG_SERVER_UDP_PORT = 8000;
	static final int CONFIG_CLIENT_UDP_PORT = 9000;
	
	/*
	 * TCP port
	 * android end acts as server
	 * PC end acts as client
	 */
	static final int CONFIG_SERVER_TCP_PORT = 2000;	
	static final int CONFIG_CLIENT_TCP_PORT = 2000;	
	
	
	// to receive PPS and SPS
	static final int CONFIG_BUFFER_SIZE = 40*1024; // 30 bytes
	
	// the max size of one RTP packet
	static final int CONFIG_RTP_PACKET_SIZE = 1400;
	
	// the number of buffer
	static final int CONFIG_BUFFER_NUM = 6;
	
	/*
	 * the size of RTP buffer,
	 * RTP buffer is used to order the RTP packets
	 */
	static final int CONFIG_RTP_BUFFER_SIZE = CONFIG_BUFFER_NUM * CONFIG_RTP_PACKET_SIZE;
	
	/**
	 * the IP address of center server
	 */
	static final String CENTER_SERVER_IP_ADDRESS = "127.0.0.1";
	
	/**
	 * the port of center server that listening the request of monitor end
	 */
	static final int CENTER_SERVER_PORT = 8000;
	
}
