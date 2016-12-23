#include <iostream>
#include <stdlib.h>
#include <hipoinc.h>
#include <fstream>

using namespace std;

bool open_hipo_File(char* filename){ 
  printf("\x1b[32m \n----> hellow hipo file.\n \x1b[0m\n");
  ifstream infile;
  infile.open(filename,ios::binary|ios::in);
  hipoFileHeader_t header;  
  infile.read((char *) &header, sizeof(header));
  printf("%12s : %X\n", "Signature", header.signatureString);
  printf("%12s : %d\n", "Header Size", header.fileHeaderLength);
  return true;
}
