#include <WCCOAJavaDrv.hxx>
#include <WCCOAJavaHWMapper.hxx>
#include <WCCOAJavaHWService.hxx>
#include <WCCOAJavaTrans.hxx>
#include <HWObject.hxx>
#include <../LibJava/Java.hxx>

WCCOAJavaDrv* WCCOAJavaDrv::thisManager;

const bool WCCOAJavaDrv::DEBUG = false;

const char *WCCOAJavaDrv::ManagerName = "WCCOAjavadrv";

const char *WCCOAJavaDrv::ManagerClassName = "at/rocworks/oa4j/driver/JDriver";

//------------------------------------------------------------------------------------

void WCCOAJavaDrv::install_HWMapper()
{
	hwMapper = new WCCOAJavaHWMapper;
}

//--------------------------------------------------------------------------------

void WCCOAJavaDrv::install_HWService()
{
	hwService = new WCCOAJavaHWService;
}

//--------------------------------------------------------------------------------

HWObject * WCCOAJavaDrv::getHWObject() const
{
	//std::cout << "getHWObject()" << std::endl;
	return new HWObject();
}

//--------------------------------------------------------------------------------

void WCCOAJavaDrv::install_AlertService()
{
	DrvManager::install_AlertService();
}

//--------------------------------------------------------------------------------
// set java evnironment 
void WCCOAJavaDrv::javaInitialize(JNIEnv *env, jobject obj)
{
	// javap -s
	//----------------------------------------------------------------------------
	g_env = env;
	g_obj = obj;
	g_objClass = env->GetObjectClass(obj);

	//----------------------------------------------------------------------------
	javaManagerClass = env->FindClass(ManagerClassName);
	if (javaManagerClass == nil) {
		std::cout << "class " << ManagerClassName << " not found" << std::endl;
	}
}


void WCCOAJavaDrv::answer4DpId(int index, Variable* varPtr)
{
	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "answer4DpId", "(ILat/rocworks/oa4j/var/Variable;)V");
	g_env->CallVoidMethod(g_obj, jm, index, Java::convertToJava(g_env, varPtr));
	g_env->DeleteLocalRef(cls);
}

void WCCOAJavaDrv::hotLink2Internal(int index, Variable* varPtr)
{
	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "hotLink2Internal", "(ILat/rocworks/oa4j/var/Variable;)V");
	g_env->CallVoidMethod(g_obj, jm, index, Java::convertToJava(g_env, varPtr));
	g_env->DeleteLocalRef(cls);
}

PVSSboolean WCCOAJavaDrv::initialize(int argc, char *argv[])
{
	jstring str;
	jobjectArray jargv = 0;
	jargv = g_env->NewObjectArray(argc, g_env->FindClass("java/lang/String"), 0);
	for (int i = 0; i < argc; i++)
	{
		str = g_env->NewStringUTF(argv[i]);
		g_env->SetObjectArrayElement(jargv, i, str);
	}

	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "initialize", "([Ljava/lang/String;)Z");
	jboolean ret = g_env->CallBooleanMethod(g_obj, jm, jargv);
	g_env->DeleteLocalRef(cls);
	return ret ? PVSS_TRUE : PVSS_FALSE;
}

PVSSboolean WCCOAJavaDrv::start()
{
	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "start", "()Z");
	jboolean ret = g_env->CallBooleanMethod(g_obj, jm);
	g_env->DeleteLocalRef(cls);
	return ret ? PVSS_TRUE : PVSS_FALSE;
}

void WCCOAJavaDrv::stop()
{
	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "stop", "()V");
	g_env->CallBooleanMethod(g_obj, jm);
	g_env->DeleteLocalRef(cls);
}

