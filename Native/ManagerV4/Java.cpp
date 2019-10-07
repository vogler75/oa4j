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
#include <Java.hxx>
#include <Manager.hxx>

#include <DpVCItem.hxx>               

#include <DpIdentifier.hxx>
#include <DpIdentifierVar.hxx>

#include <Variable.hxx>
#include <DynVar.hxx>
#include <AnyTypeVar.hxx>

#include <Bit32Var.hxx>
#include <Bit64Var.hxx>
#include <FloatVar.hxx>
#include <ShortVar.hxx>
#include <ByteVar.hxx>
#include <IntegerVar.hxx>
#include <UIntegerVar.hxx>
#include <TextVar.hxx>
#include <CharVar.hxx>
#include <LongVar.hxx>
#include <ULongVar.hxx>
#include <TimeVar.hxx>

#include <qmutex.h>

bool Java::DEBUG = false;

#define JUnknown 0
#define JAnyTypeVar 1
#define JBit32Var 2
#define JBit64Var 3
#define JBitVar 4
#define JBlobVar 5
#define JCharVar 6
#define JDpIdentifierVar 7
#define JDynVar 8
#define JErrorVar 9
#define JFloatVar 10
#define JIntegerVar 11
#define JLangTextVar 12
#define JTextVar 13
#define JTimeVar 14
#define JUIntegerVar 15
#define JLongVar 16
#define JULongVar 17
#define JNullVar 0xFFFFFFFF

//---------------------------------------------------------------------------------------------------------
JDpIdentifierClass::JDpIdentifierClass(JNIEnv *p_env)
{
	env = p_env;
	cls = env->FindClass(Java::DpIdentifierClassName);
	midInit = env->GetMethodID(cls, "<init>", "()V");
	midSetName = env->GetMethodID(cls, "setName", "(Ljava/lang/String;)V");
}

JDpIdentifierClass::~JDpIdentifierClass()
{
	env->DeleteLocalRef(cls);
}

//---------------------------------------------------------------------------------------------------------
JVariableClass::JVariableClass(JNIEnv *p_env)
{
	env = p_env;
	cls = env->FindClass(Java::VariableClassName);
	clsDynVar = env->FindClass(Java::DynVarClassName);
	midNewBitVar = 0;
	midNewBit32Var = 0;
	midNewBit64Var = 0;
	midNewFloatVar = 0;
	midNewLongVar = 0;
	midNewIntegerVar = 0;
	midNewUIntegerVar = 0;
	midNewCharVar = 0;
	midNewTextVar = 0;
	midNewLangTextVar = 0;
	midSetLangTextVar = 0;
	midNewTimeVar = 0;
	midNewDynVar = 0;
	midNewDynVarSized = 0;
	midAddDynVar = 0;
}

JVariableClass::~JVariableClass()
{
	env->DeleteLocalRef(cls);
	env->DeleteLocalRef(clsDynVar);
}


//---------------------------------------------------------------------------------------------------------
const char *Java::NAME = "java";

const char *Java::DpIdentifierClassName = "at/rocworks/oa4j/var/DpIdentifierVar";
const char *Java::VariableClassName = "at/rocworks/oa4j/var/Variable";
const char *Java::DynVarClassName = "at/rocworks/oa4j/var/DynVar";
const char *Java::DpVCItemClassName = "at/rocworks/oa4j/base/JDpVCItem";

QMutex Java::dpIdMutex;

jclass Java::FindClass(JNIEnv *env, const char* name)
{
	jclass jClass = env->FindClass(name);
	if (jClass == NULL) {
		std::string msg = "class " + std::string(name) + " not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			NAME, "FindClass", msg.c_str());
	}
	return jClass;
}

jmethodID Java::GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig)
{
	jmethodID jMethod = env->GetMethodID(clazz, name, sig);
	if (jMethod == NULL) {
		std::string msg = "java method " + std::string(name) + std::string(sig) + " not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			NAME, "GetMethodID", msg.c_str());
	}
	return jMethod;
}

