#include <../LibJava/Java.hxx>

#include <WCCOAJavaManager.hxx>
#include <WCCOAJavaResources.hxx>
#include <HotLinkWaitForAnswer.hxx>   
#include <StartDpInitSysMsg.hxx>    
#include <DpIdentifierVar.hxx>
#include <DpMsgAnswer.hxx>            
#include <DpMsgHotLink.hxx>           
#include <DpHLGroup.hxx>              
#include <DpVCItem.hxx>               
#include <ErrHdl.hxx>                
#include <ErrClass.hxx>              
#include <AnswerItem.hxx>
#include <Variable.hxx>
#include <FloatVar.hxx>
#include <IntegerVar.hxx>
#include <UIntegerVar.hxx>
#include <CharVar.hxx>
#include <ULongVar.hxx>
#include <TimeVar.hxx>
#include <AnyTypeVar.hxx>
#include <InitSysMsg.hxx>
#include <signal.h>
#include <jni.h>

#include <WCCOAJavaHotLinkWaitForAnswer.hxx>
#include <WCCOAJavaAlertHotLinkWaitForAnswer.hxx>

WCCOAJavaManager* WCCOAJavaManager::thisManager;

const bool WCCOAJavaManager::DEBUG = false;

const char *WCCOAJavaManager::ManagerName = "WCCOAjava";

const char *WCCOAJavaManager::ManagerClassName = "at/rocworks/oa4j/base/JManager";
const char *WCCOAJavaManager::HotLinkWaitForAnswerClassName = "at/rocworks/oa4j/base/JHotLinkWaitForAnswer";

//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
//
// Manager class
//
//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------

PVSSboolean WCCOAJavaManager::doExit = PVSS_FALSE;
PVSSboolean WCCOAJavaManager::doPause = PVSS_FALSE;

//--------------------------------------------------------------------------------
// The constructor defines Manager type (API_MAN, DB_MAN) and Manager number

WCCOAJavaManager::WCCOAJavaManager(ManagerType manType)
	: Manager(ManagerIdentifier(manType, Resources::getManNum()))
{
}

//------------------------------------------------------------------------------------------------

void WCCOAJavaManager::startupManager(int &argc, char *argv[], JNIEnv *env, jobject obj, ManagerType manType, jboolean connectToData, jboolean connectToEvent)
{
	// Now run our manager
	thisManager = new WCCOAJavaManager(manType);
	thisManager->g_env = env;
	thisManager->g_obj = obj;
	thisManager->javaInitialize(env, obj);
	if (connectToData) thisManager->connectDataManager();
	if (connectToEvent) thisManager->connectEventManager();
	thisManager->g_env = nil;
	thisManager->g_obj = nil;	
}

