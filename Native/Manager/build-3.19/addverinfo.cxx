// Specify version information printed with the -version command line option

// This is in fact cxx, but it should not be
// compiled by default in Resources subproject.

// The file will be copied to the managers directory
// as cxx and will be recompiled each time a manager
// is linked.

#include <Resources.hxx>

extern const char *linkedAt_versionInfo;
extern const char *linkedAt_dateTime;

class AddVersionInfo
{
  public:
    AddVersionInfo()
    {
      CharString vi(linkedAt_versionInfo);
      vi += " linked at ";
      vi += linkedAt_dateTime;

      Resources::setAddVersionInfo(vi);
    }
};

static AddVersionInfo linkedAt;
