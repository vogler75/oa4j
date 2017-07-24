#include <at_rocworks_oa4j_jni_DpMsgRequest.h>
#include <DpMsgRequest.hxx>

#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>


JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_getFirstGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgRequest *req = (DpMsgRequest*)cptr;
	RequestGroup *grp = req->getFirstGroup();
	if (grp == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/RequestGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jgrp = env->NewObject(cls, mid, (jlong)grp);
	env->DeleteLocalRef(cls);

	return jgrp;	
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_getNextGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgRequest *req = (DpMsgRequest*)cptr;
	RequestGroup *grp = req->getNextGroup();
	if (grp == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/RequestGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jgrp = env->NewObject(cls, mid, (jlong)grp);
	env->DeleteLocalRef(cls);

	return jgrp;
}

JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_getMultipleAnswersAllowed
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgRequest *req = (DpMsgRequest*)cptr;
	return req->getMultipleAnswersAllowed();
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_toString
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgRequest *msg = (DpMsgRequest*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	stream << (*msg);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}