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
#ifndef AVC_DEC_H_INCLUDED
#define AVC_DEC_H_INCLUDED

#include "avcdec_api.h"

class AvcDecoder
{

public:
	AvcDecoder();
    ~AvcDecoder();

    OMX_ERRORTYPE AvcDecInit();

    bool AvcDecodeVideo(unsigned char* aInputBuf, int aInBufSize,
                                unsigned char* aOutBuffer, unsigned int* aOutputLength,
                                unsigned int* aWidth, unsigned int* aHeight,
                                bool *aResizeFlag);

    OMX_ERRORTYPE AvcDecDeinit();

private:
    bool InitializeVideoDecode();

    bool FlushOutput(unsigned char* aOutBuffer, unsigned int* aOutputLength, int OldWidth, int OldHeight);

    AVCDec_Status GetNextFullNAL(uint8** aNalBuffer, int32* aNalSize, unsigned char* aInputBuf, unsigned int* aInBufSize);

public:
	AVCHandle		AvcHandle;
	AVCDecSPSInfo	SeqInfo;
	uint32			FrameSize;
	uint8*			pDpbBuffer;

private:
	uint8*			pNalBufferTemp;
	int32			NalSizeTemp;
	bool		DecodeSliceFlag;
};


#endif	//#ifndef AVC_DEC_H_INCLUDED