bool WCCOAJavaDrv::workProc(HWObject *hw)
{
	int ret = PVSS_TRUE;

	//jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(g_objClass, "readData", "()Lat/rocworks/oa4j/jni/HWObject;");
	jobject jhw = g_env->CallObjectMethod(g_obj, jm);
	//g_env->DeleteLocalRef(cls);

	if (jhw == NULL) {
		return false;
	}

	/*
	public String address;
	public byte[] data;
	public TimeVar orgTime;
	public boolean timeOfPeriphFlag;
	*/
	
	jclass cls = g_env->GetObjectClass(jhw);

	// --------------------------------------------------------------------------------------------------------------
	// Address
	jstring jAddress = (jstring)g_env->GetObjectField(jhw, g_env->GetFieldID(cls, "address", "Ljava/lang/String;"));
	if (jAddress == NULL) {
		std::cout << "WCCOAJavaDrv::workProc() hw object has no address! " << std::endl;
		ret = PVSS_FALSE;
	}
	else {
		CharString *address = Java::convertJString(g_env, jAddress);
		g_env->DeleteLocalRef(jAddress);
		hw->setAddress(address->c_str());
		delete address;
	}

	// --------------------------------------------------------------------------------------------------------------
	// Data
	jbyteArray jData = (jbyteArray)g_env->GetObjectField(jhw, g_env->GetFieldID(cls, "data", "[B"));
	if (jData == NULL) {
		std::cout << "WCCOAJavaDrv::workProc() hw object has no data!" << std::endl;
		ret = PVSS_FALSE;
	} else {
		int jDataLen = g_env->GetArrayLength(jData);
		if (jDataLen > 0)
		{
			jboolean isCopy;
			jbyte* jbytes = g_env->GetByteArrayElements(jData, &isCopy);
			PVSSchar *cbytes = (PVSSchar*)new unsigned char[jDataLen];
			memcpy(cbytes, jbytes, jDataLen);
			hw->setData(cbytes);
			hw->setDlen(jDataLen);
			if (isCopy) {
				g_env->ReleaseByteArrayElements(jData, jbytes, JNI_ABORT);
			}
		}
		else {
			std::cout << "workProc: no data!" << std::endl;
		}
		g_env->DeleteLocalRef(jData);
	}

	// --------------------------------------------------------------------------------------------------------------
	// Time
	jboolean jTimeOfPeriphFlag = g_env->GetBooleanField(jhw, g_env->GetFieldID(cls, "timeOfPeriphFlag", "Z"));
	if (jTimeOfPeriphFlag) {
		jobject jOrgTime = g_env->GetObjectField(jhw, g_env->GetFieldID(cls, "orgTime", "Lat/rocworks/oa4j/var/TimeVar;"));
		if (jOrgTime == NULL){
			std::cout << "WCCOAJavaDrv::workProc() hw object has time of periph flag, but no time is given!" << std::endl;
			ret = PVSS_FALSE;
		}
		else{
			VariablePtr ptr = Java::convertJVariable(g_env, jOrgTime);
			TimeVar *tvar = (TimeVar*)ptr;
			//std::cout << "workProc timeOfPeriph: " << tvar->getValue() << std::endl;
			hw->setOrgTime(*tvar);
			hw->setTimeOfPeriphFlag();
			delete tvar;
			g_env->DeleteLocalRef(jOrgTime);
		}
	}
	else {
		hw->setOrgTime(TimeVar());  // current time
	}

	// clean up
	g_env->DeleteLocalRef(cls);
	g_env->DeleteLocalRef(jhw);

	return ret;
}

PVSSboolean WCCOAJavaDrv::writeData(HWObject *objPtr)
{
	// send the data to java
	//std::cout << "writeData: info: " << objPtr->getInfo()
	//	<< " orgTime: " << objPtr->getOrgTime()
	//	<< " orgTimeMS: " << std::to_string(objPtr->getOrgTime().getDouble())
	//	<< " statusSize: " << (objPtr->getStatus()).getSize()
	//	<< " subIndex: " << objPtr->getSubindex()
	//	<< " objSrcType: " << objPtr->getObjSrcType()
	//	<< std::endl;
	jboolean ret;

	// Time
	TimeVar orgTime = objPtr->getOrgTime();
	jobject jOrgTime = Java::convertToJava(g_env, &orgTime);

	// Status
	////BitVec status = objPtr->getStatus();

	// Subindex
	jint jSubindex = objPtr->getSubindex();

	// Data
	PVSSchar *buffer = objPtr->cutData();
	int dlen; // objPtr->getDlen()
	memcpy(&dlen, buffer, sizeof(int));
	jbyteArray jBuffer = g_env->NewByteArray(dlen);
	g_env->SetByteArrayRegion(jBuffer, 0, dlen, buffer+sizeof(int));
	delete buffer;

	// Address
	const CharString address = objPtr->getAddress();
	jstring jAddress = Java::convertToJava(g_env, address);

	// TransformationType
	TransformationType transType = objPtr->getType();
	jint jTransType = (int)transType;

	// Java Call
	//jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(g_objClass, "writeData", "(Ljava/lang/String;I[BILat/rocworks/oa4j/var/TimeVar;)Z");
	ret = g_env->CallBooleanMethod(g_obj, jm, jAddress, jTransType, jBuffer, jSubindex, jOrgTime);
	//g_env->DeleteLocalRef(cls);

	// clean up
	g_env->DeleteLocalRef(jAddress);
	g_env->DeleteLocalRef(jBuffer);
	g_env->DeleteLocalRef(jOrgTime);

	return ret ? PVSS_TRUE : PVSS_FALSE;
}

