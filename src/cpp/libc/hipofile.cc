#include <iostream>
#include <stdlib.h>
#include <hipoinc.h>
#include <fstream>
#include "hipo.h"

using namespace std;

void processRecordHeader(hipoRecordHeader_t *recordHeader);

bool openHipoFile(const char* filename){

  printf("\x1b[32m \n----> hellow hipo file.\n \x1b[0m\n");
  ifstream infile;
  infile.open(filename,ios::binary|ios::in);


  hipoFileHeader_t header;
  hipoRecordHeader_t record;

  infile.read((char *) &header, sizeof(header));
  printf("%18s : %X\n", "Signature", header.signatureString);
  printf("%18s : %X\n", "Header Size", header.versionString);
  printf("%18s : %d\n", "Unique ID", header.uniqueID);
  printf("%18s : %d\n", "File Type", header.fileType);
  printf("%18s : %d\n", "Header Length ", header.fileHeaderLength);

  infile.seekg(0,ios_base::end);

  long gcount = infile.gcount();
  long  tellg = infile.tellg();

  printf("  >>>> ifstream : gcount = %ld %ld\n",gcount,tellg);

  int position = 72;
  int counter  = 1;
  //for(int i = 0; i < 5; i++){
  while(true){

    //printf("----> position = %d\n",position);
    if(position+40>= tellg) break;

    infile.seekg(position);
    infile.read( (char *) &record, sizeof(record) );

    processRecordHeader(&record);

    printf("%3d : ",counter);printRecordHeader(record);
    counter++;

    int dataStart = position + 40 + record.indexDataLength;
    char *data = new char[record.recordDataLengthCompressed];
    infile.seekg(dataStart);
    infile.read(data,record.recordDataLengthCompressed);

    uncompress_LZ4(data,record.recordDataLengthCompressed,record.recordDataLength);

    position = position + record.recordLength;
  }
  return true;
}

void printRecordHeader(hipoRecordHeader_t rh){
  printf("RECORD (\033[32m%10X\033[0m) : SIZE = %8d, # EVENTS = \033[31m%9d\033[0m, DATA SIZE (COMP) = ( \033[35m%9d\033[0m, \033[36m%9d\033[0m), TYPE = %2d, INDEX SIZE = %6d\n",
    rh.signatureString,
    rh.recordLength,rh.numberOfEvents, rh.recordDataLength,
    rh.recordDataLengthCompressed,rh.compressionType,rh.indexDataLength);
}


void processRecordHeader(hipoRecordHeader_t *recordHeader){
  int length = recordHeader->recordDataLengthCompressed;
  int index  = recordHeader->indexDataLength;

  recordHeader->recordDataLengthCompressed = (length&0x00FFFFFF);
  recordHeader->indexDataLength = (index&0x00FFFFFF);
  recordHeader->compressionType = (length&0xAF000000)>>24;
   //printf(" data length = %d\n", (length&0x00FFFFFF));
}
