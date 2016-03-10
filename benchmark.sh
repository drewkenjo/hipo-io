#!/bin/sh

java -cp "target/hipo-1.0.jar" org.jlab.hipo.benchmark.HipoBenchmark write testHipo.hipo $*
java -cp "target/hipo-1.0.jar" org.jlab.hipo.benchmark.HipoBenchmark read  testHipo.hipo 

java -cp "target/hipo-1.0.jar" org.jlab.hipo.benchmark.HipoBenchmark write -gzip testHipo.hipo $*
java -cp "target/hipo-1.0.jar" org.jlab.hipo.benchmark.HipoBenchmark read  testHipo.hipo 

java -cp "target/hipo-1.0.jar" org.jlab.hipo.benchmark.HipoBenchmark write -lz4 testHipo.hipo $*
java -cp "target/hipo-1.0.jar" org.jlab.hipo.benchmark.HipoBenchmark read  testHipo.hipo 
