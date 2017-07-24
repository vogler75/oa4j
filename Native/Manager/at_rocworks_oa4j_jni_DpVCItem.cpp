#include <at_rocworks_oa4j_jni_DpVCItem.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>


/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    malloc
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_malloc
(JNIEnv *env, jobject)
{
	DpVCItem *cptr = new DpVCItem();
	return (jlong)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_free
(JNIEnv *env, jobject, jlong cptr)
{
	if (cptr != nil) delete (DpVCItem*)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    getDpIdentifier
* Signature: ()Lat/rocworks/oa4j/var/DpIdentifierVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_getDpIdentifier
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCItem *item = (DpVCItem*)cptr;
	DpIdentifier dpid = item->getDpIdentifier();
	if (!dpid.isNull())
		return Java::convertToJava(env, dpid);
	else
		return nil;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    setDpIdentifier
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;)V
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_setDpIdentifier
(JNIEnv *env, jobject obj, jobject jdpid)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpVCItem *item = (DpVCItem*)cptr;

	DpIdentifier dpid;
	if( Java::convertJDpIdentifierToDpIdentifier(env, jdpid, dpid) ) 
	{ 
		item->setDpIdentifier(dpid);
		return true;
	} 
	else {
		return false;
	}
	
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    getValue
* Signature: ()Lat/rocworks/oa4j/var/Variable;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_getValue
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpVCItem *item = (DpVCItem*)cptr;

	VariablePtr ptr = item->getValuePtr();
	if (ptr != nil)
		return Java::convertToJava(env, ptr);
	else
		return nil;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    setValue
* Signature: (Lat/rocworks/oa4j/var/Variable;)V
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_setValue
(JNIEnv *env, jobject obj, jobject value)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCItem *item = (DpVCItem*)cptr;
	VariablePtr ptr = Java::convertJVariable(env, value);
	item->setValue(ptr);
	return true;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_toDebug
(JNIEnv *env, jobject jobj, jint level)
{
	jclass cls = env->GetObjectClass(jobj);
	DpVCItem *obj = (DpVCItem*)env->GetLongField(jobj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	obj->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}