jmethodID Java::GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig)
{
	jmethodID jMethod = env->GetStaticMethodID(clazz, name, sig);
	if (jMethod == NULL) {
		std::string msg = "static java method " + std::string(name) + std::string(sig) + " not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			NAME, "GetStaticMethodID", msg.c_str());
	}
	return jMethod;
}

int Java::CheckException(JNIEnv *env, const char *msg)
{
	if (env->ExceptionCheck()) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			NAME, "Exception", msg);
		env->ExceptionDescribe();
		env->ExceptionClear();
		return -1;
	}
	return 0;
}

//--------------------------------------------------------------------------------
// copy a java string to a cpp string

void Java::copyJavaStringToString(JNIEnv *env, jstring s, char **d)
{
	CharString *cs;
	const char *js = env->GetStringUTFChars(s, 0);
	cs = new CharString(js);
	*d = (char *)malloc((cs)->len() + 1);
	strcpy(*d, *cs);
	env->ReleaseStringUTFChars(s, js);
}


//--------------------------------------------------------------------------------
// Converts a DpIdentifier to a Java Object

jobject Java::convertToJava(JNIEnv *env, const DpIdentifier &dpid, JDpIdentifierClass *cdpid)
{
	jobject objDpId;
	jstring jstr;
	bool cdpidTmp=false;

    if (Java::DEBUG) std::cout << "convertToJava:DpIdentifier:" << dpid << std::endl;

	if (!cdpid) { 
		cdpid = new JDpIdentifierClass(env);
		cdpidTmp = true;
	}

	// create dpid object
	objDpId = env->NewObject(cdpid->Class(), cdpid->Init());

	CharString cstr;
	dpid.convertToString(cstr);
    if (Java::DEBUG) std::cout << "convertToJava:DpIdentifier=" << cstr << std::endl;
	if (cstr.c_str() == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE,
			NAME, "convertToJava", CharString("Error in converting DpIdentifier to string!"));
	}
	else {
		// set value of dpid object
		jstr = env->NewStringUTF(cstr);
		env->CallVoidMethod(objDpId, cdpid->SetName(), jstr);
		env->DeleteLocalRef(jstr);
	}

	if (cdpidTmp) delete cdpid;

	return objDpId;
}

