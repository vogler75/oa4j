echo off

call vc-env_3.18.bat

echo check...

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
call "%VC_HOME%\Auxiliary\Build\vcvarsall.bat" amd64
call "%API_ROOT%\checkAPIenv.cmd"

echo set java paths...
if "%JAVA_HOME%"=="" (
  echo JAVA_HOME is not defined!
  pause
  exit
)

set API_INCL=%JAVA_HOME%\include;%JAVA_HOME%\include\win32
set API_LIB=%JAVA_HOME%\lib\jvm.lib
set PATH=%PATH%;%JAVA_HOME%\bin\;%JAVA_HOME%\jre\bin\server\

echo %OA_VERS%

echo "start visual studio..."
devenv
