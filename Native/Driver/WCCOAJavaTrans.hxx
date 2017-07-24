#ifndef WCCOAJavaTRANS_H
#define WCCOAJavaTRANS_H

#include <Transformation.hxx>
#include <jni.h>

// Our Transformation class for Text
// As the Transformation really depends on the format of data you send and
// receive in your protocol (see HWService), this template is just an
// example.
// Things you have to change are marked with TODO

class WCCOAJavaTrans : public Transformation
{
public:
	// TODO probably your ctor looks completely different ...
	//WCCOAJavaTrans(TransformationType type) : type(type) {}
	WCCOAJavaTrans(const CharString& name, TransformationType type);
	~WCCOAJavaTrans();

	virtual TransformationType isA() const;
	virtual TransformationType isA(TransformationType type) const;

	// (max) size of one item. This is needed by DrvManager to
	// create the buffer used in toPeriph and by the Low-Level-Compare
	// For our Text-Transformation we set it arbitrarly to 256 Bytes
	virtual int itemSize() const;

	// The type of Variable we are expecting here
	virtual VariableType getVariableType() const;

	// Clone of our class
	virtual Transformation *clone() const;

	// Conversion from PVSS to Hardware
	virtual PVSSboolean toPeriph(PVSSchar *dataPtr, PVSSuint len, const Variable &var, const PVSSuint subix) const;

	// Conversion from Hardware to PVSS
	virtual VariablePtr toVar(const PVSSchar *data, const PVSSuint dlen, const PVSSuint subix) const;

private:
	CharString name;
	TransformationType type;
	jobject jTransObject;
};

#endif