jobject Java::convertToJava(JNIEnv *env, VariablePtr varptr, JDpIdentifierClass *cdpid, JVariableClass *cvar)
{
	TimeVar t1;
	jobject res;
	bool cvarTmp=false;

	if (varptr == NULL) { // can happen, for example if a datapoint is deleted we get a hotlink with NULL as value
		return 0;
	}

	if (!cvar) {
		cvar = new JVariableClass(env);	
		cvarTmp = true;
	}

	jclass clsVariable = cvar->Class();

	// create Variable object	
	switch (varptr->isA())
	{
	case DPIDENTIFIER_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:DPIDENTIFIER_VAR" << std::endl;
		DpIdentifierVar value = ((DpIdentifierVar*)varptr)->getValue();
		res = convertToJava(env, value, cdpid);
        if (Java::DEBUG) std::cout << "convertToJava:DPIDENTIFIER_VAR...done" << std::endl;
		break;
	}
	case BIT_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:BIT_VAR" << std::endl;

		bool value = ((BitVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newBitVar(), value);
		break;
	}
	case BIT32_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:BIT32_VAR" << std::endl;

		PVSSulong value = ((Bit32Var*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newBit32Var(), value);
		break;
	}
	case BIT64_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:BIT64_VAR" << std::endl;

		PVSSulonglong value = ((Bit64Var*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newBit64Var(), value);
		break;
	}
	case FLOAT_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:FLOAT_VAR" << std::endl;

		double value = ((FloatVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newFloatVar(), value);
		break;
	}
	case LONG_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:LONG_VAR" << std::endl;

		PVSSlonglong value = ((LongVar*)varptr)->getValue();
		jlong jValue = value;
		res = env->CallStaticObjectMethod(clsVariable, cvar->newLongVar(), jValue);
		break;
	}
    case ULONG_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:ULONG_VAR" << std::endl;

		PVSSlonglong value = ((ULongVar*)varptr)->getValue();
		jlong jValue = value;
		res = env->CallStaticObjectMethod(clsVariable, cvar->newLongVar(), jValue);
		break;
	}
    case BYTE_VAR:	{
        if (Java::DEBUG) std::cout << "convertToJava:BYTE_VAR" << std::endl;

		int value = ((ByteVar*)varptr)->getValue();
        if (Java::DEBUG) std::cout << "convertToJava:BYTE_VAR=" << value << std::endl;
		res = env->CallStaticObjectMethod(clsVariable, cvar->newIntegerVar(), value);
        if (Java::DEBUG) std::cout << "convertToJava:BYTE_VAR...done" << std::endl;
		break;
	}    
    case SHORT_VAR:	{
        if (Java::DEBUG) std::cout << "convertToJava:SHORT_VAR" << std::endl;

		int value = ((ShortVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newIntegerVar(), value);
		break;
	}
    case INTEGER_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:INTEGER_VAR" << std::endl;

		int value = ((IntegerVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newIntegerVar(), value);
		break;
	}
	case UINTEGER_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:UINTEGER_VAR" << std::endl;

		unsigned int value = ((UIntegerVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newUIntegerVar(), value);
		break;
	}
	case CHAR_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:CHAR_VAR" << std::endl;

		PVSSuchar value = ((CharVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newCharVar(), (jchar)value);
		break;
	}
	case TEXT_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:TEXT_VAR" << std::endl;

		const char *value = ((TextVar*)varptr)->getValue();
		jstring jstr = env->NewStringUTF(value);
		jobject jobj = env->CallStaticObjectMethod(clsVariable, cvar->newTextVar(), jstr);
		env->DeleteLocalRef(jstr);
		res = jobj;
		break;
	}
	case LANGTEXT_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:LANGTEXT_VAR" << std::endl;

		jobject jobj = env->CallStaticObjectMethod(clsVariable, cvar->newLangTextVar());

		jclass cls = env->GetObjectClass(jobj);
		jmethodID jm = env->GetMethodID(cls, "setValue", "(ILjava/lang/String;)V");
		
		LangText value = ((LangTextVar*)varptr)->getValue();
		int count = value.getNoOfLanguages();
		for (int langId = 0; langId < count; langId++)
		{			
			CharString text = value.getLText(langId).text;
			jstring jstr = env->NewStringUTF(text.c_str());
			env->CallVoidMethod(jobj, jm, langId, jstr);
			env->DeleteLocalRef(jstr);
		}

		env->DeleteLocalRef(cls);
		res = jobj;
		break;
	}
	case TIME_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:TIME_VAR" << std::endl;

		PVSSTime value = ((TimeVar*)varptr)->getValue();
		//jlong ms = (jlong)(value.getDouble() * 1000);
		jlong ms = value.getSeconds() * 1000 + value.getMilliseconds();
        if (Java::DEBUG) std::cout << "convertToJava:TIME_VAR=" << ms << std::endl;
		jobject jobj = env->CallStaticObjectMethod(clsVariable, cvar->newTimeVar(), ms);
        if (Java::DEBUG) std::cout << "convertToJava:TIME_VAR...done" << std::endl;
		res = jobj;        
		break;
	}
	case DYNANYTYPE_VAR: 
	case DYNTEXT_VAR:
	case DYNTIME_VAR:
	case DYNFLOAT_VAR:
	case DYNBIT_VAR:
	case DYNBIT32_VAR:
	case DYNBIT64_VAR:
    case DYNBYTE_VAR:
    case DYNSHORT_VAR:
	case DYNINTEGER_VAR:
	case DYNUINTEGER_VAR:
	case DYNLONG_VAR:
	case DYNULONG_VAR:
	case DYNDPIDENTIFIER_VAR:
	case DYNLANGTEXT_VAR:    
	{
        if (Java::DEBUG) std::cout << "convertToJava:DYN" << std::endl;

		// gettimeofday(&tp, NULL); long int ms1 = tp.tv_usec; 
		//jclass clsDynVar = env->FindClass(DynVarClassName);
		//jm = env->GetStaticMethodID(clsVariable, "newDynVar", "()Lat/rocworks/oa4j/var/Variable;");
		//jclass clsDynVar = cvar->ClassDynVar();		

		int size = ((DynVar*)varptr)->getArrayLength();

		jobject jdyn = env->CallStaticObjectMethod(clsVariable, cvar->newDynVarSized(), size);
		jobject jvar;
		// gettimeofday(&tp, NULL); long int ms2 = tp.tv_usec;

		//for (Variable *var = ((DynVar*)varptr)->getFirstVar(); var; var = ((DynVar*)varptr)->getNextVar())
        unsigned int len = ((DynVar*)varptr)->getArrayLength();
        for (unsigned int i=0; i<len; i++)
		{
            Variable *var = ((DynVar*)varptr)->getAt(i);
			switch (varptr->isA()) {
			case DYNANYTYPE_VAR: {
				Variable *ptr = ((AnyTypeVar*)var)->getVar(); // 17.07.2016: a new created datapoint element is NULL
				jvar = ptr ? convertToJava(env, ((AnyTypeVar*)var)->getVar(), cdpid, cvar) : NULL;
				break;
			}
			case DYNTEXT_VAR: jvar = convertToJava(env, (TextVar*)var, cdpid, cvar); break;
			case DYNTIME_VAR: jvar = convertToJava(env, (TimeVar*)var, cdpid, cvar); break;
			case DYNFLOAT_VAR: jvar = convertToJava(env, (FloatVar*)var, cdpid, cvar); break;
			case DYNBIT_VAR: jvar = convertToJava(env, (BitVar*)var, cdpid, cvar); break;
			case DYNBIT32_VAR: jvar = convertToJava(env, (Bit32Var*)var, cdpid, cvar); break;
			case DYNBIT64_VAR: jvar = convertToJava(env, (Bit64Var*)var, cdpid, cvar); break;
            case DYNSHORT_VAR: jvar = convertToJava(env, (ShortVar*)var, cdpid, cvar); break;
            case DYNBYTE_VAR: jvar = convertToJava(env, (ByteVar*)var, cdpid, cvar); break;
			case DYNINTEGER_VAR: jvar = convertToJava(env, (IntegerVar*)var, cdpid, cvar); break;
			case DYNUINTEGER_VAR: jvar = convertToJava(env, (UIntegerVar*)var, cdpid, cvar); break;
			case DYNLONG_VAR: jvar = convertToJava(env, (LongVar*)var, cdpid, cvar); break;
			case DYNULONG_VAR: jvar = convertToJava(env, (ULongVar*)var, cdpid, cvar); break;
			case DYNDPIDENTIFIER_VAR: jvar = convertToJava(env, (DpIdentifierVar*)var, cdpid, cvar); break;
			case DYNLANGTEXT_VAR: jvar = convertToJava(env, (LangTextVar*)var, cdpid, cvar); break;
			default: 
                if (Java::DEBUG) std::cout << "convertToJava:dynvariable type not implemented " << varptr->isA() << std::endl;            
                jvar = NULL;                
			}
			//jm = env->GetMethodID(clsDynVar, "add", "(Lat/rocworks/oa4j/var/Variable;)V");
			env->CallVoidMethod(jdyn, cvar->addDynVar(), jvar);
			if ( jvar != NULL ) env->DeleteLocalRef(jvar);
		}

    	//env->DeleteLocalRef(clsDynVar);
		res = jdyn;
		break;
	}
	case DYNDYNANYTYPE_VAR: {
        if (Java::DEBUG) std::cout << "convertToJava:DYNDYN" << std::endl;

		//jclass clsDynVar = env->FindClass(DynVarClassName);
		//jm = env->GetStaticMethodID(clsVariable, "newDynVar", "()Lat/rocworks/oa4j/var/Variable;");
		//jclass clsDynVar = cvar->ClassDynVar();
		int size = ((DynVar*)varptr)->getArrayLength();
		jobject jydyn = env->CallStaticObjectMethod(clsVariable, cvar->newDynVarSized(), size);
		jobject jxdyn;

        unsigned int len = ((DynVar*)varptr)->getArrayLength();
        for (unsigned int y=0; y<len; y++)
		{
            Variable *yvar = ((DynVar*)varptr)->getAt(y);
			if (yvar->isA() != DYNANYTYPE_VAR)
			{
				ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE,
					NAME, "convertToJava", CharString("dyndynanytype element is not of type dynanytype ") + (Variable::getTypeName(yvar->isA())));
				continue;
			}

			jxdyn = convertToJava(env, yvar, cdpid, cvar);
			//jm = env->GetMethodID(clsDynVar, "add", "(Lat/rocworks/oa4j/var/Variable;)V");
			env->CallVoidMethod(jydyn, cvar->addDynVar(), jxdyn);
		}
		//env->DeleteLocalRef(clsDynVar);
		res = jydyn;
		break;
	}
	default:
        if (Java::DEBUG) std::cout << "convertToJava:variable type not implemented " << varptr->isA() << std::endl;
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE,
			NAME, "convertToJava", CharString("variable type not implemented ") + (Variable::getTypeName(varptr->isA())));
		res = NULL;
		break;
	}	

	TimeVar t2;

	PVSSdouble d = t2.getDouble() - t1.getDouble();
	if (d > 1) // Seconds
	{
		ErrHdl::error(ErrClass::PRIO_WARNING, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE, NAME, "convertToJava", 
		CharString("convert to java took long time [") + (Variable::getTypeName(varptr->isA())) + CharString("] ")+CharString((long)(d*1000))+CharString(" ms"));
	}

	if (cvarTmp) delete cvar;

	return res;
}