//--------------------------------------------------------------------------------
// set java evnironment 
void WCCOAJavaManager::javaInitialize(JNIEnv *env, jobject obj){
	// javap -s

	//----------------------------------------------------------------------------
	javaManagerClass = env->FindClass(ManagerClassName);
	if (javaManagerClass == nil) {
		std::cout << "class " << ManagerClassName << " not found" << std::endl;
		return;
	}

	midJavaHotlink = env->GetMethodID(javaManagerClass, "callbackHotlink", "(IILat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;)I");
	if (midJavaHotlink == nil) {
		std::string msg = "mid callbackHotlink not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", msg.c_str());
		return;
	}

	midJavaHotlinkI = env->GetMethodID(javaManagerClass, "callbackHotlink", "(II)I");
	if (midJavaHotlinkI == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for callbackHotlinkI not found"));
		return;
	}

	midJavaHotlinkGroup = env->GetMethodID(javaManagerClass, "callbackHotlinkGroup", "(IJ)I");
	if (midJavaHotlinkGroup == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for callbackHotlinkGroup not found"));
		return;
	}

	midJavaAnswer = env->GetMethodID(javaManagerClass, "callbackAnswer", "(IILat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;)I");
	if (midJavaAnswer == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for callbackAnswer not found"));
		return;
	}

	midJavaAnswerI = env->GetMethodID(javaManagerClass, "callbackAnswer", "(II)I");
	if (midJavaAnswerI == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for callbackAnswerI not found"));
		return;
	}

	midJavaAnswerErr = env->GetMethodID(javaManagerClass, "callbackAnswerError", "(IILjava/lang/String;)I");
	if (midJavaAnswerErr == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for callbackAnswerErr not found"));
		return;
	}

	midDoReceiveSysMsg = env->GetMethodID(javaManagerClass, "doReceiveSysMsg", "(J)Z");
	if (midJavaAnswerI == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for doReceiveSysMsg not found"));
		return;
	}

	midDoReceiveDpMsg = env->GetMethodID(javaManagerClass, "doReceiveDpMsg", "(J)Z");
	if (midJavaAnswerI == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for doReceiveDpMsg not found"));
		return;
	}

	//----------------------------------------------------------------------------
	//javaLoggerClass = env->FindClass(LoggerClassName);
	//if (javaLoggerClass == nil) {
	//	std::string msg = "class " + std::string(LoggerClassName) + " not found";
	//	ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
	//		ManagerName, "javaInitialize", msg.c_str());
	//	return;
	//}

	//midJavaHotlinkLogger = env->GetMethodID(javaLoggerClass, "callbackLogger", "(Ljava/lang/String;DD)I");
	//if (midJavaHotlinkLogger == nil) {
	//	ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
	//		ManagerName, "javaInitialize", CharString("mid for callbackLogger not found"));
	//	return;
	//}

	//----------------------------------------------------------------------------
	javaHotLinkWaitForAnswerClass = env->FindClass(HotLinkWaitForAnswerClassName);
	if (javaHotLinkWaitForAnswerClass == nil) {
		std::string msg = "class " + std::string(HotLinkWaitForAnswerClassName) + " not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", msg.c_str());
		return;
	}

	midJavaHotLinkGetHdlId = env->GetMethodID(javaHotLinkWaitForAnswerClass, "getHdlId", "()I");
	if (midJavaHotLinkGetHdlId == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			ManagerName, "javaInitialize", CharString("mid for getHdlId not found"));
		return;
	}

	//std::cout << "javaInitialize...done" << std::endl;
}

//--------------------------------------------------------------------------------

void WCCOAJavaManager::connectDataManager()
{
	long sec, usec;

	// First connect to Data manager.
	// We want Typecontainer and Identification so we can resolve names
	// This call succeeds or the manager will exit
	//std::cout << "connect to data manager..." << std::endl;
	connectToData(StartDpInitSysMsg::TYPE_CONTAINER | StartDpInitSysMsg::DP_IDENTIFICATION);

	// While we are in STATE_INIT we are initialized by the Data manager
	while (getManagerState() == STATE_INIT)
	{
		// Wait max. 1 second in select to receive next message from data.
		// It won't take that long...
		sec = 1;
		usec = 0;

		dispatch(sec, usec);
	}

	// Request for the DM to send me the internal DPS	
	SysMsg newMsg = SysMsg(Manager::dataId, START_MANAGER);
	thisManager->send(newMsg, Manager::dataId);
	//std::cout << "connect to data manager...done" << std::endl;
}

void WCCOAJavaManager::connectEventManager()
{
	//std::cout << "connect to event manager..." << std::endl;
	// We are now in STATE_ADJUST and can connect to Event manager
	// This call will succeed or the manager will exit
	connectToEvent();
	//std::cout << "connect to event manager...done" << std::endl;
}

//--------------------------------------------------------------------------------
// Receive Signals.
// We are interested in SIGINT and SIGTERM.

void WCCOAJavaManager::signalHandler(int sig)
{
	Manager::signalHandler(sig);
}

//--------------------------------------------------------------------------------

