#include <iostream>
#include <stdlib.h>
#include <hipoinc.h>
#include <fstream>
#include "hipo.h"

#ifdef __LZ4__
#include "lz4.h"
#endif

using namespace std;

bool uncompress_LZ4(const char* buffer, int bufferLength, int decompressedLength){

#ifdef __LZ4__
  //printf("decompressing -> %d  %d\n",bufferLength,decompressedLength);
  char *destUnCompressed = new char[decompressedLength];
  int result = LZ4_decompress_safe(buffer,destUnCompressed,bufferLength,decompressedLength);  
  //printf(" FIRST (%d) = %x %x %x %x\n",result,destUnCompressed[0],destUnCompressed[1],
  //  destUnCompressed[2],destUnCompressed[3]);  
  //LZ4_decompress_fast(buffer,destUnCompressed,decompressedLength);  
  //LZ4_uncompress(buffer,destUnCompressed,decompressedLength);
#endif

#ifndef __LZ4__
  printf("\n   >>>>> LZ4 compression is not supported.");
  printf("\n   >>>>> check if libz4 is installed on your system.");  
  printf("\n   >>>>> recompile the library with liblz4 installed.\n");  
#endif  
  return true;
}

