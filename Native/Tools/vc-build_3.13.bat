echo file C:\Siemens\Automation\WinCC_OA\3.13\api\include\BCMNew\ItcDispatcher.h from 3.14 or higher needed!
echo off
call vc-env_3.13.bat

if "%VC_HOME%"=="" (
  echo VC_HOME is not defined!
  pause
  exit
)

if "%API_ROOT%"=="" (
  echo API_ROOT is not defined!
  pause
  exit
)

echo check api env...
call "%VC_HOME%\vcvarsall.bat" amd64
call "%API_ROOT%\checkAPIenv.cmd"

echo set java paths...
if "%JAVA_HOME%"=="" (
  echo JAVA_HOME is not defined!
  pause
  exit
)

set API_INCL=%API_INCL%;%JAVA_HOME%\include;%JAVA_HOME%\include\win32
set API_LIB=%API_LIB%;%JAVA_HOME%\lib\jvm.lib
set PATH=%PATH%;%JAVA_HOME%\bin\;%JAVA_HOME%\jre\bin\server\

devenv ..\Manager\WCCOAJavaManager.sln /rebuild
devenv ..\Driver\WCCOAJavaDrv.sln /rebuild
devenv ..\CtrlExt\JavaCtrlExt.sln /rebuild

call deploy.bat