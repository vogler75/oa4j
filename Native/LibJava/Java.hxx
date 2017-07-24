#ifndef _Java_H_
#define _Java_H_

#include <DpIdentifier.hxx>   
#include <DpIdentification.hxx>
#include <DpIdentList.hxx>
#include <Variable.hxx>
#include <Mutex.hxx>

#include <jni.h>

class Java {
private:
	static const char *NAME;
	static const bool DEBUG;

	static const char *DpVCItemClassName;
	static const char *DpIdentifierClassName;
	static const char *VariableClassName;
	static const char *DynVarClassName;

public:
	static jclass FindClass(JNIEnv *env, const char* name);
	static jmethodID GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	static jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	static int CheckException(JNIEnv *env, const char* msg);

	static void				copyJavaStringToString(JNIEnv *env, jstring s, char **d);

	static jobject			convertToJava(JNIEnv *env, const DpIdentifier &dpid);
	static jobject			convertToJava(JNIEnv *env, VariablePtr varptr);
	static jstring			convertToJava(JNIEnv *env, CharString *str);
	static jstring          convertToJava(JNIEnv *env, const CharString &str);


	static VariablePtr		convertJVariable(JNIEnv *env, jobject jVariable);
	static CharString*		convertJString(JNIEnv *env, jstring jString);
	static bool	    		convertJDpIdentifierToDpIdentifier(JNIEnv *env, jobject dp, DpIdentifier &dpid);

	static DpIdentList*		convertJArrayOfStringToDpIdentList(JNIEnv *env, jobjectArray dps);
	static DpIdentList*		convertJArrayOfDpIdentifierToDpIdentList(JNIEnv *env, jobjectArray dps);
	static DpIdValueList*	convertJArrayOfDpVCItemToDpIdValueList(JNIEnv *env, jobjectArray dps);

	static Mutex dpIdMutex; // public, because we also need it to lock RequestItem.getId
	static PVSSboolean getId(const char *name, DpIdentifier &dpId); // getId with Mutex
	static PVSSboolean getTypeName(DpTypeId typeId, CharString &typeName,
		SystemNumType sysNum = DpIdentification::getDefaultSystem());
};

#endif
