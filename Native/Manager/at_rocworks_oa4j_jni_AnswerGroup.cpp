#include <at_rocworks_oa4j_jni_AnswerGroup.h>
#include <WCCOAJavaManager.hxx>
#include <DpMsgAnswer.hxx>
#include <../LibJava/Java.hxx>
#include <TimeVar.hxx>
#include <FloatVar.hxx>

/*
* Class:     at_rocworks_oa4j_jni_AnswerGroup
* Method:    insertItem
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;Lat/rocworks/oa4j/var/TimeVar;)Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_AnswerGroup_insertItem
(JNIEnv *env, jobject obj, jobject jId, jobject jValue, jobject jTime)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	AnswerGroup *group = (AnswerGroup*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdentifier dpid;
	if ( Java::convertJDpIdentifierToDpIdentifier(env, jId, dpid) ) 
	{
		Variable *value = Java::convertJVariable(env, jValue);
		TimeVar *time = (TimeVar*)Java::convertJVariable(env, jTime);

		group->insertItem(dpid, *value, *time);

		delete value;
		delete time;

		return true;
	}
	else 
		return false;
}

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_AnswerGroup_malloc
(JNIEnv *, jobject)
{
	AnswerGroup *cptr = new AnswerGroup();
	return (jlong)cptr;
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_AnswerGroup_free
(JNIEnv *, jobject, jlong cptr)
{
	if (cptr != nil) delete (AnswerGroup*)cptr;
}