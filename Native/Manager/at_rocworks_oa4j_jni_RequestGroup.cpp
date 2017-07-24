#include <at_rocworks_oa4j_jni_RequestGroup.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>
#include <DpMsgRequest.hxx>

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_getFirstItem 
(JNIEnv *env, jobject obj)
{
	jclass cls;
	
	cls = env->GetObjectClass(obj);
	jfieldID fld = env->GetFieldID(cls, "cptr", "J");
	jlong cptr = env->GetLongField(obj, fld);
	env->DeleteLocalRef(cls);

	RequestGroup *grp = (RequestGroup*)cptr;
	RequestItem *itm = grp->getFirstItem();
	if (itm == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/RequestItem");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitm = env->NewObject(cls, mid, (jlong)itm);
	env->DeleteLocalRef(cls);

	return jitm;
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_getNextItem
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jfieldID fld = env->GetFieldID(cls, "cptr", "J");
	jlong cptr = env->GetLongField(obj, fld);
	RequestGroup *grp = (RequestGroup*)cptr;
	env->DeleteLocalRef(cls);

	RequestItem *itm = grp->getNextItem();
	if (itm == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/RequestItem");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitm = env->NewObject(cls, mid, (jlong)itm);
	env->DeleteLocalRef(cls);
	
	return jitm;
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_getTime1
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jfieldID fld = env->GetFieldID(cls, "cptr", "J");
	jlong cptr = env->GetLongField(obj, fld);
	env->DeleteLocalRef(cls);

	RequestGroup *grp = (RequestGroup*)cptr;
	TimeVar *t = new TimeVar(grp->getTime1());
	jobject jt = Java::convertToJava(env, t);
	delete t;

	return jt;
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_getTime2
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jfieldID fld = env->GetFieldID(cls, "cptr", "J");
	jlong cptr = env->GetLongField(obj, fld);
	env->DeleteLocalRef(cls);

	RequestGroup *grp = (RequestGroup*)cptr;
	TimeVar *t = new TimeVar(grp->getTime2());
	jobject jt = Java::convertToJava(env, t);
	delete t;

	return jt;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_toDebug
(JNIEnv *env, jobject obj, jint level)
{	
	jclass cls = env->GetObjectClass(obj);
	RequestGroup *msg = (RequestGroup*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_malloc
(JNIEnv *, jobject)
{
	RequestGroup *cptr = new RequestGroup();
	return (jlong)cptr;
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_RequestGroup_free
(JNIEnv *, jobject, jlong cptr)
{
	if (cptr != nil) delete (RequestGroup*)cptr;
}
