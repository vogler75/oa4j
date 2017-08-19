#include <../LibJava/Java.hxx>
#include <Manager.hxx>
#include <Mutex.hxx>

#include <DpVCItem.hxx>               

#include <DpIdentifier.hxx>
#include <DpIdentifierVar.hxx>

#include <Variable.hxx>
#include <DynVar.hxx>
#include <AnyTypeVar.hxx>

#include <Bit32Var.hxx>
#include <Bit64Var.hxx>
#include <FloatVar.hxx>
#include <IntegerVar.hxx>
#include <UIntegerVar.hxx>
#include <TextVar.hxx>
#include <CharVar.hxx>
#include <LongVar.hxx>
#include <ULongVar.hxx>
#include <TimeVar.hxx>

#include <sys/time.h>

#include <Mutex.hxx>

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
}

JVariableClass::~JVariableClass()
{
	env->DeleteLocalRef(cls);
	env->DeleteLocalRef(clsDynVar);
}


//---------------------------------------------------------------------------------------------------------
const char *Java::NAME = "java";
const bool Java::DEBUG = false;

const char *Java::DpIdentifierClassName = "at/rocworks/oa4j/var/DpIdentifierVar";
const char *Java::VariableClassName = "at/rocworks/oa4j/var/Variable";
const char *Java::DynVarClassName = "at/rocworks/oa4j/var/DynVar";
const char *Java::DpVCItemClassName = "at/rocworks/oa4j/base/JDpVCItem";

Mutex Java::dpIdMutex;

jclass Java::FindClass(JNIEnv *env, const char* name)
{
	jclass jClass = env->FindClass(name);
	if (jClass == nil) {
		std::string msg = "class " + std::string(name) + " not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			NAME, "FindClass", msg.c_str());
	}
	return jClass;
}

jmethodID Java::GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig)
{
	jmethodID jMethod = env->GetMethodID(clazz, name, sig);
	if (jMethod == nil) {
		std::string msg = "java method " + std::string(name) + std::string(sig) + " not found";
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			NAME, "GetMethodID", msg.c_str());
	}
	return jMethod;
}

