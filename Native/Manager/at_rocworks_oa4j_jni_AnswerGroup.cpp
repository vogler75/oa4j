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
#include <at_rocworks_oa4j_jni_AnswerGroup.h>
#include <WCCOAJavaManager.hxx>
#include <DpMsgAnswer.hxx>
#include <../LibJava/Java.hxx>
#include <TimeVar.hxx>
#include <FloatVar.hxx>

/*
* Class:     at_rocworks_oa4j_jni_AnswerGroup
* Method:    insertItem
* Signature: (Lat/rocworks/oa4j/var/DpIdentifierVar;Lat/rocworks/oa4j/var/Variable;Lat/rocworks/oa4j/var/TimeVar;)Z
*/
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_AnswerGroup_insertItem
(JNIEnv *env, jobject obj, jobject jId, jobject jValue, jobject jTime)
{
	jclass cls;

	cls = env->GetObjectClass(obj);
	AnswerGroup *group = (AnswerGroup*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpIdentifier dpid;
	if ( Java::convertJDpIdentifierToDpIdentifier(env, jId, dpid) ) 
	{
		Variable *value = Java::convertJVariable(env, jValue);
		TimeVar *time = (TimeVar*)Java::convertJVariable(env, jTime);

		group->insertItem(dpid, *value, *time);

		delete value;
		delete time;

		return true;
	}
	else 
		return false;
}

JNIEXPORT jlong JNICALL Java_at_rocworks_oa4j_jni_AnswerGroup_malloc
(JNIEnv *, jobject)
{
	AnswerGroup *cptr = new AnswerGroup();
	return (jlong)cptr;
}

JNIEXPORT void JNICALL Java_at_rocworks_oa4j_jni_AnswerGroup_free
(JNIEnv *, jobject, jlong cptr)
{
	if (cptr != NULL) delete (AnswerGroup*)cptr;
}