void WCCOAJavaManager::doReceive(SysMsg &sysMsg)
{
	if ( DEBUG ) std::cout << "doReceiveSysMsg..." << std::endl;
	jboolean handled = false;
	if (g_env == nil || g_obj == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE, "doReceiveSysMsg without java env!");
	}
	else
	{				
		handled = g_env->CallBooleanMethod(g_obj, midDoReceiveSysMsg, (jlong)(&sysMsg));
	}

	if (!handled) Manager::doReceive(sysMsg);
}

void WCCOAJavaManager::doReceive(DpMsg& dpMsg)
{
	if (DEBUG) std::cout << "doReceiveDpMsg..." << std::endl;
	jboolean handled = false;
	if (g_env == nil || g_obj == nil) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE, "doReceiveDpMsg without java env!");
	} 
	else 
	{
		handled = g_env->CallBooleanMethod(g_obj, midDoReceiveDpMsg, (jlong)(&dpMsg));		
	}

	if (!handled) Manager::doReceive(dpMsg);
}

/*
void  WCCOAJavaManager::reduSwitchManagerStateLogic(const ManagerIdentifier &man, PVSSboolean newState)
{
	std::cout << "reduSwitchManagerStateLogic " << man << " newState=" << newState << std::endl;
	Manager::reduSwitchManagerStateLogic(man, newState);
}

void  WCCOAJavaManager::reduSwitchConnectionStateLogic(const ManagerIdentifier &man, PVSSboolean newState)
{
	std::cout << "reduSwitchManagerStateLogic " << man << " newState=" << newState << std::endl;
	Manager::reduSwitchConnectionStateLogic(man, newState);
}
*/

//--------------------------------------------------------------------------------

void WCCOAJavaManager::handleHotLink(jint jHdl, const DpMsgAnswer &answer)
{
	JDpIdentifierClass cdpid(g_env);
	JVariableClass cvar(g_env);

	g_env->CallIntMethod(g_obj, midJavaAnswerI, jHdl, -1); // START
	for (AnswerGroup *group = answer.getFirstGroup(); group; group = answer.getNextGroup())
	{		
		ErrClass *err = group->getErrorPtr();
		if (err != 0) {
			jstring jErrText = (jstring)Java::convertToJava(g_env, err->getErrorText());
			g_env->CallIntMethod(g_obj, midJavaAnswerErr, jHdl, err->getErrorId(), jErrText);
			g_env->DeleteLocalRef(jErrText);
			//std::cout << "handleHotLink error=" << err->getErrorId() <<"::" << err->getErrorText() << std::endl;
		}
		int i = -1;
		for (AnswerItem *item = group->getFirstItem(); item; item = group->getNextItem())
		{
			if (item->getType() == AnswerItem::EVENT)
			{
				if (item != NULL) {
					jobject objDpId = Java::convertToJava(g_env, item->getDpIdentifier(), &cdpid);
					jobject objVar = Java::convertToJava(g_env, item->getValuePtr(), &cdpid, &cvar);

					// create Variable object	
					if (objDpId != NULL && objVar != NULL)
					{
						if (DEBUG) std::cout << "handleHotlink " << item->getDpIdentifier() << "/" << objVar << std::endl;
						g_env->CallIntMethod(g_obj, midJavaAnswer, jHdl, ++i, objDpId, objVar);
					}

					if (objDpId != NULL) g_env->DeleteLocalRef(objDpId);
					if (objVar != NULL) g_env->DeleteLocalRef(objVar);
				}
			} 
		}
	}
	g_env->CallIntMethod(g_obj, midJavaAnswerI, jHdl, -2); // END
}

//--------------------------------------------------------------------------------

