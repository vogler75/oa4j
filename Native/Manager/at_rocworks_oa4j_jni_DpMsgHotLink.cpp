#include <at_rocworks_oa4j_jni_DpMsgHotLink.h>
#include <WCCOAJavaManager.hxx>

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpMsgHotLink_malloc
(JNIEnv *, jobject)
{
	DpMsgHotLink *cptr = new DpMsgHotLink();
	return (jlong)cptr;
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpMsgHotLink_free
(JNIEnv *, jobject, jlong cptr)
{
	if (cptr != nil) delete (DpMsgHotLink*)cptr;
}