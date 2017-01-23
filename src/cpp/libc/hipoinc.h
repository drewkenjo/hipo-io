

//#include 

#ifndef __HIPO_INC__
#define __HIPO_INC__

struct hipoFileHeader_t {
  int signatureString;
  int versionString;
  int uniqueID;
  int fileType;
  int fileHeaderLength;
};
/*****************************************************
* Structure represents layout of the record header.
* all lengths are in BYTES.
******************************************************/
struct hipoRecordHeader_t {
	int signatureString; // 1) identifier string is HREC (int = 0x43455248 
	int recordLength; // 2) TOTAL Length of the RECORD, includes INDEX array
	int recordDataLength; // 3) Length of the DATA uncompressed
    int recordDataLengthCompressed; // 4) compressed length of the DATA buffer
	int numberOfEvents ; // 5) number of event, data buckets in DATA buffer
	int headerLength ; // 6) Length of the buffer represengin HEADER for the record
	int indexDataLength ; // 7) Length of the index buffer (in bytes)
	int compressionType;
};
/**
* Structure holds 
*/
struct hipoRecord_t {
	hipoRecordHeader_t  header;
	long                position;
	char*               buffer;
};

#endif