void WCCOAJavaManager::handleHotLink(jint jHdl, const DpHLGroup &group)
{
	JDpIdentifierClass cdpid(g_env);
	JVariableClass cvar(g_env);

	int i = -1;
	g_env->CallIntMethod(g_obj, midJavaHotlinkI, jHdl, -1); // START
	for (DpVCItem *item = group.getFirstItem(); item; item = group.getNextItem())
	{
		if (item != NULL) {
			DpIdentifier dpid = item->getDpIdentifier();
			VariablePtr var = item->getValuePtr();

			jobject objDpId = Java::convertToJava(g_env, dpid, &cdpid);
			jobject objVar = Java::convertToJava(g_env, var, &cdpid, &cvar);

			//std::cerr << "Receiving HotLink " << item->getDpIdentifier().toString() << std::endl;
			//std::cerr << "Receiving HotLink " << item->getValuePtr()->formatValue() << std::endl;			

			// create Variable object	
			if (objDpId != NULL && objVar != NULL)
			{
				g_env->CallIntMethod(g_obj, midJavaHotlink, jHdl, ++i, objDpId, objVar);
			}

			if (objDpId != NULL) g_env->DeleteLocalRef(objDpId);
			if (objVar != NULL) g_env->DeleteLocalRef(objVar);
		}
	}
	g_env->CallIntMethod(g_obj, midJavaHotlinkI, jHdl, -2); // END
}

//--------------------------------------------------------------------------------
// NOT USED

void WCCOAJavaManager::handleHotLinkGroup(jint jHdl, DpHLGroup *group)
{
	g_env->CallIntMethod(g_obj, midJavaHotlinkGroup, jHdl, (jlong)group);
}

jint WCCOAJavaManager::javaProcessHotLinkGroup(JNIEnv *env, jobject obj, jint jHdl, jlong jGroup)
{
	DpHLGroup *groupPtr = (DpHLGroup*)jGroup;
	const DpHLGroup &group = *groupPtr;
	handleHotLink(jHdl, group);
	delete groupPtr;
	return 0;
}

//--------------------------------------------------------------------------------

void WCCOAJavaManager::handleHotLink(jint jHdl, const AlertAttrList &group)
{
	//std::cerr << "Receiving Alert HotLink " << group.getATime().toString() << " items=" << group.getNumberOfItems() << std::endl;

	JDpIdentifierClass cdpid(g_env);
	JVariableClass cvar(g_env);

	int i = -1;
	g_env->CallIntMethod(g_obj, midJavaHotlinkI, jHdl, -1); // START
	
	for (DpVCItem *item = group.getFirstItem(); item; item = group.getNextItem())
	{
		if (item != NULL) {
			DpIdentifier dpid = item->getDpIdentifier();
			VariablePtr var = item->getValuePtr();

			jobject objDpId = Java::convertToJava(g_env, dpid, &cdpid);
			jobject objVar = Java::convertToJava(g_env, var, &cdpid, &cvar);

			//std::cerr << "  dpId " << item->getDpIdentifier().toString() << " value " << item->getValuePtr()->formatValue() << std::endl;			

			// create Variable object	
			if (objDpId != NULL && objVar != NULL)
			{
				g_env->CallIntMethod(g_obj, midJavaHotlink, jHdl, ++i, objDpId, objVar);
			}

			if (objDpId != NULL) g_env->DeleteLocalRef(objDpId);
			if (objVar != NULL) g_env->DeleteLocalRef(objVar);
		}
	}
	g_env->CallIntMethod(g_obj, midJavaHotlinkI, jHdl, -2); // END
}

//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
//
// Java Interface Function 
//
//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------

//--------------------------------------------------------------------------------
// dispatch called from java
void WCCOAJavaManager::javaDispatch(JNIEnv *env, jobject obj, jint sec, jint usec)
{
	g_env = env;
	g_obj = obj;
	long l_sec = sec;
	long l_usec = usec;
	dispatch(l_sec, l_usec);
	g_env = nil;
	g_obj = nil;
}

//--------------------------------------------------------------------------------
// dpConnect Single Datapoint

