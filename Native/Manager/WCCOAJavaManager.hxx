/*
OA4J - WinCC Open Architecture for Java
Copyright (C) 2017 Andreas Vogler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
#ifndef  JAVAMANAGER_H
#define  JAVAMANAGER_H

#include <Manager.hxx>        
#include <DpIdentifier.hxx>   
#include <Mutex.hxx>

#include <jni.h>

#include <at_rocworks_oa4j_jni_Manager.h>

class WCCOAJavaManager : public Manager
{
public:
	// Default constructor
	WCCOAJavaManager(ManagerType manType);

	static bool DEBUG;

	static WCCOAJavaManager* thisManager;
	static void startupManager(int &argc, char *argv[], JNIEnv *env, jobject obj, ManagerType manType, jboolean connectToData, jboolean connectToEvent);

	// Java functions
	void javaInitialize(JNIEnv *env, jobject obj);
	void javaDispatch(JNIEnv *env, jobject obj, jint jsec, jint jusec);

	jint javaDpGet(JNIEnv *env, jobject obj, jobject jHdl, /*DpIdentifierVar[]*/jobjectArray dps);
	jint javaDpSet(JNIEnv *env, jobject obj, jobject jHdl, /*DpVCItem[]*/jobjectArray dps);
	jint javaDpSetTimed(JNIEnv *env, jobject obj, jobject jHdl, jobject originTime, /*DpVCItem[]*/jobjectArray dps);
	jint javaDpGetPeriod(JNIEnv *env, jobject obj, jobject jHdl, /*TimeVar*/jobject start, /*TimeVar*/jobject stop, jint num, /*DpIdentifierVar[]*/jobjectArray dps);

	jint javaDpConnect(JNIEnv *env, jobject obj, jobject jHdl, jstring dp);
	jint javaDpDisconnect(JNIEnv *env, jobject obj, jobject jHdl, jstring dp);

	jint javaDpConnect(JNIEnv *env, jobject obj, jobject jHdl, /*String[]*/jobjectArray dps);
	jint javaDpDisconnect(JNIEnv *env, jobject obj, jobject jHdl, /*String[]*/jobjectArray dps);

	jint javaDpQuery(JNIEnv *env, jobject obj, jobject jHdl, jstring jQuery);
	jint javaDpQueryConnect(JNIEnv *env, jobject obj, jobject jHdl, jboolean jValues, jstring jQuery, jboolean single);
	jint javaDpQueryDisconnect(JNIEnv *env, jobject obj, jobject jHdl);

	jint javaAlertConnect(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps);
	jint javaAlertDisconnect(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps);

	jint javaProcessHotLinkGroup(JNIEnv *env, jobject obj, jint jHdl, jlong group);

	// handle incoming hotlinks by group
	void handleHotLink(jint jHdl, const DpMsgAnswer &group);

	void handleHotLink(jint jHdl, const DpHLGroup &group);
	void handleHotLinkGroup(jint jHdl, DpHLGroup *group);

	void handleHotLink(jint jHdl, const AlertAttrList &group);

	//void handleHotLinkLogger(const DpHLGroup &group);

	void doReceive(SysMsg& sysMsg);
	void doReceive(DpMsg& dpMsg);

	//void  reduSwitchManagerStateLogic(const ManagerIdentifier &man, PVSSboolean newState);
	//void  reduSwitchConnectionStateLogic(const ManagerIdentifier &man, PVSSboolean newState);

	static PVSSboolean doExit;
	static PVSSboolean doPause;

	JNIEnv* getJNIEnv();
	jobject getJNIObj();

private:
	static void connectDataManager();
	static void connectEventManager();

	JNIEnv *g_env;
	jobject g_obj;

	static const char *ManagerName;
	static const char *ManagerClassName;
	static const char *HotLinkWaitForAnswerClassName;

	jclass javaManagerClass;
	jclass javaHotLinkWaitForAnswerClass;

	jmethodID midJavaAnswer;
	jmethodID midJavaAnswerI;
	jmethodID midJavaAnswerErr;
	jmethodID midJavaHotlink;
	jmethodID midJavaHotlinkI;
	jmethodID midJavaHotlinkGroup;
	jmethodID midJavaHotLinkGetHdlId;

	jmethodID midDoReceiveSysMsg;
	jmethodID midDoReceiveDpMsg;

	void javaSetHdlCptr(JNIEnv *env, jobject jHdl, jlong cptr);
	jlong javaGetHdlCptr(JNIEnv *env, jobject jHdl);

	void javaSetHdlCid(JNIEnv *env, jobject jHdl, jlong cid);
	jlong javaGetHdlCid(JNIEnv *env, jobject jHdl);

    virtual void signalHandler(int sig);
};

#endif
