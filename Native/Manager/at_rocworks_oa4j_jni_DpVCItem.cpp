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
#include <at_rocworks_oa4j_jni_DpVCItem.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    malloc
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_malloc
(JNIEnv *env, jobject)
{
	DpVCItem *cptr = new DpVCItem();
	return (jlong)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_free
(JNIEnv *env, jobject, jlong cptr)
{
	if (cptr != NULL) delete (DpVCItem*)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    getDpIdentifier
* Signature: ()Lat/rocworks/oa4j/var/DpIdentifierVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_getDpIdentifier
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCItem *item = (DpVCItem*)cptr;
	DpIdentifier dpid = item->getDpIdentifier();
	if (!dpid.isNull())
		return Java::convertToJava(env, dpid);
	else
		return NULL;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    setDpIdentifier
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;)V
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_setDpIdentifier
(JNIEnv *env, jobject obj, jobject jdpid)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpVCItem *item = (DpVCItem*)cptr;

	DpIdentifier dpid;
	if( Java::convertJDpIdentifierToDpIdentifier(env, jdpid, dpid) ) 
	{ 
		item->setDpIdentifier(dpid);
		return true;
	} 
	else {
		return false;
	}
	
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    getValue
* Signature: ()Lat/rocworks/oa4j/var/Variable;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_getValue
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpVCItem *item = (DpVCItem*)cptr;

	VariablePtr ptr = item->getValuePtr();
	if (ptr != NULL)
		return Java::convertToJava(env, ptr);
	else
		return NULL;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCItem
* Method:    setValue
* Signature: (Lat/rocworks/oa4j/var/Variable;)V
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_setValue
(JNIEnv *env, jobject obj, jobject value)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCItem *item = (DpVCItem*)cptr;
	VariablePtr ptr = Java::convertJVariable(env, value);
	item->setValue(ptr);
	return true;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpVCItem_toDebug
(JNIEnv *env, jobject jobj, jint level)
{
	jclass cls = env->GetObjectClass(jobj);
	DpVCItem *obj = (DpVCItem*)env->GetLongField(jobj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	obj->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}
