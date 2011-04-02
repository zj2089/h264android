package h264.com;

import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
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
    
    // decode the NALU and display the picture
    public void decodeNalAndDisplay(byte[] nalBuf, int nalLen) {
    	
    	int iTmp = DecoderNal(nalBuf, nalLen, mPixel);
    	
    	if( iTmp > 0 ) {
    		postInvalidate();
    	}
    }
        
    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);   
        
//    	Bitmap tmpBit = Bitmap.createBitmap(mPixel, 320, 480, Bitmap.Config.RGB_565);//.ARGB_8888);            
    	
        VideoBit.copyPixelsFromBuffer(buffer);//makeBuffer(data565, N));
    	
        canvas.drawBitmap(VideoBit, 0, 0, null); 
    }

	@Override
	public void run() {
		
		InitDecoder(width, height);
		
    	CTCPServerThread serverThrd = new CTCPServerThread(this);
    	serverThrd.start();
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
//    public void run()   
//    {   
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
//    	   	
//    	InitDecoder(width, height);
//    	CTCPServerThread serverThrd = new CTCPServerThread(this);
//    	serverThrd.start();
//    	
//    }
}
