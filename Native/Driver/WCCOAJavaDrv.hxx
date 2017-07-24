#ifndef WCCOAJavaDRV_H_
#define WCCOAJavaDRV_H_

#include <DrvManager.hxx>
#include <WCCOAJavaDrvIntDp.hxx>
#include <WCCOAJavaTrans.hxx>
#include <jni.h>

class WCCOAJavaDrv : public DrvManager
{
public:
	static const char *ManagerName;

	static WCCOAJavaDrv* thisManager;

	// Java Functions
	void javaInitialize(JNIEnv *env, jobject obj);

	// Driver Functions
    virtual void install_HWMapper();
    virtual void install_HWService();
    virtual void install_AlertService();

    virtual HWObject *getHWObject() const; 

	// Java HWService
	void answer4DpId(int index, Variable* varPtr);
	void hotLink2Internal(int index, Variable* varPtr);
	PVSSboolean initialize(int argc, char *argv[]);
	PVSSboolean start();
	void stop();
	bool workProc(HWObject *hw);
	PVSSboolean writeData(HWObject *objPtr);
	void flushHW();
	void notifyDisableCommands(PVSSboolean dc);
	unsigned int getAttribs2Connect(const PeriphAddr *) const;

	// HWMapper
	void JavaAddDpPa(DpIdentifier &dpid, PeriphAddr *confPtr);
	void JavaClrDpPa(DpIdentifier &dpid, PeriphAddr *confPtr);

	// Transformation
	jobject      JavaTransformationNewObject(WCCOAJavaTrans *trans, const CharString& name, TransformationType type);
	void         JavaTransformationDelObject(jobject jtrans);
	int		     JavaTransformationGetSize(jobject jtrans);
	VariableType JavaTransformationGetVariableType(jobject jtrans);
	PVSSboolean  JavaTransformationToPeriph(jobject jtrans, PVSSchar *buffer, PVSSuint len, const Variable &var, const PVSSuint subix);
	VariablePtr  JavaTransformationToVar(jobject jtrans, const PVSSchar *buffer, const PVSSuint dlen, const PVSSuint subix);

private:
	JNIEnv *g_env;
	jobject g_obj;
	jclass  g_objClass;

	static const bool DEBUG;
	static const char *ManagerClassName;
	static const char *TransformationClassName;
	jclass javaManagerClass;

	WCCOAJavaDrvIntDp drvIntDp;
};

#endif
