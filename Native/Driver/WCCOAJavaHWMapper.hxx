#ifndef WCCOAJavaHWMAPPER_H_
#define WCCOAJavaHWMAPPER_H_

#include <HWMapper.hxx>

class WCCOAJavaHWMapper : public HWMapper
{
  public:
    virtual PVSSboolean addDpPa(DpIdentifier &dpId, PeriphAddr *confPtr);
    virtual PVSSboolean clrDpPa(DpIdentifier &dpId, PeriphAddr *confPtr);
};

#endif
