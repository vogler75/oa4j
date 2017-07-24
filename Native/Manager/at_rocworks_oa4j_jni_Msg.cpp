#include <at_rocworks_oa4j_jni_Msg.h>
#include <WCCOAJavaManager.hxx>

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Msg_isA
(JNIEnv *, jobject, jlong cptr)
{
	Msg *v = (Msg*)(cptr);
	return v->isA();
}

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_Msg_getMsgId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	if (msg != nil)
	{
		PVSSulong id = msg->getCurrentMsgId();
		//std::cout << "getMsgId " << id << std::endl;
		return (jlong)id;
	}
	else
	{
		return 0;
	}
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Msg_forwardMsg
(JNIEnv *env, jobject obj, jint manType, jint manNum)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	if (msg != nil)
	{
		PVSSuchar xManType = PVSSuchar(manType);
		PVSSuchar xManNum = PVSSuchar(manNum);
		std::cout << "forwardMsg " << msg << " " << (*msg) << std::endl;
		ManagerIdentifier id = ManagerIdentifier(xManType, xManNum);
		msg->forwardMsg(id);
		Manager::send(*msg, id);
		std::cout << "->forwardMsg done " << msg << " " << (*msg) << std::endl;
	}
	else
	{
		std::cout << "forwardMsg null message!" << std::endl;
	}
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Msg_forwardMsgToData
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	if (msg != nil)
	{
		//std::cout << "forwardMsgToData " << msg << " " << (*msg) << std::endl;
		msg->forwardMsg(Manager::dataId);
		Manager::send(*msg, Manager::dataId);
		//std::cout << "->forwardMsgToData done " << msg << " " << (*msg) << std::endl;
	}
	else
	{
		std::cout << "forwardMsgToData null message!" << std::endl;
	}
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Msg_toString
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	stream << (*msg);
	std::string str = stream.str();	

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;	
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Msg_toDebug
(JNIEnv *env, jobject obj, jint level)
{
	jclass cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}


JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Msg_free
(JNIEnv *, jobject, jlong cptr)
{
	//std::cout << "free Msg" << std::endl;
	if ( cptr != nil ) delete (Msg*)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_Msg
* Method:    getSourceManTypeNr
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Msg_getSourceManTypeNr
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	if (msg != nil)
	{
		return msg->getSource().getManType();
	}
	else
	{
		return 0;
	}
}

/*
* Class:     at_rocworks_oa4j_jni_Msg
* Method:    getSourceManNum
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Msg_getSourceManNum
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	Msg *msg = (Msg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	if (msg != nil)
	{
		return msg->getSource().getManNum();
	}
	else
	{
		return 0;
	}
}