jmethodID Java::GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig)
{
	jmethodID jMethod = env->GetStaticMethodID(clazz, name, sig);
	if (jMethod == nil) {
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

	if (DEBUG) std::cout << "convertToJava::DpIdentifier " << dpid << std::endl;

	if (!cdpid) { 
		cdpid = new JDpIdentifierClass(env);
		cdpidTmp = true;
	}

	// create dpid object
	objDpId = env->NewObject(cdpid->Class(), cdpid->Init());

	CharString cstr;
	dpid.convertToString(cstr);
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

	if (!cvar) {
		cvar = new JVariableClass(env);	
		cvarTmp = true;
	}

	jclass clsVariable = cvar->Class();

	// create Variable object	
	if (DEBUG) std::cout << "convertToJava " << varptr->getTypeName(varptr->isA()) << std::endl;
	switch (varptr->isA())
	{
	case DPIDENTIFIER_VAR: {
		DpIdentifierVar value = ((DpIdentifierVar*)varptr)->getValue();

		//jm = env->GetStaticMethodID(clsVariable, "newDpIdentifierVar", "(Lat/rocworks/oa4j/var/DpIdentifierVar;)Lat/rocworks/oa4j/var/Variable;");		
		//jobject jValue = convertToJava(env, value);
		//res = env->CallStaticObjectMethod(clsVariable, jm, jValue);

		res = convertToJava(env, value, cdpid);
		break;
	}
	case BIT_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newBitVar", "(Z)Lat/rocworks/oa4j/var/Variable;");
		bool value = ((BitVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newBitVar(), value);
		break;
	}
	case BIT32_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newBit32Var", "(J)Lat/rocworks/oa4j/var/Variable;");
		PVSSulong value = ((Bit32Var*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newBit32Var(), value);
		break;
	}
	case BIT64_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newBit64Var", "(J)Lat/rocworks/oa4j/var/Variable;");
		PVSSulonglong value = ((Bit64Var*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newBit64Var(), value);
		break;
	}
	case FLOAT_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newFloatVar", "(D)Lat/rocworks/oa4j/var/Variable;");
		double value = ((FloatVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newFloatVar(), value);
		break;
	}
	case LONG_VAR: case ULONG_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newLongVar", "(J)Lat/rocworks/oa4j/var/Variable;");
		PVSSlonglong value = ((LongVar*)varptr)->getValue();
		jlong jValue = value;
		res = env->CallStaticObjectMethod(clsVariable, cvar->newLongVar(), jValue);
		break;
	}
	case INTEGER_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newIntegerVar", "(I)Lat/rocworks/oa4j/var/Variable;");
		int value = ((IntegerVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newIntegerVar(), value);
		break;
	}
	case UINTEGER_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newUIntegerVar", "(I)Lat/rocworks/oa4j/var/Variable;");
		unsigned int value = ((UIntegerVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newUIntegerVar(), value);
		break;
	}
	case CHAR_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newCharVar", "(C)Lat/rocworks/oa4j/var/Variable;");
		PVSSuchar value = ((CharVar*)varptr)->getValue();
		res = env->CallStaticObjectMethod(clsVariable, cvar->newCharVar(), (jchar)value);
		break;
	}
	case TEXT_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newTextVar", "(Ljava/lang/String;)Lat/rocworks/oa4j/var/Variable;");
		const char *value = ((TextVar*)varptr)->getValue();
		jstring jstr = env->NewStringUTF(value);
		jobject jobj = env->CallStaticObjectMethod(clsVariable, cvar->newTextVar(), jstr);
		env->DeleteLocalRef(jstr);
		res = jobj;
		break;
	}
	case LANGTEXT_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newLangTextVar", "()Lat/rocworks/oa4j/var/Variable;");		
		jobject jobj = env->CallStaticObjectMethod(clsVariable, cvar->newLangTextVar());

		jclass cls = env->GetObjectClass(jobj);
		jmethodID jm = env->GetMethodID(cls, "setValue", "(ILjava/lang/String;)V");
		
		LangText value = ((LangTextVar*)varptr)->getValue();
		int count = value.getNoOfLanguages();
		for (int langId = 0; langId < count; langId++)
		{			
			CharString text = value.getText(langId);
			if (DEBUG) std::cout << "convertToJava " << langId << " : " << text << std::endl;
			jstring jstr = env->NewStringUTF(text.c_str());
			env->CallVoidMethod(jobj, jm, langId, jstr);
			env->DeleteLocalRef(jstr);
		}

		env->DeleteLocalRef(cls);
		res = jobj;
		break;
	}
	case TIME_VAR: {
		//jm = env->GetStaticMethodID(clsVariable, "newTimeVar", "(J)Lat/rocworks/oa4j/var/Variable;");
		PVSSTime value = ((TimeVar*)varptr)->getValue();
		jlong ms = (jlong)(value.getDouble() * 1000);
		//std::cout << "TIME_VAR=" << ms << std::endl;
		jobject jobj = env->CallStaticObjectMethod(clsVariable, cvar->newTimeVar(), ms);
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
	case DYNINTEGER_VAR:
	case DYNUINTEGER_VAR:
	case DYNLONG_VAR:
	case DYNULONG_VAR:
	case DYNDPIDENTIFIER_VAR:
	case DYNLANGTEXT_VAR:    
	{
		// gettimeofday(&tp, NULL); long int ms1 = tp.tv_usec; 
		//jclass clsDynVar = env->FindClass(DynVarClassName);
		//jm = env->GetStaticMethodID(clsVariable, "newDynVar", "()Lat/rocworks/oa4j/var/Variable;");
		//jclass clsDynVar = cvar->ClassDynVar();		

		int size = ((DynVar*)varptr)->getArrayLength();

		jobject jdyn = env->CallStaticObjectMethod(clsVariable, cvar->newDynVarSized(), size);
		jobject jvar;
		// gettimeofday(&tp, NULL); long int ms2 = tp.tv_usec;

		int i = 0;
		for (Variable *var = ((DynVar*)varptr)->getFirstVar(); var; var = ((DynVar*)varptr)->getNextVar())
		{
			i++;
			if (DEBUG) std::cout << "i: " << i << ": " << var->getTypeName(var->isA()) << ": " << varptr->getTypeName(varptr->isA()) << std::endl;
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
			case DYNINTEGER_VAR: jvar = convertToJava(env, (TimeVar*)var, cdpid, cvar); break;
			case DYNUINTEGER_VAR: jvar = convertToJava(env, (UIntegerVar*)var, cdpid, cvar); break;
			case DYNLONG_VAR: jvar = convertToJava(env, (LongVar*)var, cdpid, cvar); break;
			case DYNULONG_VAR: jvar = convertToJava(env, (ULongVar*)var, cdpid, cvar); break;
			case DYNDPIDENTIFIER_VAR: jvar = convertToJava(env, (DpIdentifierVar*)var, cdpid, cvar); break;
			case DYNLANGTEXT_VAR: jvar = convertToJava(env, (LangTextVar*)var, cdpid, cvar); break;
			default: jvar = NULL;
			}
			//jm = env->GetMethodID(clsDynVar, "add", "(Lat/rocworks/oa4j/var/Variable;)V");
			env->CallVoidMethod(jdyn, cvar->addDynVar(), jvar);
			if ( jvar != NULL ) env->DeleteLocalRef(jvar);
		}

		//gettimeofday(&tp, NULL); long int ms3 = tp.tv_usec;
		//std::cout << "convertToJava " << varptr->getTypeName(varptr->isA()) << "...done: " << i<< ": " << (ms2-ms1) << "/" << (ms3-ms2) << std::endl;

		//env->DeleteLocalRef(clsDynVar);
		res = jdyn;
		break;
	}
	case DYNDYNANYTYPE_VAR: {
		//jclass clsDynVar = env->FindClass(DynVarClassName);
		//jm = env->GetStaticMethodID(clsVariable, "newDynVar", "()Lat/rocworks/oa4j/var/Variable;");
		//jclass clsDynVar = cvar->ClassDynVar();
		int size = ((DynVar*)varptr)->getArrayLength();
		jobject jydyn = env->CallStaticObjectMethod(clsVariable, cvar->newDynVarSized(), size);
		jobject jxdyn;
		int y = 0;
		for (Variable *yvar = ((DynVar*)varptr)->getFirstVar(); yvar; yvar = ((DynVar*)varptr)->getNextVar())
		{
			y++;
			if (DEBUG) std::cout << "y: " << y << ": " << yvar->getTypeName(yvar->isA()) << std::endl;

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
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE,
			NAME, "convertToJava", CharString("variable type not implemented ") + (Variable::getTypeName(varptr->isA())));
		res = NULL;
		break;
	}

	if (DEBUG) std::cout << "convertToJava " << varptr->getTypeName(varptr->isA()) << "...done" << std::endl;

	TimeVar t2;

	PVSSdouble d = t2.getDouble() - t1.getDouble();
	if (d > 0.1)
	{
		std::cout << "convertToJava " << varptr->getTypeName(varptr->isA()) << "...done in " << d << std::endl;
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

	if (DEBUG) std::cout << "convertJVariable:getVariableTypeAsNr..." << std::endl;
	jm = env->GetMethodID(clsVariable, "getVariableTypeAsNr", "()I");
	jint jTypeNr = env->CallIntMethod(jVariable, jm);

	if (DEBUG) std::cout << "convertJVariable:getValueObject..." << std::endl;
	jm = env->GetMethodID(clsVariable, "getValueObject", "()Ljava/lang/Object;");
	jobject jValueObject = env->CallObjectMethod(jVariable, jm);

	env->DeleteLocalRef(clsVariable);

	if (DEBUG) std::cout << "convertJVariable type:" << jTypeNr << std::endl;

	VariablePtr varPtr = NULL;

	switch (jTypeNr) {
	case ANYTYPE_VAR /*AnyTypeVar*/: break;
	case BIT32_VAR /*Bit32Var */:  {
		if (DEBUG) std::cout << "convertJVariable:13:Bit32Var" << std::endl;
		jclass cls = env->FindClass("java/lang/Long");
		jm = env->GetMethodID(cls, "longValue", "()J");
		jlong jValue = env->CallLongMethod(jValueObject, jm);
		PVSSulong pValue = (PVSSulong)jValue;
		Bit32Var *var = new Bit32Var(pValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "Bit32Var=" << var << std::endl;		
		break;
	}
	case BIT64_VAR /*Bit64Var */: {
		if (DEBUG) std::cout << "convertJVariable:13:Bit64Var" << std::endl;
		jclass cls = env->FindClass("java/lang/Long");
		jm = env->GetMethodID(cls, "longValue", "()J");
		jlong jValue = env->CallLongMethod(jValueObject, jm);
		PVSSulonglong pValue = (PVSSulonglong)jValue;
		if (DEBUG) std::cout << "convertJVariable:13:Bit64Var " << jValue << " / " << pValue << std::endl;
		Bit64Var *var = new Bit64Var(pValue);
		if (DEBUG) std::cout << "convertJVariable:13:Bit64Var " << (*var) << std::endl;

		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "Bit64Var=" << var << std::endl;
		break;
	}
	case BIT_VAR /*BitVar */: {
		if (DEBUG) std::cout << "convertJVariable:13:BitVar" << std::endl;
		jclass cls = env->FindClass("java/lang/Boolean");
		jm = env->GetMethodID(cls, "booleanValue", "()Z");
		jboolean jValue = env->CallBooleanMethod(jValueObject, jm);
		BitVar *var = new BitVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "BitVar=" << var << std::endl;
		break;
	}
	case BLOB_VAR /*BlobVar */: break;
	case CHAR_VAR /*CharVar */: {
		if (DEBUG) std::cout << "convertJVariable:6:CharVar" << std::endl;
		jclass cls = env->FindClass("java/lang/Character");
		jm = env->GetMethodID(cls, "charValue", "()C");
		jchar jValue = env->CallCharMethod(jValueObject, jm);
		CharVar *var = new CharVar((PVSSuchar)jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "CharVar=" << var << std::endl;
		break;
	}
	case DPIDENTIFIER_VAR /*DpIdentifierVar */: {
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
		std::cout << "DpIdentifierVar=" << dpId << std::endl;
		//env->DeleteLocalRef(cls);
		delete dpName;
		varPtr = new DpIdentifierVar(dpId);
		break;
	}
	case DYN_VAR /*DynVar */: {
		if (DEBUG) std::cout << "convertJVariable:13:DynVar" << std::endl;
		jclass cls = env->GetObjectClass(jVariable);

		// get size of dyn
		jm = env->GetMethodID(cls, "size", "()I");
		jint size = env->CallIntMethod(jVariable, jm);
		if (DEBUG) std::cout << "convertJVariable:13:DynVar size=" << size << std::endl;

		// get type of dyn
		jm = env->GetMethodID(cls, "getElementsTypeAsNr", "()I");
		jint type = env->CallIntMethod(jVariable, jm);
		if (DEBUG) std::cout << "convertJVariable:13:DynVar type=" << type<< std::endl;
				
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
	case FLOAT_VAR /*FloatVar */: {
		if (DEBUG) std::cout << "convertJVariable:13:FloatVar" << std::endl;
		jclass cls = env->FindClass("java/lang/Double");
		jm = env->GetMethodID(cls, "doubleValue", "()D");
		jdouble jValue = env->CallDoubleMethod(jValueObject, jm);
		FloatVar *var = new FloatVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "FloatVar=" << var->formatValue("%g") << std::endl;
		break;
	}
	case INTEGER_VAR /*IntegerVar */:
	case UINTEGER_VAR /*UIntegerVar */: {
		if (DEBUG) std::cout << "convertJVariable:20:UIntegerVar" << std::endl;
		jclass cls = env->FindClass("java/lang/Integer");
		jm = env->GetMethodID(cls, "intValue", "()I");
		jint jValue = env->CallIntMethod(jValueObject, jm);
		IntegerVar *var = new IntegerVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "UIntegerVar=" << *var << std::endl;
		break;
	}
	case LANGTEXT_VAR /*LangTextVar */: break;
	case TEXT_VAR /*TextVar */: {
		if (DEBUG) std::cout << "convertJVariable:18:TextVar" << std::endl;
		jstring jValue = (jstring)jValueObject;
		CharString *cValue = convertJString(env, jValue); // TODO inefficient! copy string twice...
		TextVar *var = new TextVar(*cValue);
		delete cValue;
		env->DeleteLocalRef(jValue);
		varPtr = var;
		if (DEBUG) std::cout << "TextVar=" << var << std::endl;
		break;
	}
	case TIME_VAR /*TimeVar */: {
		if (DEBUG) std::cout << "convertJVariable:19:TimeVar" << std::endl;
		jclass cls = env->FindClass("java/util/Date");
		jm = env->GetMethodID(cls, "getTime", "()J");
		jlong jValue = env->CallLongMethod(jValueObject, jm);
		if (DEBUG) std::cout << "TimeVar ms=" << jValue << std::endl;
		PVSSTime time;
		time.setDouble(((PVSSdouble)jValue) / 1000.0);
		TimeVar *var = new TimeVar(time);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "TimeVar=" << var->formatValue("") << " ms: " << jValue << std::endl;
		break;
	}
	case LONG_VAR /*LongVar */: {
		if (DEBUG) std::cout << "convertJVariable:21:LongVar" << std::endl;
		jclass cls = env->FindClass("java/lang/Integer");
		jm = env->GetMethodID(cls, "intValue", "()I");
		jint jValue = env->CallIntMethod(jValueObject, jm);
		LongVar *var = new LongVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "LongVar=" << *var << std::endl;
		break;
	}
	case ULONG_VAR /*ULongVar */: {
		if (DEBUG) std::cout << "convertJVariable:21:ULongVar" << std::endl;
		jclass cls = env->FindClass("java/lang/Integer");
		jm = env->GetMethodID(cls, "intValue", "()I");
		jint jValue = env->CallIntMethod(jValueObject, jm);
		ULongVar *var = new ULongVar(jValue);
		varPtr = var;
		env->DeleteLocalRef(cls);
		if (DEBUG) std::cout << "ULongVar=" << *var << std::endl;
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
	if (DEBUG) std::cout << "convertJArrayOfDpIdentifierToDpIdentList " << len << std::endl;
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
	if (DEBUG) std::cout << "convertJArrayOfDpVCItemToDpIdValueList " << len << std::endl;
	for (int i = 0; i < len; i++)
	{
		jobject jDpVCItem = env->GetObjectArrayElement(dps, i);

		// DpIdentifier
		jm = env->GetMethodID(cls, "getDpName", "()Ljava/lang/String;");
		jstring jDpName = (jstring)env->CallObjectMethod(jDpVCItem, jm);
		CharString *dpName = convertJString(env, jDpName);
		env->DeleteLocalRef(jDpName);

		if (DEBUG) std::cout << "convertJArrayOfDpVCItemToDpIdValueList " << i << ": " << dpName << std::endl;

		//const char *cDpName = env->GetStringUTFChars(jDpName, 0);
		//CharString *dpName = new CharString(cDpName);
		//env->ReleaseStringUTFChars(jDpName, cDpName);   //Informs the VM that the native code no longer needs access to utf. 

		//std::cout << "dpName=" << *dpName << std::endl;

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
			//std::cout << "convertJArrayOfDpVCItemToDpIdValueList:getVariable..." << std::endl;
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
