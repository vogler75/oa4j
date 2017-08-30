#!/bin/bash

if [ ! -d "../Builds" ]; then
  mkdir ../Builds
fi 

if [ "$API_ROOT" = "" ]
then
  echo "env var \$API_ROOT is not set"
  exit 1
fi

. ./make.env
make clean
make
mv WCCOAjavadrv libWCCOAjavadrv.so ../Builds
