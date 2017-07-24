#!/bin/bash
. ./make.env
make clean
make
mv WCCOAjavadrv libWCCOAjavadrv.so ../Builds