jint WCCOAJavaManager::javaDpConnect(JNIEnv *env, jobject obj, jobject jHdl, jstring dp)
{	
	CharString *dpName = Java::convertJString(env, dp);
	DpIdentifier dpId;   // DP to connect to
	int ret;
	if (Java::getId(*dpName, dpId) == PVSS_FALSE)
	{
		ErrHdl::error(ErrClass::PRIO_SEVERE,      
			ErrClass::ERR_PARAM,       
			ErrClass::UNEXPECTEDSTATE, 
			ManagerName,             
			"javaDpConnect",                     
			CharString("Datapoint ") + (*dpName) + CharString(" missing"));
		ret=-1;
	}
	else
	{
		jlong cptr = javaGetHdlCptr(env, jHdl);
		if (cptr==0) {
			// create new callback 
			WCCOAJavaHotLinkWaitForAnswer *wait;
			wait = new WCCOAJavaHotLinkWaitForAnswer;
			wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
			javaSetHdlCptr(env, jHdl, (jlong)wait);
			ret=Manager::dpConnect(dpId, wait) ? 0 : -2;
		}
		else {
			// use existing callback
			ret=Manager::dpConnect(dpId, (WCCOAJavaHotLinkWaitForAnswer*)cptr) ? 0 : -3;
		}
	}
	delete dpName;
	return ret;
}

jint WCCOAJavaManager::javaDpDisconnect(JNIEnv *env, jobject obj, jobject jHdl, jstring dp)
{
	CharString *dpName = Java::convertJString(env, dp);
	DpIdentifier dpId;   // DP to connect to
	int ret;
	if (Java::getId(*dpName, dpId) == PVSS_FALSE)
	{
		// This name was unknown.
		ErrHdl::error(ErrClass::PRIO_SEVERE,      // It is a severe error
			ErrClass::ERR_PARAM,        // wrong name: blame others
			ErrClass::UNEXPECTEDSTATE,  // fits all
			ManagerName,              // our file name
			"javaDpDisconnect",                      // our function name
			CharString("Datapoint ") + (*dpName) +
			CharString(" missing"));
		ret = -1;
	}
	else
	{
		jlong cptr = javaGetHdlCptr(env, jHdl);
		ret = (Manager::dpDisconnect(dpId, (WCCOAJavaHotLinkWaitForAnswer*)cptr)) ? 0 : -2;
		javaSetHdlCptr(env, jHdl, 0);
	}
	delete dpName;
	return ret;
}

//--------------------------------------------------------------------------------
// dpConnect Array of Datapoints

jint WCCOAJavaManager::javaDpConnect(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	DpIdentList *dpList;
	dpList = Java::convertJArrayOfStringToDpIdentList(env, dps);
	int ret;
	if (dpList->getNumberOfItems() == 0)
		ret=-1;
	else
	{
		WCCOAJavaHotLinkWaitForAnswer *cptr = (WCCOAJavaHotLinkWaitForAnswer *)javaGetHdlCptr(env, jHdl);
		if (cptr == NULL) {
			// create new callback 
			WCCOAJavaHotLinkWaitForAnswer *wait;
			wait = new WCCOAJavaHotLinkWaitForAnswer;
			wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
			javaSetHdlCptr(env, jHdl, (jlong)wait);
			ret = Manager::dpConnect(*dpList, wait) ? 0 : -2;

		}
		else
		{
			// use existing callback 
			ret = Manager::dpConnect(*dpList, cptr) ? 0 : -3;
		}		
	}
	delete dpList;
	return ret;
}

jint WCCOAJavaManager::javaDpDisconnect(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	DpIdentList *dpList;
	dpList = Java::convertJArrayOfStringToDpIdentList(env, dps);
	int ret;
	if (dpList->getNumberOfItems() <= 0)
		ret = -1;
	else
	{
		jlong cptr = javaGetHdlCptr(env, jHdl);
		ret = (Manager::dpDisconnect(*dpList, (WCCOAJavaHotLinkWaitForAnswer*)cptr)) ? 0 : -2;
		javaSetHdlCptr(env, jHdl, 0);

	}
	delete dpList;
	return ret;
}

//--------------------------------------------------------------------------------
// dpGet / dpSet

