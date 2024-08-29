#include <WCCOAJavaDrv.hxx>
#include <WCCOAJavaResources.hxx>
#include <../LibJava/Java.hxx>
#include <at_rocworks_oa4j_jni_Driver.h>

#include <DpIdentifierVar.hxx>

//------------------------------------------------------------------------------------------------
// JAVA JNI GetLogPath

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Driver_apiGetLogPath
(JNIEnv *env, jobject obj)
{
	CharString path = WCCOAJavaResources::getLogDir();
	jstring js = env->NewStringUTF(path);
	return js;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Driver_apiGetDataPath
(JNIEnv *env, jobject obj)
{
	CharString path = WCCOAJavaResources::getDataDir();
	jstring js = env->NewStringUTF(path);
	return js;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Driver_apiGetConfigValue
(JNIEnv *env, jobject obj, jstring jkey)
{
	CharString *key = Java::convertJString(env, jkey);
	const char *value = WCCOAJavaResources::getConfigValue(key->c_str());
	jstring js = value == 0 ? 0 : env->NewStringUTF(value);
	delete key;
	return js;
}

//------------------------------------------------------------------------------------------------
// JAVA JNI startup
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Driver_apiStartup
(JNIEnv *env, jobject obj, jobjectArray jargv)
{
	int argc = env->GetArrayLength(jargv);
	char **argv = (char **)malloc(argc * sizeof(char *));
	std::cout << "ARGC: "<<argc<<std::endl;
	for (int i = 0; i < argc; i++)
	{
		jobject jstr = env->GetObjectArrayElement(jargv, i);
		Java::copyJavaStringToString(env, (jstring)jstr, &argv[i]);
		env->DeleteLocalRef(jstr);
	}

	if (argc < 1 || strcmp(argv[0], "-exe") != 0) {
		std::cout << "Java Driver must be started from WCCOAjavadrv executable!" << std::endl;
	} else {

		WCCOAJavaResources resources;
		std::cout << "InitResources...1" << std::endl;
		resources.init(argc, argv);

		std::cout << "InitDriver..." << std::endl;
		WCCOAJavaDrv::thisManager = new WCCOAJavaDrv;
		WCCOAJavaDrv::thisManager->javaInitialize(env, obj);

		std::cout << "MainProcedure..." << std::endl;
		WCCOAJavaDrv::thisManager->mainProcedure(argc, argv);
	}
}