jstring Java::convertToJava(JNIEnv *env, CharString *str)
{
	return env->NewStringUTF(str->c_str());
}

jstring Java::convertToJava(JNIEnv *env, const CharString &str)
{
	return env->NewStringUTF(str.c_str());
}

VariablePtr Java::convertJVariable(JNIEnv *env, jobject jVariable)
{
	jmethodID jm;

	jclass clsVariable = env->FindClass(VariableClassName);

	jm = env->GetMethodID(clsVariable, "getVariableTypeAsNr", "()I");
	jint jTypeNr = env->CallIntMethod(jVariable, jm);

	jm = env->GetMethodID(clsVariable, "getValueObject", "()Ljava/lang/Object;");
	jobject jValueObject = env->CallObjectMethod(jVariable, jm);

	env->DeleteLocalRef(clsVariable);	

	VariablePtr varPtr = NULL;

    if (Java::DEBUG) std::cout << "convertJVariable:" << jTypeNr << std::endl;

	switch (jTypeNr) {
	case /*ANYTYPE_VAR*/ JAnyTypeVar:  {
        if (Java::DEBUG) std::cout << "convertJVariable:ANYTYPE_VAR" << std::endl;
        break;
    }
	case /*BIT32_VAR*/ JBit32Var:  {
        if (Java::DEBUG) std::cout << "convertJVariable:BIT32_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Long");
		jm = env->GetMethodID(cls, "longValue", "()J");
		jlong jValue = env->CallLongMethod(jValueObject, jm);
		PVSSulong pValue = (PVSSulong)jValue;
		Bit32Var *var = new Bit32Var(pValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*BIT64_VAR*/ JBit64Var: {
        if (Java::DEBUG) std::cout << "convertJVariable:BIT64_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Long");
		jm = env->GetMethodID(cls, "longValue", "()J");
		jlong jValue = env->CallLongMethod(jValueObject, jm);
		PVSSulonglong pValue = (PVSSulonglong)jValue;
		Bit64Var *var = new Bit64Var(pValue);

		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*BIT_VAR*/ JBitVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:BIT_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Boolean");
		jm = env->GetMethodID(cls, "booleanValue", "()Z");
		jboolean jValue = env->CallBooleanMethod(jValueObject, jm);
		BitVar *var = new BitVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*BLOB_VAR*/ JBlobVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:BLOB_VAR" << std::endl;
        break;
    }
	case /*CHAR_VAR*/ JCharVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:CHAR_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Character");
		jm = env->GetMethodID(cls, "charValue", "()C");
		jchar jValue = env->CallCharMethod(jValueObject, jm);
		CharVar *var = new CharVar((PVSSuchar)jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*DPIDENTIFIER_VAR*/ JDpIdentifierVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:DPIDENTIFIER_VAR" << std::endl;
		//jclass cls = env->FindClass(DpIdentifierClassName);
		//jm = env->GetMethodID(cls, "getName", "()Ljava/lang/String;");
		//jstring jValue = (jstring)env->CallObjectMethod(jValueObject, jm);
		jstring jValue = (jstring)jValueObject;

		CharString *dpName = convertJString(env, jValue);
		env->DeleteLocalRef(jValue);

		DpIdentifier dpId;
		bool ok = (Java::getId(*dpName, dpId) == PVSS_TRUE);
		if (!ok)
		{
			ErrHdl::error(ErrClass::PRIO_SEVERE,ErrClass::ERR_PARAM,ErrClass::UNEXPECTEDSTATE, NAME,              
				"convertJVariable::DpIdentifierVar", CharString("Unknown Datapoint ") + (*dpName));
		}
		//env->DeleteLocalRef(cls);
		delete dpName;
		varPtr = new DpIdentifierVar(dpId);
		break;
	}
	case /*DYN_VAR*/ JDynVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:DYN_VAR" << std::endl;
		jclass cls = env->GetObjectClass(jVariable);

		// get size of dyn
		jm = env->GetMethodID(cls, "size", "()I");
		jint size = env->CallIntMethod(jVariable, jm);

		// get type of dyn
		jm = env->GetMethodID(cls, "getElementsTypeAsNr", "()I");
		jint type = env->CallIntMethod(jVariable, jm);
				
		if (type==0/*unknown/undefined*/) type=ANYTYPE_VAR;
		DynVar *var = new DynVar((VariableType)type);

		jobject jValue;
		VariablePtr varPtrTmp;
		jm = env->GetMethodID(cls, "get", "(I)Lat/rocworks/oa4j/var/Variable;");
		for (jint i = 0; i < size; i++) {
			jValue = env->CallObjectMethod(jVariable, jm, i);
			varPtrTmp = Java::convertJVariable(env, jValue);
			var->append(type == ANYTYPE_VAR ? new AnyTypeVar(varPtrTmp) : varPtrTmp);
			env->DeleteLocalRef(jValue);
		}

		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*FLOAT_VAR*/ JFloatVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:FLOAT_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Double");
		jm = env->GetMethodID(cls, "doubleValue", "()D");
		jdouble jValue = env->CallDoubleMethod(jValueObject, jm);
		FloatVar *var = new FloatVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*INTEGER_VAR*/ JIntegerVar:
	case /*UINTEGER_VAR*/ JUIntegerVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:INTEGER_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Integer");
		jm = env->GetMethodID(cls, "intValue", "()I");
		jint jValue = env->CallIntMethod(jValueObject, jm);
		IntegerVar *var = new IntegerVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*LANGTEXT_VAR*/ JLangTextVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:LANGTEXT_VAR" << std::endl;        
        break;
    }
	case /*TEXT_VAR*/ JTextVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:TEXT_VAR" << std::endl;
		jstring jValue = (jstring)jValueObject;
		CharString *cValue = convertJString(env, jValue); // TODO inefficient! copy string twice...
		TextVar *var = new TextVar(*cValue);
		delete cValue;
		env->DeleteLocalRef(jValue);
		varPtr = var;
		break;
	}
	case /*TIME_VAR*/ JTimeVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:TIME_VAR" << std::endl;
		jclass cls = env->FindClass("java/util/Date");
		jm = env->GetMethodID(cls, "getTime", "()J");
		jlong jValue = env->CallLongMethod(jValueObject, jm);
		PVSSTime time;
		time.setDouble(((PVSSdouble)jValue) / 1000.0);
		TimeVar *var = new TimeVar(time);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	case /*LONG_VAR*/ JLongVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:LONG_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Integer");
		jm = env->GetMethodID(cls, "intValue", "()I");
		jint jValue = env->CallIntMethod(jValueObject, jm);
		LongVar *var = new LongVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);		
		break;
	}
	case /*ULONG_VAR*/ JULongVar: {
        if (Java::DEBUG) std::cout << "convertJVariable:ULONG_VAR" << std::endl;
		jclass cls = env->FindClass("java/lang/Integer");
		jm = env->GetMethodID(cls, "intValue", "()I");
		jint jValue = env->CallIntMethod(jValueObject, jm);
		ULongVar *var = new ULongVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		break;
	}
	default: break;
	}

	env->DeleteLocalRef(jValueObject);

	if (varPtr == NULL)
	{
		ErrHdl::error(ErrClass::PRIO_SEVERE,
			ErrClass::ERR_PARAM,
			ErrClass::UNEXPECTEDSTATE,
			NAME,
			"convertJVariable",
			CharString("Unknown Datatype ") + jTypeNr);
	}

	return varPtr;
}

