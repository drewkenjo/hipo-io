//*******************************************************
//*  INCLUDE FILE - HIPO { High Performance Output }
//*  
//*  Author : G.Gavalian   Date : 1/21/2017
//*
//*******************************************************


#ifndef __HIPOMAIN__
#define __HIPOMAIN__

#include "hipoinc.h"

bool  uncompress_LZ4(const char* buffer, int bufferLength, int decompressedLength);

bool  openHipoFile(const char* filename);
void  printRecordHeader(hipoRecordHeader_t rh);

#endif
