#include <at_rocworks_oa4j_jni_DpVCGroup.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    getOriginTime
* Signature: ()Lat/rocworks/oa4j/var/TimeVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_getOriginTime
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCGroup *group = (DpVCGroup*)cptr;
	const TimeVar var = group->getOriginTime();
	VariablePtr ptr = (VariablePtr)&var;
	return Java::convertToJava(env, ptr);
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    setOriginTime
* Signature: (Lat/rocworks/oa4j/var/TimeVar;)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_setOriginTime
(JNIEnv *env, jobject obj, jobject jtimevar)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCGroup *group = (DpVCGroup*)cptr;
	VariablePtr ptr = Java::convertJVariable(env, jtimevar);
	TimeVar var = *((TimeVar*)ptr);
	group->setOriginTime(var);
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    insertValueChange
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;)Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_insertValueChange
(JNIEnv *env, jobject obj, jobject jdpid, jobject jvar)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCGroup *group = (DpVCGroup*)cptr;

	DpIdentifier dpid;
	if (Java::convertJDpIdentifierToDpIdentifier(env, jdpid, dpid))
	{
		VariablePtr var = Java::convertJVariable(env, jvar);
		return group->insertValueChange(dpid, var);
	}
	else
	{
		return false;
	}
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    malloc
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_malloc
(JNIEnv *env, jobject)
{
	DpVCGroup *cptr = new DpVCGroup();
	return (jlong)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_free
(JNIEnv *env, jobject, jlong cptr)
{
	if (cptr != nil) delete (DpVCGroup*)cptr;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_toDebug
(JNIEnv *env, jobject jobj, jint level)
{
	jclass cls = env->GetObjectClass(jobj);
	DpVCGroup *obj = (DpVCGroup*)env->GetLongField(jobj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	obj->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}