jint WCCOAJavaManager::javaDpGet(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	int ret;
	DpIdentList *dpList;
	dpList = Java::convertJArrayOfDpIdentifierToDpIdentList(env, dps);
	if (dpList->getNumberOfItems() > 0)
	{
		// Our Callback object
		WCCOAJavaHotLinkWaitForAnswer *wait;
		wait = new WCCOAJavaHotLinkWaitForAnswer;
		if (DEBUG) std::cout << "javaDpGet jHdl=" << jHdl << std::endl;
		wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
		javaSetHdlCptr(env, jHdl, (jlong)wait);
		ret = Manager::dpGet(*dpList, wait) ? 0 : -1;
	}
	else ret = -2;
	delete dpList;
	return ret;
}

jint WCCOAJavaManager::javaDpSet(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	int ret;
	DpIdValueList *dpList;
	dpList = Java::convertJArrayOfDpVCItemToDpIdValueList(env, dps);
	if (dpList->getNumberOfItems() > 0)
	{
		// Our Callback object
		WCCOAJavaHotLinkWaitForAnswer *wait;
		wait = new WCCOAJavaHotLinkWaitForAnswer;
		if (DEBUG) std::cout << "javaDpSet jHdl=" << jHdl << std::endl;
		wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
		javaSetHdlCptr(env, jHdl, (jlong)wait);
		ret = Manager::dpSet(*dpList, wait) ? 0 : -1;
	}
	else ret = -2;
	delete dpList;
	return ret;
}

jint WCCOAJavaManager::javaDpSetTimed(JNIEnv *env, jobject obj, jobject jHdl, jobject jOriginTimeVar, jobjectArray dps)
{
	int ret;
	DpIdValueList *dpList;
	dpList = Java::convertJArrayOfDpVCItemToDpIdValueList(env, dps);
	if (dpList->getNumberOfItems() > 0)
	{
		VariablePtr varptr = Java::convertJVariable(env, jOriginTimeVar);
		if (varptr->isA() != TIME_VAR)
		{
			ErrHdl::error(ErrClass::PRIO_SEVERE,
				ErrClass::ERR_PARAM,
				ErrClass::UNEXPECTEDSTATE,
				ManagerName,
				"javaDpSetTimed",
				CharString("originTimeVar is not of type TimeVar"));
			return -3;
		} else {
			TimeVar *originTimeVar = (TimeVar*)varptr;
			// Our Callback object
			WCCOAJavaHotLinkWaitForAnswer *wait;
			wait = new WCCOAJavaHotLinkWaitForAnswer;
			if (DEBUG) std::cout << "javaDpSetTimed jHdl=" << jHdl << std::endl;
			wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
			javaSetHdlCptr(env, jHdl, (jlong)wait);
			ret = Manager::dpSetTimed(*originTimeVar, *dpList, wait) ? 0 : -1;
		}
	}
	else ret = -2;
	delete dpList;
	return ret;
}

//--------------------------------------------------------------------------------
// dpGetPeriod

jint WCCOAJavaManager::javaDpGetPeriod(JNIEnv *env, jobject obj, jobject jHdl, /*TimeVar*/jobject start, /*TimeVar*/jobject stop, jint num, /*DpIdentifierVar[]*/jobjectArray dps)
{
	int ret;
	TimeVar *tStart = (TimeVar*)Java::convertJVariable(env, start);
	TimeVar *tStop = (TimeVar*)Java::convertJVariable(env, stop);
	DpIdentList *dpList = Java::convertJArrayOfDpIdentifierToDpIdentList(env, dps);

	if (dpList->getNumberOfItems() > 0)
	{
		// Our Callback object
		WCCOAJavaHotLinkWaitForAnswer *wait;
		wait = new WCCOAJavaHotLinkWaitForAnswer;
		if (DEBUG) std::cout << "javaDpGetPeriod jHdl=" << jHdl << std::endl;
		wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
		javaSetHdlCptr(env, jHdl, (jlong)wait);
		ret = Manager::dpGetPeriod(*tStart, *tStop, (PVSSushort)num, *dpList, wait) ? 0 : -1;
	}
	else ret = -2;
	delete dpList;
	delete tStop;
	delete tStart;
	return ret;
}

//--------------------------------------------------------------------------------
// dpQuery

