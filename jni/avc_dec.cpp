/* ------------------------------------------------------------------
 * Copyright (C) 2008 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */

#include "avc_dec.h"
#include "avcdec_int.h"


/*************************************/
/* functions needed for video engine */
/*************************************/

/* These two functions are for callback functions of AvcHandle */
int32 CBAVC_Malloc_OMX(void* aUserData, int32 aSize, int32 aAttribute)
{
    OSCL_UNUSED_ARG(aUserData);
    OSCL_UNUSED_ARG(aAttribute);
    void* pPtr;

    pPtr = oscl_malloc(aSize);
    return (int32) pPtr;
}

void CBAVC_Free_OMX(void* aUserData, int32 aMem)
{
    OSCL_UNUSED_ARG(aUserData);
    oscl_free((uint8*) aMem);
}

void UnbindBuffer_OMX(void* aUserData, int32 i)
{
	OSCL_UNUSED_ARG(aUserData);
	OSCL_UNUSED_ARG(i);
	return;
}

int32 AllocateBuffer(void* aUserData, int32 i, uint8** aYuvBuffer)
{
    AvcDecoder * dec = (AvcDecoder*)aUserData;
    *aYuvBuffer = dec->pDpbBuffer + i * dec->FrameSize;
    return 1;
}

int32 ActivateSPS(void* aUserData, uint aSizeInMbs, uint aNumBuffers)
{
	AvcDecoder * dec = (AvcDecoder*)aUserData;
    PVAVCDecGetSeqInfo(&(dec->AvcHandle), &(dec->SeqInfo));

    if (dec->pDpbBuffer)
    {
        oscl_free(dec->pDpbBuffer);
        dec->pDpbBuffer = NULL;
    }

    dec->FrameSize = (aSizeInMbs << 7) * 3;
    dec->pDpbBuffer = (uint8*) oscl_malloc(aNumBuffers * (dec->FrameSize));
    return 1;

}



//////////////////////////////////////////////////////////////////////////

AvcDecoder::AvcDecoder()
: pDpbBuffer(NULL)
, FrameSize(0)
{
	oscl_memset(&AvcHandle, 0, sizeof(AvcHandle));
	oscl_memset(&SeqInfo, 0, sizeof(SeqInfo)); 
}

AvcDecoder::~AvcDecoder()
{

}

/* initialize video decoder */
bool AvcDecoder::InitializeVideoDecode()
{
    /* Initialize AvcHandle */
    AvcHandle.AVCObject = NULL;
    AvcHandle.userData = this;
    AvcHandle.CBAVC_DPBAlloc = ActivateSPS;
    AvcHandle.CBAVC_FrameBind = AllocateBuffer;
    AvcHandle.CBAVC_FrameUnbind = UnbindBuffer_OMX;
    AvcHandle.CBAVC_Malloc = CBAVC_Malloc_OMX;
    AvcHandle.CBAVC_Free = CBAVC_Free_OMX;

    return true;
}

bool AvcDecoder::FlushOutput(unsigned char* aOutBuffer, unsigned int* aOutputLength, int OldWidth, int OldHeight)
{
    AVCFrameIO Output;
    AVCDec_Status Status;
    int32 Index, Release, FrameSize;
    int OldFrameSize = ((OldWidth + 15) & (~15)) * ((OldHeight + 15) & (~15));

    Output.YCbCr[0] = Output.YCbCr[1] = Output.YCbCr[2] = NULL;
    Status = PVAVCDecGetOutput(&(AvcHandle), (int*)(&Index), (int*)(&Release), &Output);

    if (Status == AVCDEC_FAIL)
    {
        return false;
    }

    *aOutputLength = 0; // init to 0

    if (Output.YCbCr[0])
    {
        FrameSize = Output.pitch * Output.height;
        // it should not happen that the frame size is smaller than available buffer size, but check just in case
        if (FrameSize <= OldFrameSize)
        {
            *aOutputLength = (Output.pitch * Output.height * 3) >> 1;

            oscl_memcpy(aOutBuffer, Output.YCbCr[0], FrameSize);
            oscl_memcpy(aOutBuffer + FrameSize, Output.YCbCr[1], FrameSize >> 2);
            oscl_memcpy(aOutBuffer + FrameSize + FrameSize / 4, Output.YCbCr[2], FrameSize >> 2);
        }
        // else, the frame length is reported as zero, and there is no copying
    }


    return true;
}


/* Initialization routine */
OMX_ERRORTYPE AvcDecoder::AvcDecInit()
{
    if (false == InitializeVideoDecode())
    {
        return OMX_ErrorInsufficientResources;
    }

    DecodeSliceFlag = false;
    pNalBufferTemp = NULL;

    return OMX_ErrorNone;
}


