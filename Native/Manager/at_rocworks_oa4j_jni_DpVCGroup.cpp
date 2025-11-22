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
#include <at_rocworks_oa4j_jni_DpVCGroup.h>
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>
#include <sstream>

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    getOriginTime
* Signature: ()Lat/rocworks/oa4j/var/TimeVar;
*/
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_getOriginTime
(JNIEnv *env, jobject obj)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCGroup *group = (DpVCGroup*)cptr;
	const TimeVar var = group->getOriginTime();
	VariablePtr ptr = (VariablePtr)&var;
	return Java::convertToJava(env, ptr);
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    setOriginTime
* Signature: (Lat/rocworks/oa4j/var/TimeVar;)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_setOriginTime
(JNIEnv *env, jobject obj, jobject jtimevar)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCGroup *group = (DpVCGroup*)cptr;
	VariablePtr ptr = Java::convertJVariable(env, jtimevar);
	TimeVar var = *((TimeVar*)ptr);
	group->setOriginTime(var);
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    insertValueChange
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;)Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_insertValueChange
(JNIEnv *env, jobject obj, jobject jdpid, jobject jvar)
{
	jclass cls;
	cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpVCGroup *group = (DpVCGroup*)cptr;

	DpIdentifier dpid;
	if (Java::convertJDpIdentifierToDpIdentifier(env, jdpid, dpid))
	{
		VariablePtr var = Java::convertJVariable(env, jvar);
		return group->insertValueChange(dpid, var);
	}
	else
	{
		return false;
	}
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    malloc
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_malloc
(JNIEnv *env, jobject)
{
	DpVCGroup *cptr = new DpVCGroup();
	return (jlong)cptr;
}

/*
* Class:     at_rocworks_oa4j_jni_DpVCGroup
* Method:    free
* Signature: (J)V
*/
JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_free
(JNIEnv *env, jobject, jlong cptr)
{
	if (cptr != 0) delete (DpVCGroup*)cptr;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpVCGroup_toDebug
(JNIEnv *env, jobject jobj, jint level)
{
	jclass cls = env->GetObjectClass(jobj);
	DpVCGroup *obj = (DpVCGroup*)env->GetLongField(jobj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	obj->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}
