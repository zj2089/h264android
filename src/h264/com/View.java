package h264.com;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.View;

class VView extends View implements Runnable{
    
    Bitmap  mBitQQ  = null;   
    
    Paint   mPaint = null;   
       
    Bitmap  mSCBitmap = null;   
    
    int width = 320;  // 此处设定不同的分辨率
    int height = 240;

    byte [] mPixel = new byte[width*height*2];
//    byte [] mRealPixel = new byte[width*height/2];
    
    ByteBuffer buffer = ByteBuffer.wrap( mPixel );
	Bitmap VideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);           
   
	int mTrans=0x0F0F0F0F;
	
	//String PathFileName; 
	
    public native int InitDecoder(int width, int height);
    public native int UninitDecoder(); 
    public native int DecoderNal(byte[] in, int insize, byte[] out);
    
    static {
        System.loadLibrary("H264Android");
    }
    
    public VView(Context context) {
        super(context);
        setFocusable(true);
        
       	int i = mPixel.length;
    	
        for(i=0; i<mPixel.length; i++)
        {
        	mPixel[i]=(byte)0x00;
        }
    }
           
    public void PlayVideo(String file)
    {
    	//PathFileName = file;   

    	new Thread(this).start();
    }
        
    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);   
        
//    	Bitmap tmpBit = Bitmap.createBitmap(mPixel, 320, 480, Bitmap.Config.RGB_565);//.ARGB_8888);            
    	
        VideoBit.copyPixelsFromBuffer(buffer);//makeBuffer(data565, N));
    	
        canvas.drawBitmap(VideoBit, 0, 0, null); 
    }
    
