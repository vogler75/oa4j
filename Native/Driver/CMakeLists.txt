cmake_minimum_required(VERSION 3.1)

set(API_ROOT "$ENV{API_ROOT}" CACHE FILEPATH "directory of the WinCC_OA API installation")

include(${API_ROOT}/CMakeDefines.txt)

# Add Java include directories
include_directories($ENV{JAVA_HOME}/include)
include_directories($ENV{JAVA_HOME}/include/linux)

# Find JVM library
find_library(JVM_LIB jvm PATHS $ENV{JAVA_HOME}/lib $ENV{JAVA_HOME}/jre/lib NO_DEFAULT_PATH PATH_SUFFIXES server)
if(NOT JVM_LIB)
  message(FATAL_ERROR "Could not find libjvm.so in JAVA_HOME: $ENV{JAVA_HOME}")
endif()

project(Driver)

#############################################################
# WCCOAJavaDriver (EXE)
#############################################################

set(TARGET WCCOAjavadrv)

set(SOURCES
WCCOAJavaMain.cxx
WCCOAJavaAlertService.cxx
WCCOAJavaConnection.cxx
WCCOAJavaDrv.cxx
WCCOAJavaDrvIntDp.cxx
WCCOAJavaHWMapper.cxx
WCCOAJavaHWService.cxx
WCCOAJavaResources.cxx
WCCOAJavaTrans.cxx
at_rocworks_oa4j_jni_Driver.cpp
../LibJava/Java.cpp
../LibJava/JNIValidator.cpp
)

if ( WIN32 )
  set (SOURCES ${SOURCES} VersInfo.rc)
endif()

add_driver(${TARGET} ${SOURCES})
target_link_libraries(${TARGET} ${JVM_LIB})

#############################################################
# WCCOAJavaDriverLibrary (DLL)
#############################################################

add_library(WCCOAJavaDriverLibrary SHARED
WCCOAJavaMain.cxx
WCCOAJavaAlertService.cxx
WCCOAJavaConnection.cxx
WCCOAJavaDrv.cxx
WCCOAJavaDrvIntDp.cxx
WCCOAJavaHWMapper.cxx
WCCOAJavaHWService.cxx
WCCOAJavaResources.cxx
WCCOAJavaTrans.cxx
at_rocworks_oa4j_jni_Driver.cpp
../LibJava/Java.cpp
../LibJava/JNIValidator.cpp
)

target_link_libraries(WCCOAJavaDriverLibrary ${WCCOA_LIB_PREFIX}ComDrv ${WCCOA_LIB_PREFIX}V24 ${MANAGER_LIBS} ${JVM_LIB})
set_target_properties(WCCOAJavaDriverLibrary PROPERTIES INSTALL_RPATH \$ORIGIN)
add_dependencies(WCCOAJavaDriverLibrary linkedAt_lib)
set_target_properties(WCCOAJavaDriverLibrary PROPERTIES OUTPUT_NAME WCCOAjavadrv)
