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

project(Manager)

#############################################################
# WCCOAjava (EXE)
#############################################################

set(TARGET WCCOAjava)

set(SOURCES
WCCOAJavaMain.cxx
WCCOAJavaResources.cxx
../LibJava/Java.cpp
)

if ( WIN32 )
  set (SOURCES ${SOURCES} VersInfo.rc)
endif()

add_manager(${TARGET} ${SOURCES})
target_link_libraries(${TARGET} ${JVM_LIB})

#############################################################
# WCCOAJavaLibrary (DLL) 
#############################################################

add_library(WCCOAJavaLibrary SHARED
WCCOAJavaMain.cxx
WCCOAJavaManager.cxx
WCCOAJavaResources.cxx
WCCOAJavaAlertHotLinkWaitForAnswer.cpp
WCCOAJavaHotLinkWaitForAnswer.cpp
at_rocworks_oa4j_jni_AnswerGroup.cpp
at_rocworks_oa4j_jni_DpIdValueList.cpp
at_rocworks_oa4j_jni_DpMsgAnswer.cpp
at_rocworks_oa4j_jni_DpMsgComplexVC.cpp
at_rocworks_oa4j_jni_DpMsgHotLink.cpp
at_rocworks_oa4j_jni_DpMsgManipDp.cpp
at_rocworks_oa4j_jni_DpMsgRequest.cpp
at_rocworks_oa4j_jni_DpMsgValueChange.cpp
at_rocworks_oa4j_jni_DpVCGroup.cpp
at_rocworks_oa4j_jni_DpVCItem.cpp
at_rocworks_oa4j_jni_Manager.cpp
at_rocworks_oa4j_jni_Msg.cpp
at_rocworks_oa4j_jni_RequestGroup.cpp
at_rocworks_oa4j_jni_RequestItem.cpp
at_rocworks_oa4j_jni_SysMsg.cpp
at_rocworks_oa4j_jni_Cns.cpp
../LibJava/Java.cpp
../LibJava/JNIValidator.cpp
)

target_link_libraries(WCCOAJavaLibrary ${MANAGER_LIBS} ${JVM_LIB})
set_target_properties(WCCOAJavaLibrary PROPERTIES INSTALL_RPATH \$ORIGIN)
add_dependencies(WCCOAJavaLibrary linkedAt_lib)
set_target_properties(WCCOAJavaLibrary PROPERTIES OUTPUT_NAME WCCOAjava)