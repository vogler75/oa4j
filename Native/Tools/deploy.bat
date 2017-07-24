mkdir ..\Builds\%OA_VERS%-Win64\
copy ..\Manager\bin\WCCOAjava.dll   ..\Builds\%OA_VERS%-Win64\
copy ..\Manager\bin\WCCOAjava.exe   ..\Builds\%OA_VERS%-Win64\
copy ..\Driver\bin\WCCOAjavadrv.dll ..\Builds\%OA_VERS%-Win64\
copy ..\Driver\bin\WCCOAjavadrv.exe ..\Builds\%OA_VERS%-Win64\
copy ..\CtrlExt\bin\JavaCtrlExt.dll ..\Builds\%OA_VERS%-Win64\
del ..\Builds\%OA_VERS%-Win64.zip
zip -j ..\Builds\%OA_VERS%-Win64.zip ..\Builds\%OA_VERS%-Win64\*