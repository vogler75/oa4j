#include <at_rocworks_oa4j_jni_DpMsgAnswer.h>
#include <WCCOAJavaManager.hxx>
#include <DpMsgAnswer.hxx>

JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgAnswer_insertGroup
(JNIEnv *env, jobject obj, jobject jAnswerGroup)
{
	jclass cls;

	// get c pointer of DpMsgAnswer
	cls = env->GetObjectClass(obj);	
	DpMsgAnswer *msg = (DpMsgAnswer*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	// get c pointer of AnswerGroup
	cls = env->GetObjectClass(jAnswerGroup);
	AnswerGroup *group = (AnswerGroup*)env->GetLongField(jAnswerGroup, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	//std::cout << "insertGroup " << msg << " " << (*msg) << std::endl;

	return msg->insertGroup(group);
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpMsgAnswer_setOutstandingProgress
(JNIEnv *env, jobject obj, jint percents)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgAnswer *msg = (DpMsgAnswer*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	//std::cout << "setOutstandingProgress " << percents << std::endl;
	msg->setOutstandingProgress((int)percents);
}

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpMsgAnswer_newFromMsg
(JNIEnv *, jobject, jlong msgPtr)
{
	const DpMsg &msg = (const DpMsg &)(*(DpMsg*)msgPtr);
	DpMsgAnswer *cptr = new DpMsgAnswer(msg);
	//std::cout << "DpMsgAnswer " << cptr << " from " << msgPtr << " newFromMsg " << (*cptr) << " NrOfGroups " << cptr->getNrOfGroups() << " id=" << cptr->getMsgId() << std::endl;
	return (jlong)cptr;
}

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpMsgAnswer_newFromMsgAnswer
(JNIEnv *, jobject, jlong msgAnswerPtr)
{
	const DpMsgAnswer &msg = (const DpMsgAnswer &)(*(DpMsgAnswer*)msgAnswerPtr);
	DpMsgAnswer *cptr = new DpMsgAnswer(msg);
	//std::cout << "DpMsgAnswer "<< cptr << " newFromMsgAnswer " << (*cptr) << " NrOfGroups " << cptr->getNrOfGroups() << " id=" << cptr->getMsgId() << std::endl;
	return (jlong)cptr;
}
