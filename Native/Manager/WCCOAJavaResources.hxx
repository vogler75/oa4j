// The Resource file for the WCCOAJava manager
#ifndef WCCOAJavaRESOURCES_H
#define WCCOAJavaRESOURCES_H

#include <Resources.hxx>
#include <map>
#include <string>

using namespace std;

class WCCOAJavaResources : public Resources
{
  public:
    // These functions initializes the manager
    static void init(int &argc, char *argv[]);

    // Read the config section
    static PVSSboolean readSection();


  public:
	static const CharString & getJvmOption() { return jvmOption; }
	static const CharString & getJvmUserDir() { return  jvmUserDir; }
	static const CharString & getJvmClassPath() { return  jvmClassPath; }
	static const CharString & getJvmLibraryPath() { return jvmLibraryPath; }
	static const CharString & getJvmConfigFile() { return jvmConfigFile; }
	static const char* getConfigValue(const char *key) { return m.count(key) > 0 ? m.at(key) : ""; }

  private:
	static CharString jvmOption;
	static CharString jvmUserDir;
	static CharString jvmClassPath;
	static CharString jvmLibraryPath;
	static CharString jvmConfigFile;

	static map<string, const char*> m;

};

#endif
