/*
OA4J - WinCC Open Architecture for Java
Copyright (C) 2017 Andreas Vogler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
#include <at_rocworks_oa4j_jni_DpMsgValueChange.h>
#include <WCCOAJavaManager.hxx>
#include <Java.hxx>

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    needsAnswer
* Signature: ()Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_needsAnswer
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	return msg->needsAnswer();
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    setWantAnswer
* Signature: (Z)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_setWantAnswer
(JNIEnv *env, jobject obj, jboolean answer)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	msg->setWantAnswer(answer);
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    getFirstGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_getFirstGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	DpVCGroup *itm = msg->getFirstGroup();
	if (itm == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitm = env->NewObject(cls, mid, (jlong)itm);
	env->DeleteLocalRef(cls);

	return jitm;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    getNextGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_getNextGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgValueChange *msg = (DpMsgValueChange*)cptr;
	DpVCGroup *itm = msg->getNextGroup();
	if (itm == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitm = env->NewObject(cls, mid, (jlong)itm);
	env->DeleteLocalRef(cls);

	return jitm;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgValueChange
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpMsgValueChange_free
(JNIEnv *env, jobject obj, jlong cptr)
{
	if (cptr != NULL) delete (DpMsgValueChange*)cptr;
}
