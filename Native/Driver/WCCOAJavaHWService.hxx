#ifndef WCCOAJavaHWSERVICE_H_
#define WCCOAJavaHWSERVICE_H_

#include <HWService.hxx>

class WCCOAJavaHWService : public HWService
{
  public:
    virtual PVSSboolean initialize(int argc, char *argv[]);
    virtual PVSSboolean start();
    virtual void stop();
    virtual void workProc();
    virtual PVSSboolean writeData(HWObject *objPtr);
	virtual void flushHW();
	virtual void notifyDisableCommands(PVSSboolean dc);
};

#endif
