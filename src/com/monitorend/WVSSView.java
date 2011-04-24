package com.monitorend;

import java.nio.ByteBuffer;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.view.View;

class WVSSView extends View{
	
    private byte [] mPixel;    
    private ByteBuffer mBuffer;
	private Bitmap mVideoBit;           
   
    public native int InitDecoder(int width, int height);
    public native int UninitDecoder(); 
    public native int DecodeNal(byte[] in, int insize, byte[] out);
    
    static {
        System.loadLibrary("H264Decoder");
    }
       
    public WVSSView(Context context, int width, int height) {
        super(context);
        setFocusable(true);
        
        Arrays.fill(mPixel, (byte) 0);
        
        mPixel = new byte[width*height*2];
        mBuffer = ByteBuffer.wrap( mPixel );
        mVideoBit = Bitmap.createBitmap(width, height, Config.RGB_565); 
        
        InitDecoder(width, height);
    }
    
    // decode the NALU and display the picture
    public void decodeNalAndDisplay(byte[] nalBuf, int nalLen) {
    	int iTmp = DecodeNal(nalBuf, nalLen, mPixel);
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
