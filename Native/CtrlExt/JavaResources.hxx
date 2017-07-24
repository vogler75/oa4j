// The Resource file for the WCCOAJava manager
#ifndef WCCOAJavaRESOURCES_H
#define WCCOAJavaRESOURCES_H

#include  <Resources.hxx>

class JavaResources : public Resources
{
  public:
    // These functions initializes the manager
    static void init();

    // Read the config section
    static PVSSboolean readSection();

  public:
	  static const CharString & getJvmOption() { return jvmOption; }
	  static const CharString & getJvmUserDir() { return  jvmUserDir; }
	  static const CharString & getJvmClassPath() { return  jvmClassPath; }
	  static const CharString & getJvmLibraryPath() { return jvmLibraryPath; }
	  static const CharString & getJvmConfigFile() { return jvmConfigFile; }

  private:
	  static CharString jvmOption;
	  static CharString jvmUserDir;
	  static CharString jvmClassPath;
	  static CharString jvmLibraryPath;
	  static CharString jvmConfigFile;

};

#endif
