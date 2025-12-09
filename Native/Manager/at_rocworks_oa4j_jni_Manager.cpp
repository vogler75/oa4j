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
#include <../LibJava/JNIValidator.hxx>
#include <at_rocworks_oa4j_jni_Manager.h>

#include <DpIdentifierVar.hxx>
#include <MsgItcDispatcher.hxx>
#include <DpTypeDefinition.hxx>

#include <vector>

//------------------------------------------------------------------------------------------------
// JAVA JNI PVSS Version

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetVersion
(JNIEnv *env, jclass)
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
		// Clean up argv strings and array
		for (int i = 0; i < len; i++) {
			free(argv[i]);
		}
		free(argv);
		return 0;
	}
	else
	if (jtype == DB_MAN) {
		//std::cout << "Startup Java/WinCCOA DB connection..." << std::endl;
		WCCOAJavaManager::startupManager(len, argv, env, jobj, DB_MAN, connectToData, connectToEvent);
		//std::cout << "Startup Java/WinCCOA DB connection...done " << std::endl;
		// Clean up argv strings and array
		for (int i = 0; i < len; i++) {
			free(argv[i]);
		}
		free(argv);
		return 0;
	}
	else
	{
		std::cout << "Unknown ManagerType " << jtype << std::endl;
		// Clean up argv strings and array
		for (int i = 0; i < len; i++) {
			free(argv[i]);
		}
		free(argv);
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

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiGetSystemNum
(JNIEnv *env, jobject obj, jstring jSystemName)
{
	return (jint)Java::parseSystemNum(env, jSystemName);
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

//------------------------------------------------------------------------------------------------
// JAVA JNI logging

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Manager_apiLog
(JNIEnv *env, jobject obj, jint jprio, jlong jstate, jstring jtext)
{
	// Validate parameters
	if (!JNIValidator::validatePriority(jprio)) {
		return;
	}
	if (!JNIValidator::validateErrorCode(jstate)) {
		return;
	}
	if (!JNIValidator::validateJString(env, jtext, "jtext")) {
		return;
	}

	// Convert and log
	CharString *text = Java::convertJString(env, jtext);
	if (text != NULL) {
		ErrHdl::error((ErrClass::ErrPrio)jprio, ErrClass::ERR_IMPL, (ErrClass::ErrCode)jstate, text->c_str());
		delete text;
	} else {
		JNIValidator::reportValidationError("jtext", "Failed to convert jstring");
	}
}

//------------------------------------------------------------------------------------------------
// Helper function to recursively convert DpTypeDefinition to Java DpTypeElement

static jobject convertDpTypeDefinitionToJava(JNIEnv *env, const DpTypeDefinition *def,
	jclass clsDpTypeElement, jmethodID midDpTypeElementInit, jmethodID midAddChild)
{
	if (def == NULL) {
		return NULL;
	}

	// Get element properties
	const CharString& name = def->getName();
	DpElementId elementId = def->getId();
	DpElementType elementType = def->getType();
	DpTypeId refTypeId = def->getReference();

	// Create Java String for the name
	jstring jname = env->NewStringUTF(name.c_str());

	// Create the DpTypeElement object
	jobject jElement = env->NewObject(clsDpTypeElement, midDpTypeElementInit,
		jname, (jint)elementId, (jint)elementType, (jint)refTypeId);

	env->DeleteLocalRef(jname);

	if (jElement == NULL) {
		return NULL;
	}

	// Recursively add children
	const DpTypeDefinition::DpTypeDefinitions& children = def->getChildren();
	for (size_t i = 0; i < children.size(); i++) {
		jobject jChild = convertDpTypeDefinitionToJava(env, children[i],
			clsDpTypeElement, midDpTypeElementInit, midAddChild);
		if (jChild != NULL) {
			env->CallVoidMethod(jElement, midAddChild, jChild);
			env->DeleteLocalRef(jChild);
		}
	}

	return jElement;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI apiDpTypeGet

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    apiDpTypeGet
* Signature: (Ljava/lang/String;Z)Lat/rocworks/oa4j/var/DpTypeElement;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpTypeGet
(JNIEnv *env, jobject obj, jstring jtypeName, jboolean includeTypeRef)
{
	if (jtypeName == NULL) {
		return NULL;
	}

	// Convert Java string to CharString
	CharString *typeName = Java::convertJString(env, jtypeName);
	if (typeName == NULL) {
		return NULL;
	}

	// Get the type ID from the type name
	DpTypeId typeId;
	if (!Manager::getTypeId(typeName->c_str(), typeId)) {
		delete typeName;
		return NULL;
	}
	delete typeName;

	// Get the DpTypeDefinition
	DpTypeDefinition *typeDef = Manager::dpTypeGet(typeId, includeTypeRef);
	if (typeDef == NULL) {
		return NULL;
	}

	// Find the DpTypeElement class and its constructor
	jclass clsDpTypeElement = env->FindClass("at/rocworks/oa4j/var/DpTypeElement");
	if (clsDpTypeElement == NULL) {
		delete typeDef;
		return NULL;
	}

	// Get the constructor: DpTypeElement(String name, int elementId, DpElementType type, int refTypeId)
	// We'll use a constructor that takes primitive int for type and create DpElementType later
	jmethodID midInit = env->GetMethodID(clsDpTypeElement, "<init>", "(Ljava/lang/String;ILat/rocworks/oa4j/var/DpElementType;I)V");
	if (midInit == NULL) {
		// Try alternative constructor signature
		env->ExceptionClear();
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeElement);
		return NULL;
	}

	// Get the addChild method
	jmethodID midAddChild = env->GetMethodID(clsDpTypeElement, "addChild", "(Lat/rocworks/oa4j/var/DpTypeElement;)V");
	if (midAddChild == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeElement);
		return NULL;
	}

	// Get DpElementType class and fromValue method
	jclass clsDpElementType = env->FindClass("at/rocworks/oa4j/var/DpElementType");
	if (clsDpElementType == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeElement);
		return NULL;
	}

	jmethodID midFromValue = env->GetStaticMethodID(clsDpElementType, "fromValue", "(I)Lat/rocworks/oa4j/var/DpElementType;");
	if (midFromValue == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeElement);
		env->DeleteLocalRef(clsDpElementType);
		return NULL;
	}

	// We need a modified helper that creates DpElementType from int
	// Let's create the root element manually and recurse

	// Helper lambda to convert recursively
	struct Converter {
		JNIEnv *env;
		jclass clsDpTypeElement;
		jclass clsDpElementType;
		jmethodID midInit;
		jmethodID midAddChild;
		jmethodID midFromValue;

		jobject convert(const DpTypeDefinition *def) {
			if (def == NULL) return NULL;

			// Get element properties
			const CharString& name = def->getName();
			DpElementId elementId = def->getId();
			DpElementType elementType = def->getType();
			DpTypeId refTypeId = def->getReference();

			// Create Java String for the name
			jstring jname = env->NewStringUTF(name.c_str());

			// Create DpElementType enum value from int
			jobject jElementType = env->CallStaticObjectMethod(clsDpElementType, midFromValue, (jint)elementType);
			if (jElementType == NULL) {
				env->DeleteLocalRef(jname);
				return NULL;
			}

			// Create the DpTypeElement object
			jobject jElement = env->NewObject(clsDpTypeElement, midInit,
				jname, (jint)elementId, jElementType, (jint)refTypeId);

			env->DeleteLocalRef(jname);
			env->DeleteLocalRef(jElementType);

			if (jElement == NULL) {
				return NULL;
			}

			// Recursively add children
			const DpTypeDefinition::DpTypeDefinitions& children = def->getChildren();
			for (size_t i = 0; i < children.size(); i++) {
				jobject jChild = convert(children[i]);
				if (jChild != NULL) {
					env->CallVoidMethod(jElement, midAddChild, jChild);
					env->DeleteLocalRef(jChild);
				}
			}

			return jElement;
		}
	};

	Converter converter = {env, clsDpTypeElement, clsDpElementType, midInit, midAddChild, midFromValue};
	jobject result = converter.convert(typeDef);

	// Cleanup
	delete typeDef;
	env->DeleteLocalRef(clsDpTypeElement);
	env->DeleteLocalRef(clsDpElementType);

	return result;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI apiDpTypeGetFlat
