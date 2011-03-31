package h264.com;	

// Some Constant
public interface ClientConfig {
	
	static final String SERVER_HOST = "localhost";
	static final int CONFIG_SERVER_PORT = 2000;
	static final int CONFIG_BUFFER_SIZE = 40*1024; // 40K
	
	// the max size of one rtp packet
	static final int CONFIG_RTP_PACKET_SIZE = 1400;
}
