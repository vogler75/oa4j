#include <at_rocworks_oa4j_jni_RequestItem.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>
#include <DpMsgRequest.hxx>

/*
* Class:     at_rocworks_oa4j_jni_RequestItem
* Method:    getId
* Signature: ()Lat/rocworks/oa4j/var/DpIdentifierVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestItem_getId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	RequestItem *item = (RequestItem*)cptr;
	const DpIdentifier dpid = item->getId();
	//std::cout << "Java_at_rocworks_oa4j_jni_RequestItem_getId: " << dpid << std::endl;

	jobject jobj = Java::convertToJava(env, dpid);
	//std::cout << "Java_at_rocworks_oa4j_jni_RequestItem_getId: " << jobj << std::endl;
	return jobj;
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_RequestItem_getNumber
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	RequestItem *item = (RequestItem*)cptr;
	return item->getNumber();
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_RequestItem_toDebug
(JNIEnv *env, jobject obj, jint level)
{
	jclass cls = env->GetObjectClass(obj);
	RequestItem *msg = (RequestItem*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}


JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_RequestItem_free
(JNIEnv *, jobject, jlong cptr)
{
	if (cptr != nil) delete (RequestItem*)cptr;
}
