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
#include <at_rocworks_oa4j_jni_DpMsgComplexVC.h>
#include <WCCOAJavaManager.hxx>
#include <Java.hxx>

/*
* Class:     at_rocworks_oa4j_jni_DpMsgComplexVC
* Method:    getFirstGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgComplexVC_getFirstGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgComplexVC *list = (DpMsgComplexVC*)cptr;
	DpVCGroup *item = list->getFirstGroup();

	if (item == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitem = env->NewObject(cls, mid, (jlong)item);
	env->DeleteLocalRef(cls);

	return jitem;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgComplexVC
* Method:    getNextGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgComplexVC_getNextGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgComplexVC *list = (DpMsgComplexVC*)cptr;
	DpVCGroup *item = list->getNextGroup();

	if (item == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitem = env->NewObject(cls, mid, (jlong)item);
	env->DeleteLocalRef(cls);

	return jitem;
}


/*
* Class:     at_rocworks_oa4j_jni_DpMsgComplexVC
* Method:    getLastGroup
* Signature: ()Lat/rocworks/oa4j/jni/DpVCGroup;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgComplexVC_getLastGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgComplexVC *list = (DpMsgComplexVC*)cptr;
	DpVCGroup *item = list->getLastGroup();

	if (item == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/DpVCGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jitem = env->NewObject(cls, mid, (jlong)item);
	env->DeleteLocalRef(cls);

	return jitem;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgComplexVC
* Method:    getNrOfGroups
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_DpMsgComplexVC_getNrOfGroups
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgComplexVC *list = (DpMsgComplexVC*)cptr;
	return list->getNrOfGroups();	
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgComplexVC_toDebug
(JNIEnv *env, jobject obj, jint level)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgComplexVC *msg = (DpMsgComplexVC*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}
