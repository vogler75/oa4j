#include <at_rocworks_oa4j_jni_DpIdValueList.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    getFirstItem
* Signature: ()Lat/rocworks/oa4j/jni/DpVCItem;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_getFirstItem
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdValueList *list = (DpIdValueList*)cptr;
	DpVCItem *item = list->getFirstItem();

	if (item == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCItem");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitem = env->NewObject(cls, mid, (jlong)item);
	env->DeleteLocalRef(cls);

	return jitem;
}

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    getNextItem
* Signature: ()Lat/rocworks/oa4j/jni/DpVCItem;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_getNextItem
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdValueList *list = (DpIdValueList*)cptr;
	DpVCItem *item = list->getNextItem();

	if (item == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCItem");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitem = env->NewObject(cls, mid, (jlong)item);
	env->DeleteLocalRef(cls);

	return jitem;
}

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    cutFirstItem
* Signature: ()Lat/rocworks/oa4j/jni/DpVCItem;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_cutFirstItem
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdValueList *list = (DpIdValueList*)cptr;
	DpVCItem *item = list->cutFirstItem();

	if (item == nil) return nil;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCItem");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitem = env->NewObject(cls, mid, (jlong)item);
	env->DeleteLocalRef(cls);

	return jitem;
}

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    getNumberOfItems
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_getNumberOfItems
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdValueList *list = (DpIdValueList*)cptr;
	return list->getNumberOfItems();
}

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    appendItem
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;)Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_appendItem
(JNIEnv *env, jobject obj, jobject jdpid, jobject jvar)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdValueList *list = (DpIdValueList*)cptr;

	DpIdentifier dpid;
	if (Java::convertJDpIdentifierToDpIdentifier(env, jdpid, dpid))
	{
		VariablePtr var = Java::convertJVariable(env, jvar);
		return list->appendItem(dpid, var);
	}
	else
	{
		return false;
	}
}

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    malloc
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_malloc
(JNIEnv *env, jobject)
{
	DpIdValueList *cptr = new DpIdValueList();
	return (jlong)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpIdValueList
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_free
(JNIEnv *env, jobject, jlong cptr)
{
	if (cptr != nil) delete (DpIdValueList*)cptr;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpIdValueList_toDebug
(JNIEnv *env, jobject jobj, jint level)
{
	jclass cls = env->GetObjectClass(jobj);
	DpIdValueList *obj = (DpIdValueList*)env->GetLongField(jobj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	obj->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}