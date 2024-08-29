#include <Resources.hxx>
#include <stdio.h>

// as significant part of "-version" - output is generated in a shared lib 
// (since 2.10 on NT and since 2.11 also on Linux) und is therefore not 
// significant for the executable:
//#include <version.hxx>

void SET_VERSINFO(char *vi) {
  sprintf(vi, "%s %s platform %s linked at %s","3.19", "", "Windows", __DATE__ " " __TIME__);
}


#ifndef ADDVERINFO
 
  class AddVersionInfo
  {
    public:
      AddVersionInfo();
  };
 
  AddVersionInfo::AddVersionInfo()
  {
    char vi[256];
    
    SET_VERSINFO(vi); 
    Resources::setAddVersionInfo(vi);
  }
 
  static AddVersionInfo linkedAt;
 
 
#else
 
  void setAddVersionInfo()
  {
    char vi[256];
    
    SET_VERSINFO(vi); 
    Resources::setAddVersionInfo(vi);
  }
#endif