CharString *Java::convertJString(JNIEnv *env, jstring jString)
{
	const char *cString = env->GetStringUTFChars(jString, 0);
	CharString *dpName = new CharString(cString);
	env->ReleaseStringUTFChars(jString, cString);   //Informs the VM that the native code no longer needs access to utf. 
	return dpName;
}

DpIdentList *Java::convertJArrayOfStringToDpIdentList(JNIEnv *env, jobjectArray dps)
{
	DpIdentList *dpList = new DpIdentList();
	int len = env->GetArrayLength(dps);
	for (int i = 0; i < len; i++)
	{
		jobject jdp = env->GetObjectArrayElement(dps, i);
		CharString *dpName = convertJString(env, (jstring)jdp);
		env->DeleteLocalRef(jdp);
		DpIdentifier dpId;   // DP to connect to
		bool ok = (Java::getId(*dpName, dpId) == PVSS_TRUE);
		if (!ok)
		{
			// This name was unknown.
			// The parameters are in Bascis/ErrClass.hxx
			ErrHdl::error(ErrClass::PRIO_SEVERE,      // It is a severe error
				ErrClass::ERR_PARAM,        // wrong name: blame others
				ErrClass::UNEXPECTEDSTATE,  // fits all
				NAME,              // our file name
				"convertJArrayOfStringToDpIdentList",                      // our function name
				CharString("Unknown Datapoint ") + (*dpName));
		}
		else
		{
			dpList->append(dpId);
		}
		delete dpName;
	}
	return dpList;
}