// Returns element names and types organized by hierarchy level (dyn_dyn_string, dyn_dyn_int)

/*
* Class:     at_rocworks_oa4j_jni_Manager
* Method:    apiDpTypeGetFlat
* Signature: (Ljava/lang/String;Z)Lat/rocworks/oa4j/var/DpTypeResult;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiDpTypeGetFlat
(JNIEnv *env, jobject obj, jstring jtypeName, jboolean includeSubTypes)
{
	if (jtypeName == NULL) {
		return NULL;
	}

	// Convert Java string to CharString
	CharString *typeName = Java::convertJString(env, jtypeName);
	if (typeName == NULL) {
		return NULL;
	}

	// Get the type ID from the type name
	DpTypeId typeId;
	if (!Manager::getTypeId(typeName->c_str(), typeId)) {
		delete typeName;
		return NULL;
	}
	delete typeName;

	// Get the DpTypeDefinition
	DpTypeDefinition *typeDef = Manager::dpTypeGet(typeId, includeSubTypes);
	if (typeDef == NULL) {
		return NULL;
	}

	// Find the DpTypeResult class and its constructor
	jclass clsDpTypeResult = env->FindClass("at/rocworks/oa4j/var/DpTypeResult");
	if (clsDpTypeResult == NULL) {
		delete typeDef;
		return NULL;
	}

	// Get the default constructor
	jmethodID midResultInit = env->GetMethodID(clsDpTypeResult, "<init>", "()V");
	if (midResultInit == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeResult);
		return NULL;
	}

	// Get the addLevel method
	jmethodID midAddLevel = env->GetMethodID(clsDpTypeResult, "addLevel", "(Ljava/util/List;Ljava/util/List;)V");
	if (midAddLevel == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeResult);
		return NULL;
	}

	// Get ArrayList class and methods
	jclass clsArrayList = env->FindClass("java/util/ArrayList");
	if (clsArrayList == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeResult);
		return NULL;
	}

	jmethodID midArrayListInit = env->GetMethodID(clsArrayList, "<init>", "()V");
	jmethodID midArrayListAdd = env->GetMethodID(clsArrayList, "add", "(Ljava/lang/Object;)Z");
	if (midArrayListInit == NULL || midArrayListAdd == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeResult);
		env->DeleteLocalRef(clsArrayList);
		return NULL;
	}

	// Get Integer class for boxing
	jclass clsInteger = env->FindClass("java/lang/Integer");
	jmethodID midIntegerValueOf = env->GetStaticMethodID(clsInteger, "valueOf", "(I)Ljava/lang/Integer;");
	if (clsInteger == NULL || midIntegerValueOf == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeResult);
		env->DeleteLocalRef(clsArrayList);
		return NULL;
	}

	// Create the DpTypeResult object
	jobject jResult = env->NewObject(clsDpTypeResult, midResultInit);
	if (jResult == NULL) {
		delete typeDef;
		env->DeleteLocalRef(clsDpTypeResult);
		env->DeleteLocalRef(clsArrayList);
		env->DeleteLocalRef(clsInteger);
		return NULL;
	}

	// Traverse the type definition tree level by level using BFS
	std::vector<const DpTypeDefinition*> currentLevel;
	std::vector<const DpTypeDefinition*> nextLevel;
	CharString prefix;

	// Start with root
	currentLevel.push_back(typeDef);

	while (!currentLevel.empty()) {
		// Create lists for this level
		jobject jElementList = env->NewObject(clsArrayList, midArrayListInit);
		jobject jTypeList = env->NewObject(clsArrayList, midArrayListInit);

		for (size_t i = 0; i < currentLevel.size(); i++) {
			const DpTypeDefinition *node = currentLevel[i];

			// Build element path
			CharString elementPath;
			// For root level, just use the name
			// For other levels, we need to build the full path
			elementPath = node->getName();

			// Add element name
			jstring jElementName = env->NewStringUTF(elementPath.c_str());
			env->CallBooleanMethod(jElementList, midArrayListAdd, jElementName);
			env->DeleteLocalRef(jElementName);

			// Add element type
			jobject jType = env->CallStaticObjectMethod(clsInteger, midIntegerValueOf, (jint)node->getType());
			env->CallBooleanMethod(jTypeList, midArrayListAdd, jType);
			env->DeleteLocalRef(jType);

			// Add children to next level
			const DpTypeDefinition::DpTypeDefinitions& children = node->getChildren();
			for (size_t j = 0; j < children.size(); j++) {
				nextLevel.push_back(children[j]);
			}
		}

		// Add this level to result
		env->CallVoidMethod(jResult, midAddLevel, jElementList, jTypeList);
		env->DeleteLocalRef(jElementList);
		env->DeleteLocalRef(jTypeList);

		// Move to next level
		currentLevel = nextLevel;
		nextLevel.clear();
	}

	// Cleanup
	delete typeDef;
	env->DeleteLocalRef(clsDpTypeResult);
	env->DeleteLocalRef(clsArrayList);
	env->DeleteLocalRef(clsInteger);

	return jResult;
}