void WCCOAJavaDrv::flushHW()
{
	jmethodID jm = g_env->GetMethodID(g_objClass, "flushHW", "()V");
	g_env->CallVoidMethod(g_obj, jm);
}

void WCCOAJavaDrv::notifyDisableCommands(PVSSboolean dc)
{
	jmethodID jm = g_env->GetMethodID(g_objClass, "notifyDisableCommands", "(Z)V");
	g_env->CallVoidMethod(g_obj, jm, (jboolean)dc);
}

unsigned int WCCOAJavaDrv::getAttribs2Connect(const PeriphAddr *) const
{
	return DRVCONNMODE_VALUE | DRVCONNMODE_TIME;
}

// ------------------------------------------------------------------------------------------------------------------
// HWMapper
// ------------------------------------------------------------------------------------------------------------------

void WCCOAJavaDrv::JavaAddDpPa(DpIdentifier &dpid, PeriphAddr *confPtr)
{	
	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "addDpPa", "(Lat/rocworks/oa4j/var/DpIdentifierVar;Ljava/lang/String;B)V");

	jobject jdpid = Java::convertToJava(g_env, dpid);

	const CharString addr = confPtr->getName();
	jstring jaddr = Java::convertToJava(g_env, addr);

	jbyte jdirection = confPtr->getDirection();

	g_env->CallVoidMethod(g_obj, jm, jdpid, jaddr, jdirection);
	g_env->DeleteLocalRef(jaddr);
	g_env->DeleteLocalRef(jdpid);
	g_env->DeleteLocalRef(cls);
}

void WCCOAJavaDrv::JavaClrDpPa(DpIdentifier &dpid, PeriphAddr *confPtr)
{	
	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "clrDpPa", "(Lat/rocworks/oa4j/var/DpIdentifierVar;Ljava/lang/String;B)V");

	jobject jdpid = Java::convertToJava(g_env, dpid);

	const CharString addr = confPtr->getName();
	jstring jaddr = Java::convertToJava(g_env, addr);

	jbyte jdirection = confPtr->getDirection();

	g_env->CallVoidMethod(g_obj, jm, jdpid, jaddr, jdirection);
	g_env->DeleteLocalRef(jaddr);
	g_env->DeleteLocalRef(jdpid);
	g_env->DeleteLocalRef(cls);
}

// ------------------------------------------------------------------------------------------------------------------
// Transformation
// ------------------------------------------------------------------------------------------------------------------

jobject WCCOAJavaDrv::JavaTransformationNewObject(WCCOAJavaTrans *trans, const CharString& name, TransformationType type)
{
	jint jtype = type;

	jclass cls = g_env->GetObjectClass(g_obj);
	jmethodID jm = g_env->GetMethodID(cls, "newTransformation", "(Ljava/lang/String;I)Lat/rocworks/oa4j/jni/Transformation;");
	jstring jname = Java::convertToJava(g_env, name);
	jobject jobj = g_env->CallObjectMethod(g_obj, jm, jname, jtype);

	
	// with cptr
	//jmethodID jm = g_env->GetMethodID(cls, "newTransformation", "(JI)Lat/rocworks/oa4j/jni/Transformation;");
	//jlong jtrans = (jlong)trans;
	//jobject jobj = g_env->CallObjectMethod(g_obj, jm, jtrans, jtype);

	g_env->DeleteLocalRef(jname);
	g_env->DeleteLocalRef(cls);
	return jobj;
}

