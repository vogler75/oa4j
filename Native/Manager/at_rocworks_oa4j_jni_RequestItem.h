/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class at_rocworks_oa4j_jni_RequestItem */

#ifndef _Included_at_rocworks_oa4j_jni_RequestItem
#define _Included_at_rocworks_oa4j_jni_RequestItem
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     at_rocworks_oa4j_jni_RequestItem
 * Method:    getId
 * Signature: ()Lat/rocworks/oa4j/var/DpIdentifierVar;
 */
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestItem_getId
  (JNIEnv *, jobject);

/*
 * Class:     at_rocworks_oa4j_jni_RequestItem
 * Method:    getNumber
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_RequestItem_getNumber
  (JNIEnv *, jobject);

/*
 * Class:     at_rocworks_oa4j_jni_RequestItem
 * Method:    toDebug
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_RequestItem_toDebug
  (JNIEnv *, jobject, jint);

/*
 * Class:     at_rocworks_oa4j_jni_RequestItem
 * Method:    free
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_RequestItem_free
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif