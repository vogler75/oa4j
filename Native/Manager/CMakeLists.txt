cmake_minimum_required(VERSION 3.1)

set(API_ROOT "$ENV{API_ROOT}" CACHE FILEPATH "directory of the WinCC_OA API installation")

include(${API_ROOT}/CMakeDefines.txt)
include_directories($ENV{API_INCL})

project(Manager)

set(TARGET WCCOAManager)

set(SOURCES
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
../LibJava/Java.cpp
)

if ( WIN32 )
  set (SOURCES ${SOURCES} VersInfo.rc)
endif()

add_manager(${TARGET} ${SOURCES})
