/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class at_rocworks_oa4j_jni_Driver */

#ifndef _Included_at_rocworks_oa4j_jni_Driver
#define _Included_at_rocworks_oa4j_jni_Driver
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     at_rocworks_oa4j_jni_Driver
 * Method:    apiGetLogPath
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Driver_apiGetLogPath
  (JNIEnv *, jobject);

/*
 * Class:     at_rocworks_oa4j_jni_Driver
 * Method:    apiGetDataPath
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Driver_apiGetDataPath
  (JNIEnv *, jobject);

/*
 * Class:     at_rocworks_oa4j_jni_Driver
 * Method:    apiStartup
 * Signature: ([Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_Driver_apiStartup
  (JNIEnv *, jobject, jobjectArray);

/*
* Class:     at_rocworks_oa4j_jni_Driver
* Method:    apiGetConfigValue
* Signature: (Ljava/lang/String)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Driver_apiGetConfigValue
(JNIEnv *, jobject, jstring);

#ifdef __cplusplus
}
#endif
#endif
