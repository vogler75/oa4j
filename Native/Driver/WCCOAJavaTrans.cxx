// Our transformation class PVSS <--> Hardware
#include <WCCOAJavaTrans.hxx>
#include <WCCOAJavaDrv.hxx>
#include <ErrHdl.hxx>     // The Error handler Basics/Utilities
#include <TextVar.hxx>    // Our Variable type Basics/Variables
#include <IntegerVar.hxx>

#include <stdio.h>

//----------------------------------------------------------------------------


WCCOAJavaTrans::WCCOAJavaTrans(const CharString& name, TransformationType type)
{
	WCCOAJavaTrans::name = CharString(name);
	WCCOAJavaTrans::type = type;
	//std::cout << "newTrans" << std::endl;
	jTransObject = WCCOAJavaDrv::thisManager->JavaTransformationNewObject(this, name, type);
	if (jTransObject == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			WCCOAJavaDrv::ManagerName, "WCCOAJavaTrans::WCCOAJavaTrans", CharString("no transformation object!"));

	}
}

WCCOAJavaTrans::~WCCOAJavaTrans()
{
	//std::cout << "delTrans" << std::endl;
	WCCOAJavaDrv::thisManager->JavaTransformationDelObject(jTransObject);
}


//----------------------------------------------------------------------------

TransformationType WCCOAJavaTrans::isA() const
{
	return type;
}

//----------------------------------------------------------------------------

TransformationType WCCOAJavaTrans::isA(TransformationType type) const
{
	if (type == isA())
		return type;
	else
		return Transformation::isA(type);
}

//----------------------------------------------------------------------------

Transformation *WCCOAJavaTrans::clone() const
{
	return new WCCOAJavaTrans(name, type);
}

//----------------------------------------------------------------------------
// Our item size. The max we will use is 256 Bytes.
// This is an arbitrary value! A Transformation for a long e.g. would return 4

int WCCOAJavaTrans::itemSize() const
{
	if (jTransObject == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			WCCOAJavaDrv::ManagerName, "WCCOAJavaTrans::itemSize()", CharString("no transformation object!"));
		return 0;
	}
	else {
		return WCCOAJavaDrv::thisManager->JavaTransformationGetSize(jTransObject);
	}
}

//----------------------------------------------------------------------------
// Our preferred Variable type. Data will be converted to this type
// before toPeriph is called.

VariableType WCCOAJavaTrans::getVariableType() const
{
	if (jTransObject == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			WCCOAJavaDrv::ManagerName, "WCCOAJavaTrans::getVariableType()", CharString("no transformation object!"));
		return VARIABLE;
	}
	else {
		return WCCOAJavaDrv::thisManager->JavaTransformationGetVariableType(jTransObject);
	}

}

//----------------------------------------------------------------------------
// Convert data from PVSS to Hardware.
// In this example the subindex is ignored

PVSSboolean WCCOAJavaTrans::toPeriph(PVSSchar *buffer, PVSSushort len,
	const Variable &var, const PVSSushort subix) const
{	
	//sprintf(reinterpret_cast<char *>(buffer), "%s", static_cast<const TextVar &>(var).getValue());
	if (jTransObject == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			WCCOAJavaDrv::ManagerName, "WCCOAJavaTrans::itemSize()", CharString("no transformation object!"));
		return PVSS_FALSE;
	}
	else {
		return WCCOAJavaDrv::thisManager->JavaTransformationToPeriph(jTransObject, buffer, len, var, subix);
	}
}

//----------------------------------------------------------------------------
// Conversion from Hardware to PVSS

VariablePtr WCCOAJavaTrans::toVar(const PVSSchar *buffer, const PVSSushort dlen,
	const PVSSushort subix ) const
{
	// TODO  everything in here has to be adapted to your needs

	// Return pointer to new PVSS Variable
	//Variable *var = Variable::allocate(varType);
	//(*var) = TextVar(reinterpret_cast<const char *>(buffer));   // virtual operator= in all Variables
	if (jTransObject == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_IMPL, ErrClass::UNEXPECTEDSTATE,
			WCCOAJavaDrv::ManagerName, "WCCOAJavaTrans::itemSize()", CharString("no transformation object!"));
		return NULL;
	}
	else {
		return WCCOAJavaDrv::thisManager->JavaTransformationToVar(jTransObject, buffer, dlen, subix);
	}	
}

//----------------------------------------------------------------------------