bool Java::convertJDpIdentifierToDpIdentifier(JNIEnv *env, jobject dp, DpIdentifier &dpid)
{
	jclass cls = env->GetObjectClass(dp);
	jmethodID jm = env->GetMethodID(cls, "getName", "()Ljava/lang/String;");
	jstring jName = (jstring)env->CallObjectMethod(dp, jm);
	CharString *dpName = convertJString(env, (jstring)jName);
	env->DeleteLocalRef(jName); 
	bool ok = (Java::getId(*dpName, dpid) == PVSS_TRUE);
	if ( !ok )
	{
		// This name was unknown.
		ErrHdl::error(ErrClass::PRIO_SEVERE,      // It is a severe error
			ErrClass::ERR_PARAM,        // wrong name: blame others
			ErrClass::UNEXPECTEDSTATE,  // fits all
			NAME,              // our file name
			"convertJDpIdentifierToDpIdentifier",  // our function name
			CharString("Unknown Datapoint ") + (*dpName));
	}
	env->DeleteLocalRef(cls);
	delete dpName;
	return ok;
}

DpIdentList *Java::convertJArrayOfDpIdentifierToDpIdentList(JNIEnv *env, jobjectArray dps)
{
	DpIdentList *dpList = new DpIdentList();
	int len = env->GetArrayLength(dps);
	for (int i = 0; i < len; i++)
	{
		jclass cls = env->FindClass(DpIdentifierClassName);
		jobject jdpid = env->GetObjectArrayElement(dps, i);

		DpIdentifier dpid;
		if (convertJDpIdentifierToDpIdentifier(env, jdpid, dpid))
			dpList->append(dpid);

		env->DeleteLocalRef(jdpid);
		env->DeleteLocalRef(cls);
	}
	return dpList;
}