jint WCCOAJavaManager::javaDpQuery(JNIEnv *env, jobject obj, jobject jHdl, jstring jQuery)
{
	int ret;

	CharString *query = Java::convertJString(env, jQuery);
	PVSSulong queryId;

	//WCCOAJavaHotlinkWaitForAnswer *cptr = (WCCOAJavaHotlinkWaitForAnswer *)javaGetHdlCptr(env, jHdl);

	// create new callback 
	WCCOAJavaHotLinkWaitForAnswer *wait;
	wait = new WCCOAJavaHotLinkWaitForAnswer;
	wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
	javaSetHdlCptr(env, jHdl, (jlong)wait);

	if (DEBUG) std::cout << "javaDpQuery=" << *query << std::endl;
	ret = Manager::dpQuery(*query, queryId, wait) ? 0 : -1;

	if (DEBUG) std::cout << "javaDpQuery ret: " << ret << " id: " << queryId << std::endl;
	javaSetHdlCid(env, jHdl, (jlong)queryId);

	delete query;
	return ret;
}


//--------------------------------------------------------------------------------
// dpQueryConnect

jint WCCOAJavaManager::javaDpQueryConnect(JNIEnv *env, jobject obj, jobject jHdl, jboolean jValues, jstring jQuery, jboolean single)
{
	int ret;

	CharString *query = Java::convertJString(env, jQuery);
	PVSSulong queryId;
	PVSSboolean values = jValues;

	WCCOAJavaHotLinkWaitForAnswer *cptr = (WCCOAJavaHotLinkWaitForAnswer *)javaGetHdlCptr(env, jHdl);
	if (cptr == NULL) {
		// create new callback 
		WCCOAJavaHotLinkWaitForAnswer *wait;
		wait = new WCCOAJavaHotLinkWaitForAnswer;
		wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
		javaSetHdlCptr(env, jHdl, (jlong)wait);

		if (single) {
			if (DEBUG) std::cout << "javaDpQueryConnectSingle=" << *query << std::endl;
			ret = Manager::dpQueryConnectSingle(*query, queryId, values, wait) ? 0 : -1;
		}
		else {
			if (DEBUG) std::cout << "javaDpQueryConnectAll=" << *query << std::endl;
			ret = Manager::dpQueryConnectAll(*query, queryId, values, wait) ? 0 : -1;
		}
		if (DEBUG) std::cout << "javaDpQueryConnect ret: " << ret << " id: " << queryId << std::endl;
		javaSetHdlCid(env, jHdl, (jlong)queryId);
	}
	else
	{
		// use existing callback 		
		if (single) {
			if (DEBUG) std::cout << "javaDpQueryConnectSingle=" << *query << std::endl;
			ret = Manager::dpQueryConnectSingle(*query, queryId, values, cptr) ? 0 : -1;
		}
		else {
			if (DEBUG) std::cout << "javaDpQueryConnectAll=" << *query << std::endl;
			ret = Manager::dpQueryConnectAll(*query, queryId, values, cptr) ? 0 : -1;
		}
		if (DEBUG) std::cout << "javaDpQueryConnect ret: " << ret << " id: " << queryId << std::endl;
		javaSetHdlCid(env, jHdl, (jlong)queryId);
	}

	delete query;
	return ret;
}

jint WCCOAJavaManager::javaDpQueryDisconnect(JNIEnv *env, jobject obj, jobject jHdl)
{
	jlong cptr = javaGetHdlCptr(env, jHdl);
	PVSSulong queryId = (PVSSulong)javaGetHdlCid(env, jHdl);
	int ret = (Manager::dpQueryDisconnect(queryId, (WCCOAJavaHotLinkWaitForAnswer*)cptr)) ? 0 : -1;
	javaSetHdlCptr(env, jHdl, 0);
	javaSetHdlCid(env, jHdl, 0);
	return ret;
}

//--------------------------------------------------------------------------------
// alertConnect 