//    int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf, int SockBufUsed, int SockRemain)
//    {
//    	int  i=0;
//    	byte Temp;
//
//    	for(i=0; i<SockRemain; i++)
//    	{
//    		Temp  =SockBuf[i+SockBufUsed];
//    		NalBuf[i+NalBufUsed]=Temp;
//
//    		mTrans <<= 8;
//    		mTrans  |= Temp;
//
//    		if(mTrans == 1) // 找到一个开始字 
//    		{
//    			i++;
//    			break;
//    		}	
//    	}
//
//    	return i;
//    }
//    
    public void run()   
    {   
//    	InputStream is = null;
//    	FileInputStream fileIS=null;
//    	
//    	int iTemp=0;
//    	int nalLen;
//    	
//    	boolean bFirst=true;
//    	boolean bFindPPS=true;
//    	
//    	int bytesRead=0;    	
//    	int NalBufUsed=0;
//    	int SockBufUsed=0;
//        
//    	byte [] NalBuf = new byte[307200]; // 300k
//    	byte [] SockBuf = new byte[2048];
//    	
//    	try
//    	{    
//   			fileIS = new FileInputStream(PathFileName);	
//    	}
//    	catch(IOException e) 
//    	{
//    		return ;
//    	} 
//    	
//    	InitDecoder(width, height); 
// 	
//        while (!Thread.currentThread().isInterrupted())   
//        {   
//            try  
//            {   
//          		bytesRead = fileIS.read(SockBuf, 0, 2048);
//            }
//            catch (IOException e) {}
//            
//            if(bytesRead<=0)
//            	break;
//            
//            SockBufUsed =0;
//            
//    		while(bytesRead-SockBufUsed>0)
//    		{
//    			nalLen = MergeBuffer(NalBuf, NalBufUsed, SockBuf, SockBufUsed, bytesRead-SockBufUsed);
//    					
//    			NalBufUsed += nalLen;
//    			SockBufUsed += nalLen;
//    			
//    			while(mTrans == 1)
//    			{
//    				mTrans = 0xFFFFFFFF;
//
//    				if(bFirst==true) // the first start flag
//    				{
//    					bFirst = false;
//    				}
//    				else  // a complete NAL data, include 0x00000001 trail.
//    				{
//    					if(bFindPPS==true) // true
//    					{
//    						if( (NalBuf[4]&0x1F) == 7 )
//    						{
//    							bFindPPS = false;
//    						}
//    						else
//    						{
//    			   				NalBuf[0]=0;
//    		    				NalBuf[1]=0;
//    		    				NalBuf[2]=0;
//    		    				NalBuf[3]=1;
//    		    				
//    		    				NalBufUsed=4;
//    		    				
//    							break;
//    						}
//    					}
//    					//	decode nal
//    					iTemp=DecoderNal(NalBuf, NalBufUsed-4, mPixel);   
//					
//    		            if(iTemp>0)
//    		            	postInvalidate();  //使用postInvalidate可以直接在线程中更新界面    // postInvalidate();
//    				}
//
//    				NalBuf[0]=0;
//    				NalBuf[1]=0;
//    				NalBuf[2]=0;
//    				NalBuf[3]=1;
//    				
//    				NalBufUsed=4;
//    			}		
//    		} 
//        } 
//        try{        	
//	        if(fileIS!=null)
//	        	fileIS.close();		
//	        if(is!=null)
//	        	is.close();
//        }
//	    catch (IOException e) {
//	    	e.printStackTrace();
//        }
//        UninitDecoder();
    	
    	InitDecoder(width, height);
    	CTCPServerThread serverThrd = new CTCPServerThread();
    	serverThrd.start();
    	
    	
    }  
    
    class CTCPServerThread extends Thread {
    	// 套接字服务接口
    	private ServerSocket mServerSocket = null;
    	
    	public CTCPServerThread() {
    		
    		// 初始化服务套接字
    		try {
    			//创建套接字服务示例
    			mServerSocket = new ServerSocket(ClientConfig.CONFIG_SERVER_PORT);
    		}
    		catch(IOException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	@Override
    	public void run() {
    		//启动服务
    		startServer();
    		
    		if(mServerSocket == null) {
    			return;
    		}
    		
    		try {
    			mServerSocket.close();
    		}
    		catch(IOException e) {
    			e.printStackTrace();
    		}
    	}

    	// 开始服务
    	private void startServer() {
    		Socket socket = null;
    		try {
    			
    			while(true) {
    				
    				System.out.println("connecting...");
    				
    				// 接受客户端连接
    				socket = mServerSocket.accept();
    				
    				System.out.println("connect successfully!");
    				
    				// 启动客户端服务线程（开始一个会话）
    				CTCPSessionThread sessionThrd = new CTCPSessionThread(socket);
    				sessionThrd.start();
    			}
    		}
    		catch(IOException e) {
    			e.printStackTrace();
    		}
    		finally {
    			try {
					socket.close();
				} 
    			catch (IOException e) {
					
					e.printStackTrace();
				}
    		}
    	}
    }
    
    public class CTCPSessionThread extends Thread {
    	// 与客户端通信套接字
    	private boolean mIsFinish = false;
    	private InputStream inputstream;
    	FileInputStream fis = null; 
    	
    	public CTCPSessionThread(Socket socket) {
    		
    		// 获取会话用输入/输出流
    		try {
    			inputstream = socket.getInputStream();
    			//fis = new FileInputStream("/sdcard/640x480.yuv");
    		}
    		catch(IOException e) {
    			e.printStackTrace();
    		}
    	}
    	
		private byte[] InputStreamToByte(InputStream is) throws IOException {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		}
    	
    	@Override
    	public void run() {
    		while(!mIsFinish) {
    			
    			
    			// 获取客户端请求
    			try {
    				
    				byte buf[] = new byte[ClientConfig.CONFIG_BUFFER_SIZE];
    				byte btNalLen[] = new byte[2]; 
    				
    				
    				inputstream.read(btNalLen, 0, 2);
    				
    				int highBit = ((int)btNalLen[0]>=0)?((int)btNalLen[0]):(256+(int)btNalLen[0]);
    				int lowBit = ((int)btNalLen[1]>=0)?((int)btNalLen[1]):(256+(int)btNalLen[1]);
    				
    				int nalLen = highBit*256+lowBit;
    				
    				Log.d("NalLen", ""+nalLen);
    				
    				int bufLen = inputstream.read(buf, 0, nalLen);
    				
    				
    				// 对获取的数据进行处理
    				
    				int iTemp = DecoderNal(buf, bufLen, mPixel);
    				  
//    				InputStream is = new ByteArrayInputStream(mPixel); 
//    				
//    				is.read(mRealPixel, 0, mRealPixel.length);
    				
    				if(iTemp>0)
    					postInvalidate(); 
    				
    				try {
						Thread.currentThread().sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				
    				Log.d("pIC", "end process input");
    			
    			}
    			catch(IOException e) {
    				e.printStackTrace();
    				Log.d("nullpoint", "inputstream is null");
    			}
    		}
    	//	super.run();
    	}
    	
    	
    	// 设置该线程是否结束的标识
    	public void setIsFinish(boolean isFinish) {
    		this.mIsFinish = isFinish;
    	}
    }
    
    class CRTPClientThread extends Thread {
    	
    	private DatagramSocket mClientDatagram = null;
    	
    	public CRTPClientThread() {
    		try {
				mClientDatagram = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			// to recevie the rtp packet
			byte[] rtpPacket = new byte[ClientConfig.CONFIG_RTP_PACKET_SIZE];
			DatagramPacket rtpDatagram = new DatagramPacket(rtpPacket, rtpPacket.length);
			
			while (true) {
				
				try {
					mClientDatagram.receive(rtpDatagram);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				break;
			}
			
			super.run();
		}
    }
}