DpIdValueList *Java::convertJArrayOfDpVCItemToDpIdValueList(JNIEnv *env, jobjectArray dps)
{
	DpIdValueList *dpList = new DpIdValueList();

	jclass cls;
	jmethodID jm;

	// create Variable object	
	cls = env->FindClass(DpVCItemClassName);

	int len = env->GetArrayLength(dps);
	for (int i = 0; i < len; i++)
	{
		jobject jDpVCItem = env->GetObjectArrayElement(dps, i);

		// DpIdentifier
		jm = env->GetMethodID(cls, "getDpName", "()Ljava/lang/String;");
		jstring jDpName = (jstring)env->CallObjectMethod(jDpVCItem, jm);
		CharString *dpName = convertJString(env, jDpName);
		env->DeleteLocalRef(jDpName);

		//const char *cDpName = env->GetStringUTFChars(jDpName, 0);
		//CharString *dpName = new CharString(cDpName);
		//env->ReleaseStringUTFChars(jDpName, cDpName);   //Informs the VM that the native code no longer needs access to utf. 

		DpIdentifier dpId;
		bool ok = (Java::getId(*dpName, dpId) == PVSS_TRUE);
		if (!ok)
		{
			ErrHdl::error(ErrClass::PRIO_SEVERE,
				ErrClass::ERR_PARAM,
				ErrClass::UNEXPECTEDSTATE,
				NAME,
				"convertJArrayOfDpVCItemToDpIdValueList",
				CharString("Unknown Datapoint ") + (*dpName));
		}
		else
		{
			// Variable
			jm = env->GetMethodID(cls, "getVariable", "()Lat/rocworks/oa4j/var/Variable;");
			jobject jVariable = env->CallObjectMethod(jDpVCItem, jm);
			VariablePtr varPtr = convertJVariable(env, jVariable);
			env->DeleteLocalRef(jVariable);

			// Add DpIdentifier+Variable to dpList
			dpList->appendItem(dpId, varPtr);
		}
		env->DeleteLocalRef(jDpVCItem);
		delete dpName;
	}
	env->DeleteLocalRef(cls);
	return dpList;
}

PVSSboolean Java::getId(const char *name, DpIdentifier &dpId)
{
	dpIdMutex.lock(); // 28.07.2016 needed, because of parallel queries and parallel inserts into result group
	PVSSboolean ret = Manager::getId(name, dpId);
	dpIdMutex.unlock();
	return ret;
}

PVSSboolean Java::getTypeName(DpTypeId typeId, CharString &typeName, SystemNumType sysNum)
{
	dpIdMutex.lock();
	PVSSboolean ret = Manager::getTypeName(typeId, typeName, sysNum);
	dpIdMutex.unlock();
	return ret;
}
