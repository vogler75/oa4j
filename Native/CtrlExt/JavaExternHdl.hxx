#ifndef _JavaEXTERNHDL_H_
#define _JavaEXTERNHDL_H_

#include <BaseExternHdl.hxx>
#include <DpIdentifier.hxx>   
#include <jni.h>


class JavaExternHdl : public BaseExternHdl
{
public:
	JavaExternHdl(BaseExternHdl *nextHdl, PVSSulong funcCount, FunctionListRec fnList[])
		: BaseExternHdl(nextHdl, funcCount, fnList) {}

	virtual const Variable *execute(ExecuteParamRec &param);

	static const char *ManagerName;
	static const bool DEBUG;

	static const char *ExternHdlClassName;
	static const char *JavaCallClassName;

	static jclass clsJavaCall;

private:

	JavaVM *jvm;                      // Pointer to the JVM (Java Virtual Machine)
	JNIEnv *env;                      // Pointer to native interface
	jint jvmState = JNI_ABORT;

	const Variable* startVM(ExecuteParamRec &param);
	const Variable* stopVM(ExecuteParamRec &param);
	const Variable* javaCall(ExecuteParamRec &param);
	const Variable* javaCallAsync(ExecuteParamRec &param);	
};

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_ExternHdl_apiGetLogDir
(JNIEnv *, jclass);

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_ExternHdl_apiGetManType
(JNIEnv *, jclass);

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_ExternHdl_apiGetManNum
(JNIEnv *, jclass);

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_ExternHdl_apiAddResult
(JNIEnv *, jclass, jlong jWaitCondPtr, jobject jvar);

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_ExternHdl_apiStartFunc
(JNIEnv *, jclass, jlong jWaitCondPtr, jstring jname, jobject jargs);

#endif
