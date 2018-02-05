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
#include <at_rocworks_oa4j_jni_RequestItem.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>
#include <DpMsgRequest.hxx>

/*
* Class:     at_rocworks_oa4j_jni_RequestItem
* Method:    getId
* Signature: ()Lat/rocworks/oa4j/var/DpIdentifierVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_RequestItem_getId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	RequestItem *item = (RequestItem*)cptr;
	const DpIdentifier dpid = item->getId();
	//std::cout << "Java_at_rocworks_oa4j_jni_RequestItem_getId: " << dpid << std::endl;

	jobject jobj = Java::convertToJava(env, dpid);
	//std::cout << "Java_at_rocworks_oa4j_jni_RequestItem_getId: " << jobj << std::endl;
	return jobj;
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_RequestItem_getNumber
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	RequestItem *item = (RequestItem*)cptr;
	return item->getNumber();
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_RequestItem_toDebug
(JNIEnv *env, jobject obj, jint level)
{
	jclass cls = env->GetObjectClass(obj);
	RequestItem *msg = (RequestItem*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}


JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_RequestItem_free
(JNIEnv *, jobject, jlong cptr)
{
	if (cptr != nil) delete (RequestItem*)cptr;
}
