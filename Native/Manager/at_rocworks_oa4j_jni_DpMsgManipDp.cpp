#include <at_rocworks_oa4j_jni_DpMsgManipDp.h>
#include <DpMsgManipDp.hxx>

#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>

/*
 * Class:     at_rocworks_oa4j_jni_DpMsgManipDp
 * Method:    isDeleteDpMsg
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_isDeleteDpMsg
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;
	return msg->isDeleteDpMsg();
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgManipDp
* Method:    getDpName
* Signature: ()Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpName
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;	
	jstring jstr = env->NewStringUTF(msg->getIdentifierItemPtr()->getDatapointName());
	return jstr;
}

/*
 * Class:     at_rocworks_oa4j_jni_DpMsgManipDp
 * Method:    getDpName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;

	DpIdentifier dpid = msg->getIdentifierItemPtr()->getDpId();
	jobject jdpid = Java::convertToJava(env, dpid);
	return jdpid;
}

/*
 * Class:     at_rocworks_oa4j_jni_DpMsgManipDp
 * Method:    getDpType
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpTypeId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;
	DpTypeId type = msg->getDpType();
	return type;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgManipDp
* Method:    getDpTypeName
* Signature: ()Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpTypeName
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;
	DpTypeId type = msg->getDpType();
	CharString name;
	jstring jname = Java::getTypeName(type, name) ? Java::convertToJava(env, name) : env->NewStringUTF("");
	return jname;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_toString
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgManipDp *msg = (DpMsgManipDp*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	stream << (*msg);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_toDebug
(JNIEnv *env, jobject obj, jint level)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgManipDp *msg = (DpMsgManipDp*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}