@echo off
set ARG=%1

if "%ARG%x" == "x" (
  echo "usage: make exe or dll"
  exit /b
)

if "%JAVA_HOME%x" == "x" (
  echo "JAVA_HOME is not set!"
  exit /b
)

echo set APIS_ROOT=%SRCROOT%|sed s/\\/\//g > srcroot.cmd
call srcroot.cmd

if "%APIS_ROOT%x" == "x" (
  echo "APIS_ROOT is not set! [API source root]"
  echo "e.g.: APIS_ROOT=d:/Workspace/<root>/src [use slashes "/" and not backslashes!]'"
  exit /b
)

if "%PLATFORM%x" == "x" (
  echo "PLATFORM is not set!"
  echo "go to root\src and call senv [e.g. D:\Workspace\<root>\src>senv]"
  exit /b
)

IF NOT EXIST jni.h copy /y "%JAVA_HOME%\include\jni.h" jni.h
IF NOT EXIST jawt_md.h copy /y "%JAVA_HOME%\include\win32\jawt_md.h" jawt_md.h
IF NOT EXIST jni_md.h copy /y "%JAVA_HOME%\include\win32\jni_md.h" jni_md.h

if "%ARG%x" == "exex" (
  gmk -f MakefileExe.gmk
)
if "%ARG%x" == "dllx" (
  gmk -f MakefileDLL.gmk
)