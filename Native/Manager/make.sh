#!/bin/bash
. ./make.env
make clean
make
mv WCCOAjava libWCCOAjava.so ../Builds