jint WCCOAJavaManager::javaAlertConnect(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	DpIdentList *dpList;
	dpList = Java::convertJArrayOfStringToDpIdentList(env, dps);
	int ret;
	if (dpList->getNumberOfItems() == 0)
		ret = -1;
	else
	{
		WCCOAJavaAlertHotLinkWaitForAnswer *cptr = (WCCOAJavaAlertHotLinkWaitForAnswer *)javaGetHdlCptr(env, jHdl);
		if (cptr == NULL) {
			// create new callback 
			WCCOAJavaAlertHotLinkWaitForAnswer *wait;
			wait = new WCCOAJavaAlertHotLinkWaitForAnswer;
			wait->setjHdl(env->CallIntMethod(jHdl, midJavaHotLinkGetHdlId));
			javaSetHdlCptr(env, jHdl, (jlong)wait);
			ret = Manager::alertConnect(*dpList, wait) ? 0 : -2;
		}
		else
		{
			// use existing callback 
			ret = Manager::alertConnect(*dpList, cptr) ? 0 : -3;
		}
	}
	delete dpList;
	return ret;
}

jint WCCOAJavaManager::javaAlertDisconnect(JNIEnv *env, jobject obj, jobject jHdl, jobjectArray dps)
{
	DpIdentList *dpList;
	dpList = Java::convertJArrayOfStringToDpIdentList(env, dps);
	int ret;
	if (dpList->getNumberOfItems() <= 0)
		ret = -1;
	else
	{
		jlong cptr = javaGetHdlCptr(env, jHdl);
		ret = (Manager::alertDisconnect(*dpList, (WCCOAJavaAlertHotLinkWaitForAnswer*)cptr)) ? 0 : -2;
		javaSetHdlCptr(env, jHdl, 0);

	}
	delete dpList;
	return ret;
}

//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
//
// Set/Get Java Object Values 
//
//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------

JNIEnv* WCCOAJavaManager::getJNIEnv()
{
	return g_env;
}
jobject WCCOAJavaManager::getJNIObj()
{
	return g_obj;
}

void WCCOAJavaManager::javaSetHdlCptr(JNIEnv *env, jobject jHdl, jlong cptr)
{
	if (DEBUG) std::cout << "javaSetHdlCptr=" << cptr << std::endl;
	jclass cls = env->FindClass("at/rocworks/oa4j/base/JHotLinkWaitForAnswer");
	jmethodID jm = env->GetMethodID(cls, "setCPtr", "(J)V");
	env->CallVoidMethod(jHdl, jm, cptr);
	env->DeleteLocalRef(cls);
}

jlong WCCOAJavaManager::javaGetHdlCptr(JNIEnv *env, jobject jHdl)
{
	jclass cls = env->FindClass("at/rocworks/oa4j/base/JHotLinkWaitForAnswer");
	jmethodID jm = env->GetMethodID(cls, "getCPtr", "()J");
	jlong cptr = env->CallLongMethod(jHdl, jm);
	env->DeleteLocalRef(cls);

	if (DEBUG) std::cout << "javaGetHdlCptr=" << cptr << std::endl;
	return cptr;
}

void WCCOAJavaManager::javaSetHdlCid(JNIEnv *env, jobject jHdl, jlong cid)
{
	if (DEBUG) std::cout << "javaSetHdlCid=" << cid << std::endl;
	jclass cls = env->FindClass("at/rocworks/oa4j/base/JHotLinkWaitForAnswer");
	jmethodID jm = env->GetMethodID(cls, "setCId", "(J)V");
	env->CallVoidMethod(jHdl, jm, cid);
	env->DeleteLocalRef(cls);
}

jlong WCCOAJavaManager::javaGetHdlCid(JNIEnv *env, jobject jHdl)
{
	jclass cls = env->FindClass("at/rocworks/oa4j/base/JHotLinkWaitForAnswer");
	jmethodID jm = env->GetMethodID(cls, "getCId", "()J");
	jlong cid = env->CallLongMethod(jHdl, jm);
	if (DEBUG) std::cout << "javaGetHdlCid=" << cid << std::endl;
	env->DeleteLocalRef(cls);
	return cid;
}  