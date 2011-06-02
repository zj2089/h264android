#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "avc_dec.h"

#include "com_monitorend_WVSSView.h"
		
int iWidth=0;
int iHeight=0;
int oldWidth=0;
int oldHeight=0;
	
int *colortab=NULL;
int *u_b_tab=NULL;
int *u_g_tab=NULL;
int *v_g_tab=NULL;
int *v_r_tab=NULL;

unsigned int *rgb_2_pix=NULL;
unsigned int *r_2_pix=NULL;
unsigned int *g_2_pix=NULL;
unsigned int *b_2_pix=NULL;
unsigned char *outBuf=NULL;
unsigned int* orgPixel = NULL;
		
void DeleteYUVTab()
{	
	if(colortab)
	{
		free(colortab);
	}

	if (rgb_2_pix)
	{
		free(rgb_2_pix);
	}
	
}

void ConvertPic(const unsigned char* srcBuf, unsigned char* dstBuf,
				const int srcWidth, const int srcHeight,
				const int dstWidth, const int dstHeight)
{
	// the width scale
	double fx = dstWidth*1.0/srcWidth;

	// the height scale
	double fy = dstHeight*1.0/srcHeight;

	/*
	 * scanning the destination picture from bottom to top,
	 * from left to right
	 */
	for (int dstRow=dstHeight-1; dstRow>=0; dstRow--)
	{
		for(int dstCol=0; dstCol<dstWidth; dstCol++)
		{
			/*
			 * scanning to (dstCol, dstRow),(column, row)
			 */

			// the corresponding source row
			int cor_SrcRow = (int)(dstRow/fy+0.5);

			// the corresponding source column
			int cor_SrcCol = (int)(dstCol/fx+0.5);
			*(dstBuf++) = *(srcBuf + ((srcHeight-1-cor_SrcRow)*srcWidth+cor_SrcCol)*2);
			*(dstBuf++) = *(srcBuf + ((srcHeight-1-cor_SrcRow)*srcWidth+cor_SrcCol)*2 + 1);
		}
	}
}

void CreateYUVTab_16()
{
	int i;
	int u, v;

	colortab = (int *)malloc((sizeof(int)<<10));
	u_b_tab = &colortab[0*256];
	u_g_tab = &colortab[1*256];
	v_g_tab = &colortab[2*256];
	v_r_tab = &colortab[3*256];

	for (i=0; i<256; i++)
	{
		u = v = (i-128);

		u_b_tab[i] = (int) ( 1.772 * u);
		u_g_tab[i] = (int) ( 0.34414 * u);
		v_g_tab[i] = (int) ( 0.71414 * v); 
		v_r_tab[i] = (int) ( 1.402 * v);
	}

	rgb_2_pix = (unsigned int *)malloc(3*768*sizeof(unsigned int));

	r_2_pix = &rgb_2_pix[0*768];
	g_2_pix = &rgb_2_pix[1*768];
	b_2_pix = &rgb_2_pix[2*768];

	for(i=0; i<256; i++)
	{
		r_2_pix[i] = 0;
		g_2_pix[i] = 0;
		b_2_pix[i] = 0;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+256] = (i & 0xF8) << 8;
		g_2_pix[i+256] = (i & 0xFC) << 3;
		b_2_pix[i+256] = (i ) >> 3;
	}

	for(i=0; i<256; i++)
	{
		r_2_pix[i+512] = 0xF8 << 8;
		g_2_pix[i+512] = 0xFC << 3;
		b_2_pix[i+512] = 0x1F;
	}

	r_2_pix += 256;
	g_2_pix += 256;
	b_2_pix += 256;
}

