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
#include <at_rocworks_oa4j_jni_SysMsg.h>
#include <WCCOAJavaManager.hxx>
#include <SysMsg.hxx>
#include <InitSysMsg.hxx>
#include <../LibJava/Java.hxx>

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_SysMsg_getSysMsgType
(JNIEnv *, jobject, jlong cptr)
{
	SysMsg *v = (SysMsg*)(cptr);
	return v->getSysMsgType();
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_SysMsg_getSysMsgSubType
(JNIEnv *, jobject, jlong cptr)
{
	SysMsg *v = (SysMsg*)(cptr);
	return v->getSysMsgSubType();
}


JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_SysMsg_getInitSysMsgData
(JNIEnv *env, jobject obj)
{
	jclass cls = env->GetObjectClass(obj);
	SysMsg *sysMsg = (SysMsg*)env->GetLongField(obj, env->GetFieldID(cls, "cptr", "J"));
	env->DeleteLocalRef(cls);

	if (sysMsg != NULL && sysMsg->isType() == INIT_SYS_MSG)
	{
		// store data of init mesaage to a java hash map
		jclass jmapClass = env->FindClass("java/util/HashMap");
		if (jmapClass != 0)
		{
			jmethodID jini = env->GetMethodID(jmapClass, "<init>", "()V");
			jmethodID jput = env->GetMethodID(jmapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			jobject jmap = env->NewObject(jmapClass, jini);
			if (!Java::CheckException(env, "getInitSysMsgData Exception"))
			{
				jobject jkey, jval;

				CharString key;
				CharString val;
				for (bool ok = ((InitSysMsg*)sysMsg)->getFirstData(key, val); ok; ok = ((InitSysMsg*)sysMsg)->getNextData(key, val))
				{
					//std::cout << "key=" << key.c_str() << " val=" << val.c_str() << std::endl;
					jkey = Java::convertToJava(env, key);
					jval = Java::convertToJava(env, val);
					env->CallObjectMethod(jmap, jput, jkey, jval);
					env->DeleteLocalRef(jkey);
					env->DeleteLocalRef(jval);
				}
				return jmap;
			}
			else 
			{
				return NULL;
			}
		}
		else 
		{
			ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE,  "cannot find java class java/util/HashMap!");
			return NULL;
		}
	}
	else
	{
		return NULL;
	}
}