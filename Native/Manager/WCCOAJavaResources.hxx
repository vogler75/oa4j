/*
OA4J - WinCC Open Architecture for Java
Copyright (C) 2017 Andreas Vogler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
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