void DisplayYUV_16(unsigned int *pdst1, unsigned char *y, unsigned char *u, 
					unsigned char *v, int width, int height, 
					int src_ystride, int src_uvstride, int dst_ystride)
{
	int i, j;
	int r, g, b, rgb;

	int yy, ub, ug, vg, vr;

	unsigned char* yoff;
	unsigned char* uoff;
	unsigned char* voff;
	
	unsigned int* pdst=pdst1;

	int width2 = width/2;
	int height2 = height/2;
	
	if(width2>iWidth/2)
	{
		width2=iWidth/2;

		y+=(width-iWidth)/4*2;
		u+=(width-iWidth)/4;
		v+=(width-iWidth)/4;
	}

	if(height2>iHeight)
		height2=iHeight;

	/*
	 * four pixels once
	 */ 
	for(j=0; j<height2; j++) 
	{
		yoff = y + j * 2 * src_ystride;
		uoff = u + j * src_uvstride;
		voff = v + j * src_uvstride;

		for(i=0; i<width2; i++)
		{
			yy  = *(yoff+(i<<1));
			ub = u_b_tab[*(uoff+i)];
			ug = u_g_tab[*(uoff+i)];
			vg = v_g_tab[*(voff+i)];
			vr = v_r_tab[*(voff+i)];

			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst[(j*dst_ystride+i)] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);

			yy = *(yoff+(i<<1)+src_ystride);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			rgb = r_2_pix[r] + g_2_pix[g] + b_2_pix[b];

			yy = *(yoff+(i<<1)+src_ystride+1);
			b = yy + ub;
			g = yy - ug - vg;
			r = yy + vr;

			pdst [((2*j+1)*dst_ystride+i*2)>>1] = (rgb)+((r_2_pix[r] + g_2_pix[g] + b_2_pix[b])<<16);
		}
	}
}

AvcDecoder decoder;			

/*
 * Class:     com_monitorend_WVSSView
 * Method:    InitDecoder
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_monitorend_WVSSView_InitDecoder
  (JNIEnv *env, jobject thiz, jint width, jint height) 
{
	if (decoder.AvcDecInit() != OMX_ErrorNone)
	{
		return 0;
	}
	
	oldWidth = width;
	oldHeight = height;
	iWidth = width;
	iHeight = height;
	CreateYUVTab_16();
	
	outBuf = (unsigned char*)malloc(((iWidth*iHeight*3)>>1));
	orgPixel = (unsigned int*)malloc(((oldWidth*oldHeight)<<1));
	
	return 1;
}


/*
 * Class:     com_monitorend_WVSSView
 * Method:    UninitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_monitorend_WVSSView_UninitDecoder
  (JNIEnv *env, jobject thiz)
{
	if (decoder.AvcDecDeinit() != OMX_ErrorNone)
	{
		return 0;
	}
	
	DeleteYUVTab();
	
	if(outBuf)
	{
		free(outBuf);
	}
	
	if(orgPixel)
	{
		free(orgPixel);
	}
	
	return 1;
}

/*
 * Class:     com_monitorend_WVSSView
 * Method:    DecodeNal
 * Signature: ([BI[B)I
 */
JNIEXPORT jint JNICALL Java_com_monitorend_WVSSView_DecodeNal(
  JNIEnv *env, 
  jobject thiz, 
  jbyteArray in, 
  jint nalLen, 
  jbyteArray out,
  jint canvasWidth,
  jint canvasHeight
  )
{
	jbyte* nalBuf = (jbyte*)(env)->GetByteArrayElements(in, 0);
	jbyte* Pixel= (jbyte*)(env)->GetByteArrayElements(out, 0);
	
	int outLen = 0;
	bool resizeFlag = false;
	
	if (!decoder.AvcDecodeVideo(
		(unsigned char*)nalBuf+4, 
		nalLen-4, 
		outBuf, 
		(unsigned int*)&outLen, 
		(unsigned int*)&iWidth, 
		(unsigned int*)&iHeight, 
		&resizeFlag
		)
		)
	{
		return 0;
	}
	
	if(outLen > 0)
	{
		DisplayYUV_16(
		orgPixel, 
		outBuf, 
		outBuf+iWidth*iHeight, 
		outBuf+iWidth*iHeight+((iWidth*iHeight)>>2), 
		iWidth, 
		iHeight, 
		iWidth, 
		iWidth/2, 
		iWidth
		);
		
		if(canvasWidth!=oldWidth || canvasHeight!=oldHeight)
		{
			ConvertPic((unsigned char*)orgPixel, (unsigned char*)Pixel,
					 oldWidth, oldHeight, canvasWidth, canvasHeight);
		}
		else
		{
			memcpy(Pixel, orgPixel, (canvasWidth*canvasHeight)<<1);
		}
	}
	
    (env)->ReleaseByteArrayElements(in, nalBuf, 0);    
    (env)->ReleaseByteArrayElements(out, Pixel, 0); 

	return 1;	
	
}
