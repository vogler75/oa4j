cmake_minimum_required(VERSION 3.1)

set(API_ROOT "$ENV{API_ROOT}" CACHE FILEPATH "directory of the WinCC_OA API installation")

include(${API_ROOT}/CMakeDefines.txt)
include_directories($ENV{API_INCL})

set(MANAGER_LIBS
    $ENV{API_LIB}
    ${WCCOA_LIB_PREFIX}Basics${WCCOA_LIB_INFIX}
    ${WCCOA_LIB_PREFIX}Configs${WCCOA_LIB_INFIX}
    ${WCCOA_LIB_PREFIX}Datapoint${WCCOA_LIB_INFIX}
    ${WCCOA_LIB_PREFIX}Messages${WCCOA_LIB_INFIX}
    ${WCCOA_LIB_PREFIX}Manager${WCCOA_LIB_INFIX}
)

project(Manager)

#############################################################
# WCCOAJavaManager (EXE) 
#############################################################

set(TARGET WCCOAJavaManager)

set(SOURCES
WCCOAJavaMain.cxx
WCCOAJavaResources.cxx
../LibJava/Java.cpp
)

if ( WIN32 )
  set (SOURCES ${SOURCES} VersInfo.rc)
endif()

add_manager(${TARGET} ${SOURCES})

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
../LibJava/Java.cpp
)
target_link_libraries(WCCOAJavaLibrary ${MANAGER_LIBS})
set_target_properties(WCCOAJavaLibrary PROPERTIES INSTALL_RPATH \$ORIGIN)
add_dependencies(WCCOAJavaLibrary linkedAt_lib)
set_target_properties(WCCOAJavaLibrary PROPERTIES OUTPUT_NAME WCCOAjava)