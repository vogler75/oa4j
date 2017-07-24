#include <at_rocworks_oa4j_jni_DpMsgValueChange.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    needsAnswer
* Signature: ()Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_needsAnswer
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	return msg->needsAnswer();
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    setWantAnswer
* Signature: (Z)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_setWantAnswer
(JNIEnv *env, jobject obj, jboolean answer)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	msg->setWantAnswer(answer);
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    getFirstGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_getFirstGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	DpVCGroup *itm = msg->getFirstGroup();
	if (itm == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitm = env->NewObject(cls, mid, (jlong)itm);
	env->DeleteLocalRef(cls);

	return jitm;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    getNextGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_getNextGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	DpVCGroup *itm = msg->getNextGroup();
	if (itm == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitm = env->NewObject(cls, mid, (jlong)itm);
	env->DeleteLocalRef(cls);

	return jitm;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_free
(JNIEnv *env, jobject obj, jlong cptr)
{
	if (cptr != nil) delete (DpMsgValueChange*)cptr;
}