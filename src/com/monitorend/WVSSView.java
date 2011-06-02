package com.monitorend;

import java.nio.ByteBuffer;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.view.View;

class WVSSView extends View {
	
    private byte [] mPixel;    
    private ByteBuffer mBuffer;
	private Bitmap mVideoBit;
	private int mCanvasWidth;
	private int mCanvasHeight;
   
    public native int InitDecoder(int width, int height);
    public native int UninitDecoder(); 
    public native int DecodeNal(byte[] in, int insize, byte[] out, int canvasWidth,
    		int canvasHeight);
    
    static {
        System.loadLibrary("H264Decoder");
    }
       
    public WVSSView(Context context, int canvasWidth, int canvasHeight) {
        super(context);
        setFocusable(true);
        
        mCanvasWidth = canvasWidth;
        mCanvasHeight = canvasHeight;
        
        mPixel = new byte[canvasWidth*canvasHeight*2];      
        Arrays.fill(mPixel, (byte) 0);

        mBuffer = ByteBuffer.wrap( mPixel );
        mVideoBit = Bitmap.createBitmap(canvasWidth, canvasHeight, Config.RGB_565); 
        
        InitDecoder(ClientConfig.VIDEO_WIDTH, ClientConfig.VIDEO_HEIGHT);
    }
    
    // decode the NALU and display the picture
    public void decodeNalAndDisplay(byte[] nalBuf, int nalLen) {
    	int iTmp = DecodeNal(nalBuf, nalLen, mPixel, mCanvasWidth, mCanvasHeight);
    	if( iTmp > 0 ) {
    		postInvalidate();
    	}
    }
        
    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);   
        mVideoBit.copyPixelsFromBuffer(mBuffer);
        canvas.drawBitmap(mVideoBit, 0, 0, null); 
    }
}