/*Decode routine */
bool AvcDecoder::AvcDecodeVideo(unsigned char* aInputBuf, int aInBufSize,        
		unsigned char* aOutBuffer, unsigned int* aOutputLength,
        unsigned int* aWidth, unsigned int* aHeight,
        bool *aResizeFlag)
{
    AVCDec_Status Status;
    int Width, Height;
    int crop_top, crop_bottom, crop_right, crop_left;
    int32 NalType, NalRefId, PicType;
    AVCDecObject* pDecVid;

    *aResizeFlag = false;
    unsigned int OldWidth, OldHeight;

    OldWidth = 	*aWidth;
    OldHeight = *aHeight;


    if (true == DecodeSliceFlag)
    {
        Status = (AVCDec_Status) FlushOutput(aOutBuffer, aOutputLength, OldWidth, OldHeight);

        if ((Status = PVAVCDecodeSlice(&(AvcHandle), pNalBufferTemp, NalSizeTemp)) == AVCDEC_PICTURE_OUTPUT_READY)
        {
            DecodeSliceFlag = true;
        }
        else
        {
            if (pNalBufferTemp)
                oscl_free(pNalBufferTemp);
            pNalBufferTemp = NULL;
            DecodeSliceFlag = false;
        }

        return true;
    }

    if (AVCDEC_FAIL == PVAVCDecGetNALType(aInputBuf, aInBufSize, (int*)(&NalType), (int*)(&NalRefId)))
    {
        return false;
    }

    if (AVC_NALTYPE_SPS == (AVCNalUnitType)NalType)
    {
        if (PVAVCDecSeqParamSet(&(AvcHandle), aInputBuf, aInBufSize) != AVCDEC_SUCCESS)
        {
            return false;
        }

        pDecVid = (AVCDecObject*) AvcHandle.AVCObject;

        Width = (pDecVid->seqParams[0]->pic_width_in_mbs_minus1 + 1) * 16;
        Height = (pDecVid->seqParams[0]->pic_height_in_map_units_minus1 + 1) * 16;

        if (pDecVid->seqParams[0]->frame_cropping_flag)
        {
            crop_left = 2 * pDecVid->seqParams[0]->frame_crop_left_offset;
            crop_right = Width - (2 * pDecVid->seqParams[0]->frame_crop_right_offset + 1);

            if (pDecVid->seqParams[0]->frame_mbs_only_flag)
            {
                crop_top = 2 * pDecVid->seqParams[0]->frame_crop_top_offset;
                crop_bottom = Height - (2 * pDecVid->seqParams[0]->frame_crop_bottom_offset + 1);
            }
            else
            {
                crop_top = 4 * pDecVid->seqParams[0]->frame_crop_top_offset;
                crop_bottom = Height - (4 * pDecVid->seqParams[0]->frame_crop_bottom_offset + 1);
            }
        }
        else  /* no cropping flag, just give the first and last pixel */
        {
            crop_bottom = Height - 1;
            crop_right = Width - 1;
            crop_top = crop_left = 0;
        }

        *aWidth = crop_right - crop_left + 1;
        *aHeight = crop_bottom - crop_top + 1;

        //if( (OldWidth != aPortParam->format.video.nFrameWidth) || (OldHeight !=	aPortParam->format.video.nFrameHeight))
        // FORCE RESIZE ALWAYS FOR SPS
        *aResizeFlag = true;
    }

    else if (AVC_NALTYPE_PPS == (AVCNalUnitType) NalType)
    {
        if (PVAVCDecPicParamSet(&(AvcHandle), aInputBuf, aInBufSize) != AVCDEC_SUCCESS)
        {
            return false;
        }
    }

    else if (AVC_NALTYPE_SLICE == (AVCNalUnitType) NalType ||
             AVC_NALTYPE_IDR == (AVCNalUnitType) NalType)
    {
        if ((Status = PVAVCDecodeSlice(&(AvcHandle), aInputBuf, aInBufSize)) == AVCDEC_PICTURE_OUTPUT_READY)
        {
            Status = (AVCDec_Status) FlushOutput(aOutBuffer, aOutputLength, OldWidth, OldHeight);

            if ((Status = PVAVCDecodeSlice(&(AvcHandle), aInputBuf, aInBufSize)) == AVCDEC_PICTURE_OUTPUT_READY)
            {
                pNalBufferTemp = (uint8*) oscl_malloc(aInBufSize);
                oscl_memcpy(pNalBufferTemp, aInputBuf, aInBufSize);
                NalSizeTemp = aInBufSize;
                DecodeSliceFlag = true;

            }
        }

        if (Status == AVCDEC_PICTURE_READY)
        {
			Status = (AVCDec_Status) FlushOutput(aOutBuffer, aOutputLength, OldWidth, OldHeight);
        }

		if (Status == AVCDEC_FAIL)
		{
			return false;
		}
    }

    else if ((AVCNalUnitType)NalType == AVC_NALTYPE_SEI)
    {
        if (PVAVCDecSEI(&(AvcHandle), aInputBuf, aInBufSize) != AVCDEC_SUCCESS)
        {
            return false;
        }
    }

    else if ((AVCNalUnitType)NalType == AVC_NALTYPE_AUD)
    {
        PicType = aInputBuf[1] >> 5;
    }

    else if ((AVCNalUnitType)NalType == AVC_NALTYPE_EOSTREAM) // end of stream
    {
        return true;
    }

    else
    {
        printf("\nNAL_type = %d, unsupported nal type or not sure what to do for this type\n", NalType);
    }
    return true;

}


OMX_ERRORTYPE AvcDecoder::AvcDecDeinit()
{
	PVAVCCleanUpDecoder(&AvcHandle);

    if (pDpbBuffer)
    {
        oscl_free(pDpbBuffer);
        pDpbBuffer = NULL;
    }

    return OMX_ErrorNone;
}


AVCDec_Status AvcDecoder::GetNextFullNAL(uint8** aNalBuffer, int32* aNalSize, unsigned char* aInputBuf, unsigned int* aInBufSize)
{
    uint32 BuffConsumed;
    uint8* pBuff = aInputBuf;
    unsigned int InputSize;

    *aNalSize = *aInBufSize;
    InputSize = *aInBufSize;

    AVCDec_Status ret_val = PVAVCAnnexBGetNALUnit(pBuff, aNalBuffer, (int*)aNalSize);

    if (ret_val == AVCDEC_FAIL)
    {
        return AVCDEC_FAIL;
    }

    BuffConsumed = ((*aNalSize) + (int32)(*aNalBuffer - pBuff));
    aInputBuf += BuffConsumed;
    *aInBufSize = InputSize - BuffConsumed;

    return AVCDEC_SUCCESS;
}
