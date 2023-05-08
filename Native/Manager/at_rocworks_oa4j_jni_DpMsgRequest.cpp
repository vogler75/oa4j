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
#include <at_rocworks_oa4j_jni_DpMsgRequest.h>
#include <DpMsgRequest.hxx>

#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>
#include <sstream>


JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_getFirstGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgRequest *req = (DpMsgRequest*)cptr;
	RequestGroup *grp = req->getFirstGroup();
	if (grp == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/RequestGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jgrp = env->NewObject(cls, mid, (jlong)grp);
	env->DeleteLocalRef(cls);

	return jgrp;	
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_getNextGroup
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgRequest *req = (DpMsgRequest*)cptr;
	RequestGroup *grp = req->getNextGroup();
	if (grp == NULL) return NULL;

	cls = env->FindClass("at/rocworks/oa4j/jni/RequestGroup");
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jobject jgrp = env->NewObject(cls, mid, (jlong)grp);
	env->DeleteLocalRef(cls);

	return jgrp;
}

JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_getMultipleAnswersAllowed
(JNIEnv *env, jobject obj)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgRequest *req = (DpMsgRequest*)cptr;
	return req->getMultipleAnswersAllowed();
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgRequest_toString
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgRequest *msg = (DpMsgRequest*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	stream << (*msg);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}