void WCCOAJavaDrv::JavaTransformationDelObject(jobject jtrans)
{
	jclass cls = g_env->GetObjectClass(jtrans);
	jmethodID jm = g_env->GetMethodID(cls, "delete", "()V");
	g_env->CallVoidMethod(jtrans, jm);
	g_env->DeleteLocalRef(jtrans);
}

int WCCOAJavaDrv::JavaTransformationGetSize(jobject jtrans)
{
	jclass cls = g_env->GetObjectClass(jtrans);
	jmethodID jm = g_env->GetMethodID(cls, "itemSize", "()I");
	jint size = g_env->CallIntMethod(jtrans, jm);
	g_env->DeleteLocalRef(cls);
	return size + sizeof(int);
}

VariableType WCCOAJavaDrv::JavaTransformationGetVariableType(jobject jtrans)
{
	jclass cls = g_env->GetObjectClass(jtrans);
	jmethodID jm = g_env->GetMethodID(cls, "getVariableTypeAsInt", "()I");
	jint type = g_env->CallIntMethod(jtrans, jm);
	g_env->DeleteLocalRef(cls);
	return VariableType(type);
}

PVSSboolean WCCOAJavaDrv::JavaTransformationToPeriph(jobject jtrans, PVSSchar *buffer, PVSSuint len, const Variable &var, const PVSSuint subix)
{
	PVSSboolean ret = PVSS_FALSE;
	VariablePtr ptr = VariablePtr(&var);
	jobject jvar = Java::convertToJava(g_env, ptr);

	jclass cls = g_env->GetObjectClass(jtrans);
	jmethodID jm = g_env->GetMethodID(cls, "toPeriph", "(ILat/rocworks/oa4j/var/Variable;I)[B");
	jbyteArray result = (jbyteArray)g_env->CallObjectMethod(jtrans, jm, len, jvar, subix);
	g_env->DeleteLocalRef(cls);
	g_env->DeleteLocalRef(jvar);

	if (result != 0)
	{
		int reslen = g_env->GetArrayLength(result);
		if (reslen == 0) { // empty data/string
			// buffer length (0)
			memcpy(buffer, &reslen, sizeof(int));
			ret = PVSS_TRUE; 
		}
		else if (reslen <= len)
		{
			// buffer length 
			memcpy(buffer, &reslen, sizeof(int));

			// buffer data
			jboolean isCopy;
			jbyte* bytes = g_env->GetByteArrayElements(result, &isCopy);
			memcpy(buffer+sizeof(reslen), bytes, reslen);
			
			if (isCopy) {
				g_env->ReleaseByteArrayElements(result, bytes, JNI_ABORT);
			}
			ret = PVSS_TRUE;
		}
		g_env->DeleteLocalRef(result);
	}

	return ret;
}

VariablePtr WCCOAJavaDrv::JavaTransformationToVar(jobject jtrans, const PVSSchar *buffer, const PVSSuint dlen, const PVSSuint subix)
{
	jclass cls = g_env->GetObjectClass(jtrans);
	jmethodID jm = g_env->GetMethodID(cls, "toVar", "([BII)Lat/rocworks/oa4j/var/Variable;");
	g_env->DeleteLocalRef(cls);

	//// data length
	//int dlen;
	//memcpy(&dlen, buffer, sizeof(int));


	jint jSubix = subix;
	jint jDlen = dlen;

	jbyteArray jBuffer = g_env->NewByteArray(jDlen);
	g_env->SetByteArrayRegion(jBuffer, 0, jDlen, buffer/*+sizeof(int)*/);

	jobject jVar = g_env->CallObjectMethod(jtrans, jm, jBuffer, jDlen, jSubix);
	g_env->DeleteLocalRef(jBuffer);

	VariablePtr var = Java::convertJVariable(g_env, jVar);
	g_env->DeleteLocalRef(jVar);

	return var;
}
