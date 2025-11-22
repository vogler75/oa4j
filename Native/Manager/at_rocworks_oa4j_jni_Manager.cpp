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
#include <WCCOAJavaManager.hxx>
#include <WCCOAJavaResources.hxx>
#include <../LibJava/Java.hxx>
#include <at_rocworks_oa4j_jni_Manager.h>

#include <DpIdentifierVar.hxx>
#include <MsgItcDispatcher.hxx>

//------------------------------------------------------------------------------------------------
// JAVA JNI PVSS Version

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetVersion
(JNIEnv *env, jobject)
{
	return env->NewStringUTF(PVSS_VERSION);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI startup

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiStartup
(JNIEnv *env, jobject jobj, jint jtype, jobjectArray jargv, jboolean connectToData, jboolean connectToEvent, jboolean initResources, jboolean debugFlag)
{
	int len = env->GetArrayLength(jargv);
	char **argv = (char **)malloc(len * sizeof(char *));
	for (int i = 0; i < len; i++)
	{
		jobject jstr = env->GetObjectArrayElement(jargv, i);
		Java::copyJavaStringToString(env, (jstring)jstr, &argv[i]);
		env->DeleteLocalRef(jstr);
	}
  
  if (initResources) 
    WCCOAJavaResources::init(len, argv);

	if (jtype == API_MAN)
	{
		//std::cout << "Startup Java/WinCCOA API connection..." << std::endl;
		WCCOAJavaManager::startupManager(len, argv, env, jobj, API_MAN, connectToData, connectToEvent);
		//std::cout << "Startup Java/WinCCOA API connection...done " << std::endl;
		return 0;
	}
	else
	if (jtype == DB_MAN) {
		//std::cout << "Startup Java/WinCCOA DB connection..." << std::endl;
		WCCOAJavaManager::startupManager(len, argv, env, jobj, DB_MAN, connectToData, connectToEvent);
		//std::cout << "Startup Java/WinCCOA DB connection...done " << std::endl;
		return 0;
	}
	else
	{
		std::cout << "Unknown ManagerType " << jtype << std::endl;
		return -1;
	}
}

//------------------------------------------------------------------------------------------------
// JAVA JNI shutdown

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiShutdown
(JNIEnv *, jobject)
{
	Manager::closeConnection(*(WCCOAJavaManager::thisManager->getFirstConnection()));
	return 0;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dispatch


JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDispatch
(JNIEnv *env, jobject obj, jint jsec, jint jusec)
{
	WCCOAJavaManager::thisManager->javaDispatch(env, obj, jsec, jusec);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dpGet/dpSet

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpGet
(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaDpGet(env, obj, jHdl, dps);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpSet
(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaDpSet(env, obj, jHdl, dps);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpSetTimed
(JNIEnv *env, jobject obj, jobject jHdl, jobject jTimeVar, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaDpSetTimed(env, obj, jHdl, jTimeVar, dps);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dpConnect

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpConnect
(JNIEnv *env, jobject obj, jobject jHdl, jstring dp)
{
	return WCCOAJavaManager::thisManager->javaDpConnect(env, obj, jHdl, dp);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpDisconnect
(JNIEnv *env, jobject obj, jobject jHdl, jstring dp)
{
	return WCCOAJavaManager::thisManager->javaDpDisconnect(env, obj, jHdl, dp);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dpConnectArray

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpConnectArray
(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaDpConnect(env, obj, jHdl, dps);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpDisconnectArray
(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaDpDisconnect(env, obj, jHdl, dps);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI alertConnectArray

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiAlertConnect
(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaAlertConnect(env, obj, jHdl, dps);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiAlertDisconnect
(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaAlertDisconnect(env, obj, jHdl, dps);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dpQuery

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpQuery
(JNIEnv *env, jobject obj, jobject jHdl, jstring jQuery)
{
	return WCCOAJavaManager::thisManager->javaDpQuery(env, obj, jHdl, jQuery);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dpGetPeriod
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpGetPeriod
(JNIEnv *env, jobject obj, jobject jHdl, jobject tStart, jobject tStop, jint num, jobjectArray dps)
{
	return WCCOAJavaManager::thisManager->javaDpGetPeriod(env, obj, jHdl, tStart, tStop, num, dps);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI dpQueryConnect

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpQueryConnectSingle
(JNIEnv *env, jobject obj, jobject jHdl, jboolean jValues, jstring jQuery)
{
	return WCCOAJavaManager::thisManager->javaDpQueryConnect(env, obj, jHdl, jValues, jQuery, true);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpQueryConnectAll
(JNIEnv *env, jobject obj, jobject jHdl, jboolean jValues, jstring jQuery)
{
	return WCCOAJavaManager::thisManager->javaDpQueryConnect(env, obj, jHdl, jValues, jQuery, false);
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpQueryDisconnect
(JNIEnv *env, jobject obj, jobject jHdl)
{
	return WCCOAJavaManager::thisManager->javaDpQueryDisconnect(env, obj, jHdl);
}

//------------------------------------------------------------------------------------------------
// JAVA JNI GetLogPath

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetLogPath
(JNIEnv *env, jobject obj)
{
	CharString path = WCCOAJavaResources::getLogDir();
	jstring js = env->NewStringUTF(path);
	return js;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetDataPath
(JNIEnv *env, jobject obj)
{
	CharString path = WCCOAJavaResources::getDataDir();
	jstring js = env->NewStringUTF(path);
	return js;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetConfigValue
(JNIEnv *env, jobject obj, jstring jkey)
{
	CharString *key = Java::convertJString(env, jkey);
	const char *value = WCCOAJavaResources::getConfigValue(key->c_str());
	jstring js = value == 0 ? 0 : env->NewStringUTF(value);
	delete key;
	return js;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI SendArchivedDPs

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiSendArchivedDPs
(JNIEnv *env, jobject, jobject jDynVar, jboolean isAlert)
{
	TimeVar now = TimeVar();
	DynVar *dynvar = (DynVar*)Java::convertJVariable(env, jDynVar);

	DpMsgHotLink *msg = new DpMsgHotLink;
	msg->setOriginTime(now);

	//Make the first Entry with Dpidentifier = NULL and the TimeVar
	if (!isAlert)
	{
		DpHLGroup *first = new DpHLGroup();
		const DpIdentifier *empty = new DpIdentifier();
		first->appendItem(*empty, now);
		msg->appendGroup(first);
	}

	//Append the other Elements with the valid DpIdentifications
	int size = dynvar->getArrayLength();
	for (int idx = 0; idx < size; idx++)
	{
		Variable* varPtr = dynvar->getAt(idx);
		if (msg == NULL)
		{
			msg = new DpMsgHotLink();
		}
		DpHLGroup *entry = new DpHLGroup();
		DpIdentifierVar *dpIdVarPtr = ((DpIdentifierVar*)varPtr);
		std::cout << "apiSendDpMsgHotLink " << dpIdVarPtr << std::endl;
		if (!isAlert)
		{
			entry->appendItem(dpIdVarPtr->getValue(), new TimeVar(TimeVar::NullTimeVar));
		}
		else 
		{
			entry->appendItem(dpIdVarPtr->getValue(), now);
		}
		msg->appendGroup(entry);

		if ((++idx % 1024) == 0)
		{
			WCCOAJavaManager::thisManager->send(*msg, WCCOAJavaManager::dataId);
			delete msg;
			msg = NULL;
		}
	}

	//Send the last one with an empty dpidentifier
	if (msg == NULL)
	{
		msg = new DpMsgHotLink();
	}
	DpHLGroup *last = new DpHLGroup();
	const DpIdentifier *empty = new DpIdentifier();
	last->appendItem(*empty, now);

	if (!isAlert) {
		const DpIdentifier *empty = new DpIdentifier();
		last->appendItem(*empty, new TimeVar(TimeVar::NullTimeVar));
	}

	msg->appendGroup(last);

	int ret = WCCOAJavaManager::thisManager->send(*msg, WCCOAJavaManager::dataId);

	delete msg;
	return ret;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI GetIdSet

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetIdSet
(JNIEnv *env, jobject obj, jstring jpattern)
{
	jboolean patternIsCopy;
	const char *pattern = env->GetStringUTFChars(jpattern, &patternIsCopy);

	PVSSlong count;
	DpIdentifier *list;
	Manager::getIdSet(pattern, list, count);

	// create java string[]
	jclass cls = env->FindClass("java/lang/String");
	jobjectArray jarr = env->NewObjectArray(count, cls, (jobject)NULL);

	// add datapoints to java array
	CharString dpName;
	for (int i = 0; i < count; i++)
	{
		list[i].convertToString(dpName);
		jstring jstr = Java::convertToJava(env, &dpName);
		env->SetObjectArrayElement(jarr, i, jstr);
		env->DeleteLocalRef(jstr);
		//std::cout << "apiDpConnectLogger " << dpName << std::endl;
	}

	env->ReleaseStringUTFChars(jpattern, pattern);
	env->DeleteLocalRef(cls);
	delete [] list;
	//if (patternIsCopy) delete pattern;
	return jarr;
}

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetIdSetOfType
(JNIEnv *env, jobject obj, jstring jpattern, jstring jtype)
{
	jboolean patternIsCopy, typeIsCopy;
	const char *pattern = env->GetStringUTFChars(jpattern, &patternIsCopy);
	const char *type = env->GetStringUTFChars(jtype, &typeIsCopy);

	DpTypeId dptype;
	Manager::getTypeId(type, dptype);

	jclass cls = env->FindClass("java/lang/String");
	jobjectArray jarr;
	if (dptype == 0)
	{
		// create empty java string[]
		jarr = env->NewObjectArray(0, cls, (jobject)NULL);
	}
	else
	{
		PVSSlong count;
		DpIdentifier *list;
		Manager::getIdSet(pattern, list, count, dptype);

		// create java string[]		
		jarr = env->NewObjectArray(count, cls, (jobject)NULL);

		// add datapoints to java array
		CharString dpName;
		for (int i = 0; i < count; i++)
		{
			list[i].convertToString(dpName);
			jstring jstr = Java::convertToJava(env, &dpName);
			env->SetObjectArrayElement(jarr, i, jstr);
			env->DeleteLocalRef(jstr);
			//std::cout << "apiDpConnectLogger " << dpName << std::endl;
		}
		delete[] list;
	}
	env->ReleaseStringUTFChars(jpattern, pattern);
	env->ReleaseStringUTFChars(jtype, type);
	env->DeleteLocalRef(cls);
	//if (typeIsCopy) delete type; // crashes
	//if (patternIsCopy) delete pattern; // crashes
	return jarr;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI apiProcessHotlinkGroup

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiProcessHotlinkGroup
(JNIEnv *env, jobject obj, jint jHdl, jlong jGroup)
{
	return WCCOAJavaManager::thisManager->javaProcessHotLinkGroup(env, obj, jHdl, jGroup);
}

//------------------------------------------------------------------------------------------------
/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    apiDpGetComment
* Signature: (Lat/rocworks/oa4j/base/JDpId;)Lat/rocworks/oa4j/var/LangTextVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpGetComment
(JNIEnv *env, jobject obj, jobject jDpId)
{
	DpIdentifier *dpid = new DpIdentifier();
	Java::convertJDpIdentifierToDpIdentifier(env, jDpId, *dpid);
	LangTextVar *comment = new LangTextVar();
	jobject jComment = NULL;
	if (WCCOAJavaManager::thisManager->dpGetComment(*dpid, *comment) == DpIdentOK) {
		VariablePtr varptr = comment;
		jComment = Java::convertToJava(env, varptr);
	}
	delete comment;
	delete dpid;
	return jComment;
}

//------------------------------------------------------------------------------------------------

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    apiSetManagerState
* Signature: (I)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Manager_apiSetManagerState
(JNIEnv *env, jobject obj, jint state)
{
	WCCOAJavaManager::thisManager->setManagerState(static_cast<WCCOAJavaManager::ManagerState>(state));
}

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    getHostNr
* Signature: ()Z
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetConnectionState
(JNIEnv *env, jobject obj)
{
	ManagerIdentifier man(EVENT_MAN, 0);
	if (MsgItcDispatcher::instance()->getConnectionState(man))
		return 1;
	else {
		man.toggleReplica();
		if (MsgItcDispatcher::instance()->getConnectionState(man))
			return 2;
		else
			return 0;
	}
}

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    getActiveHostNr
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiIsActiveConnection
(JNIEnv *env, jobject obj)
{
	ManagerIdentifier man(EVENT_MAN, 0);
	if (MsgItcDispatcher::instance()->isActiveConnection(man))
		return 1;
	else {
		man.toggleReplica();
		if (MsgItcDispatcher::instance()->isActiveConnection(man))
			return 2;
		else
			return 0;
	}
}

//------------------------------------------------------------------------------------------------

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    checkPassword
* Signature: (Ljava/lang/String;Ljava/lang/String;)I
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_checkPassword
(JNIEnv *env, jobject obj, jstring jusername, jstring jpassword)
{
	int ret;

	if (jusername == 0) return -1; // invalid user
	if (jpassword == 0) return -2; // invalid password

	if (!WCCOAJavaManager::thisManager->isUserTableInitialized())
		WCCOAJavaManager::thisManager->initUserTable();

	CharString *username = Java::convertJString(env, jusername);
	CharString *password = Java::convertJString(env, jpassword);
	PVSSuserIdType userid = WCCOAJavaManager::thisManager->getUserId(username->c_str());
	if (userid == 65535) {
		ret = -1; // invalid user
	}
	else {
		if (!WCCOAJavaManager::thisManager->checkPassword(userid, password->c_str())) {
			ret = -2; /* wrong password*/;
		}			
		else {
			ret = 0; /*ok */			
		}
	}
	delete username;
	delete password;
	return ret;
}

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    setUserId
* Signature: (Ljava/lang/String;Ljava/lang/String;)Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_Manager_setUserId
(JNIEnv *env, jobject obj, jstring jusername, jstring jpassword)
{
	if (jusername == 0) return false;

	if (!WCCOAJavaManager::thisManager->isUserTableInitialized())
		WCCOAJavaManager::thisManager->initUserTable();

	CharString *username = Java::convertJString(env, jusername);

	CharString *password;
	const char * c_password = 0;
	if (jpassword != 0) {		
		password = Java::convertJString(env, jpassword);
		c_password = password->c_str();
	}

	PVSSuserIdType userid = WCCOAJavaManager::thisManager->getUserId(username->c_str());
	//std::cout << "setUserId: " << userid << std::endl;
	bool ret = false;
	if (userid != 65535) {
		ret = WCCOAJavaManager::thisManager->setUserId(userid, c_password);
		ret = (ret && WCCOAJavaManager::thisManager->getUserId() == userid);
	}

	delete username;
	if (jpassword != 0) 
		delete password;
	return ret;	
}
