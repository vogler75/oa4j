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
#include <at_rocworks_oa4j_jni_DpMsgManipDp.h>
#include <DpMsgManipDp.hxx>
#include <DpIdentifierItem.hxx>
#include <WCCOAJavaManager.hxx>
#include <Java.hxx>

/*
 * Class:     at_rocworks_oa4j_jni_DpMsgManipDp
 * Method:    isDeleteDpMsg
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_isDeleteDpMsg
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;
	return msg->getActionType() == DpMsgManipDp::ActionType::DELETE_DP;
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgManipDp
* Method:    getDpName
* Signature: ()Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpName
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;	
	jstring jstr = env->NewStringUTF(msg->getIdentifierItemPtr()->getDatapointName());
	return jstr;
}

/*
 * Class:     at_rocworks_oa4j_jni_DpMsgManipDp
 * Method:    getDpName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;

	DpIdentifier dpid = msg->getIdentifierItemPtr()->getDpId();
	jobject jdpid = Java::convertToJava(env, dpid);
	return jdpid;
}

/*
 * Class:     at_rocworks_oa4j_jni_DpMsgManipDp
 * Method:    getDpType
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpTypeId
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;
	DpTypeId type = msg->getDpType();
	return type.toNumber();
}

/*
* Class:     at_rocworks_oa4j_jni_DpMsgManipDp
* Method:    getDpTypeName
* Signature: ()Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_getDpTypeName
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	jlong cptr = env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);
	DpMsgManipDp *msg = (DpMsgManipDp*)cptr;
	DpTypeId type = msg->getDpType();
	CharString name;
	jstring jname = Java::getTypeName(type, name) ? Java::convertToJava(env, name) : env->NewStringUTF("");
	return jname;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_toString
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgManipDp *msg = (DpMsgManipDp*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	stream << (*msg);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());

	return jstr;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_DpMsgManipDp_toDebug
(JNIEnv *env, jobject obj, jint level)
{
	jclass cls = env->GetObjectClass(obj);
	DpMsgManipDp *msg = (DpMsgManipDp*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	std::ostringstream stream;
	msg->debug(stream, level);
	std::string str = stream.str();

	jstring jstr = env->NewStringUTF(str.c_str());
	return jstr;
}
