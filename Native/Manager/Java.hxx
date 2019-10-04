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
#ifndef _Java_H_
#define _Java_H_

#include <DpIdentifier.hxx>
#include <DpIdentification.hxx>

#include <DpIdentList.hxx>
#include <DpIdValueList.hxx>
#include <Variable.hxx>

#include <qmutex.h>

#include <jni.h>

class JDpIdentifierClass {
private:
	JNIEnv *env;
	jclass cls;
	jmethodID midInit;
	jmethodID midSetName;

public:
	JDpIdentifierClass(JNIEnv *env);
	~JDpIdentifierClass();

	jclass Class() { return cls;  }
	jmethodID Init() { return midInit; }
	jmethodID SetName() { return midSetName; }
};

class JVariableClass {
private:
	JNIEnv *env;
	jclass cls;
	jclass clsDynVar;
	jmethodID midNewBitVar;
	jmethodID midNewBit32Var;
	jmethodID midNewBit64Var;
	jmethodID midNewFloatVar;
	jmethodID midNewLongVar;
	jmethodID midNewIntegerVar;
	jmethodID midNewUIntegerVar;
	jmethodID midNewCharVar;
	jmethodID midNewTextVar;
	jmethodID midNewLangTextVar;
	jmethodID midSetLangTextVar;
	jmethodID midNewTimeVar;
	jmethodID midNewDynVar;
	jmethodID midNewDynVarSized;
	jmethodID midAddDynVar;

public:

	JVariableClass(JNIEnv *env);
	~JVariableClass();

	jclass Class() { return cls; }
	jclass ClassDynVar() { return clsDynVar; }
	inline jmethodID newBitVar() { 
		return midNewBitVar ? midNewBitVar : (midNewBitVar=env->GetStaticMethodID(cls, "newBitVar", "(Z)Lat/rocworks/oa4j/var/Variable;")); 
	}
	inline jmethodID newBit32Var() {
		return midNewBit32Var ? midNewBit32Var : (midNewBit32Var = env->GetStaticMethodID(cls, "newBit32Var", "(J)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newBit64Var() {
		return midNewBit64Var ? midNewBit64Var : (midNewBit64Var = env->GetStaticMethodID(cls, "newBit64Var", "(J)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newFloatVar() {
		return midNewFloatVar ? midNewFloatVar : (midNewFloatVar = env->GetStaticMethodID(cls, "newFloatVar", "(D)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newLongVar() {
		return midNewLongVar ? midNewLongVar : (midNewLongVar = env->GetStaticMethodID(cls, "newLongVar", "(J)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newIntegerVar() {
		return midNewIntegerVar ? midNewIntegerVar : (midNewIntegerVar = env->GetStaticMethodID(cls, "newIntegerVar", "(I)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newUIntegerVar() {
		return midNewUIntegerVar ? midNewUIntegerVar : (midNewUIntegerVar = env->GetStaticMethodID(cls, "newUIntegerVar", "(I)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newCharVar() {
		return midNewCharVar ? midNewCharVar : (midNewCharVar = env->GetStaticMethodID(cls, "newCharVar", "(C)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newTextVar() {
		return midNewTextVar ? midNewTextVar : (midNewTextVar = env->GetStaticMethodID(cls, "newTextVar", "(Ljava/lang/String;)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newLangTextVar() {
		return midNewLangTextVar ? midNewLangTextVar : (midNewLangTextVar = env->GetStaticMethodID(cls, "newLangTextVar", "()Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newTimeVar() {
		return midNewTimeVar ? midNewTimeVar : (midNewTimeVar = env->GetStaticMethodID(cls, "newTimeVar", "(J)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newDynVar() {
		return midNewDynVar ? midNewDynVar : (midNewDynVar = env->GetStaticMethodID(cls, "newDynVar", "()Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID newDynVarSized() {
		return midNewDynVarSized ? midNewDynVarSized : (midNewDynVarSized = env->GetStaticMethodID(cls, "newDynVar", "(I)Lat/rocworks/oa4j/var/Variable;"));
	}
	inline jmethodID addDynVar() {
		return midAddDynVar ? midAddDynVar : (midAddDynVar = env->GetMethodID(clsDynVar, "add", "(Lat/rocworks/oa4j/var/Variable;)V"));
	}
};

class Java {
private:
	static const char *NAME;

public:
	static bool DEBUG;

	static const char *DpVCItemClassName;
	static const char *DpIdentifierClassName;
	static const char *VariableClassName;
	static const char *DynVarClassName;

	static jclass FindClass(JNIEnv *env, const char* name);
	static jmethodID GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	static jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	static int CheckException(JNIEnv *env, const char* msg);

	static void				copyJavaStringToString(JNIEnv *env, jstring s, char **d);

	static jobject			convertToJava(JNIEnv *env, const DpIdentifier &dpid, JDpIdentifierClass *jdpid=0);
	static jobject			convertToJava(JNIEnv *env, VariablePtr varptr, JDpIdentifierClass *jdpid=0, JVariableClass *jvar=0);
	static jstring			convertToJava(JNIEnv *env, CharString *str);
	static jstring          convertToJava(JNIEnv *env, const CharString &str);

	static VariablePtr		convertJVariable(JNIEnv *env, jobject jVariable);
	static CharString*		convertJString(JNIEnv *env, jstring jString);
	static bool	    		convertJDpIdentifierToDpIdentifier(JNIEnv *env, jobject dp, DpIdentifier &dpid);

	static DpIdentList*     convertJArrayOfStringToDpIdentList(JNIEnv *env, jobjectArray dps);
	static DpIdentList*     convertJArrayOfDpIdentifierToDpIdentList(JNIEnv *env, jobjectArray dps);
	static DpIdValueList*   convertJArrayOfDpVCItemToDpIdValueList(JNIEnv *env, jobjectArray dps);

	static QMutex dpIdMutex; // public, because we also need it to lock RequestItem.getId
	static PVSSboolean getId(const char *name, DpIdentifier &dpId); // getId with Mutex
	static PVSSboolean getTypeName(DpTypeId typeId, CharString &typeName,
		SystemNumType sysNum = DpIdentification::getDefaultSystem());
};

#endif
