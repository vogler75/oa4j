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
#include  <WCCOAJavaResources.hxx>
#include  <ErrHdl.hxx>

// Our static Variable
CharString  WCCOAJavaResources::jvmOption = "";
CharString  WCCOAJavaResources::jvmUserDir = "";
CharString  WCCOAJavaResources::jvmClassPath = "";
CharString  WCCOAJavaResources::jvmLibraryPath = "";
CharString  WCCOAJavaResources::jvmConfigFile = "";

map<string, const char*> WCCOAJavaResources::m;

// Wrapper to read config file
void  WCCOAJavaResources::init(int &argc, char *argv[])
{      
  //std::cout << "Init Resources..." << std::endl;
  begin(argc, argv);
  while ( readSection() || generalSection() )
    ;
  end(argc, argv);

  // Are we called with -helpdbg or -help ?
  if (WCCOAJavaResources::getHelpDbgFlag())
  {
	  WCCOAJavaResources::printHelpDbg();
  }

  if (WCCOAJavaResources::getHelpFlag())
  {
	  WCCOAJavaResources::printHelp();
  }

  // the user wants to know std. report flags
  if (WCCOAJavaResources::getHelpReportFlag())
  {
	  WCCOAJavaResources::printHelpReport();	  
  }
  //std::cout << "Init Resources...done." << std::endl;
}


// Read the config file.
// Our section is [WCCOAJava] or [WCCOAJava<num>],
PVSSboolean  WCCOAJavaResources::readSection()
{
  // Is it our section ?
  if (!isSection("java"))
    return PVSS_FALSE;

  // Read next entry
  getNextEntry();

  // Loop thru section - V3.17
  /*
  while ( (getCfgState() != CFG_SECT_START) &&  // Not next section
          (getCfgState() != CFG_EOF) )          // End of config file
  {
    if (!getKeyWord().icmp("jvmOption"))             // It matches
		getCfgStream() >> jvmOption;                   // read the value
	else if (!getKeyWord().icmp("userDir"))             // It matches
		getCfgStream() >> jvmUserDir;                   // read the value
	else if (!getKeyWord().icmp("classPath"))             // It matches
		getCfgStream() >> jvmClassPath;                   // read the value
	else if (!getKeyWord().icmp("libraryPath"))             // It matches
		getCfgStream() >> jvmLibraryPath;                   // read the value
	else if (!getKeyWord().icmp("configFile"))             // It matches
		getCfgStream() >> jvmConfigFile;                   // read the value
	else {
		string *k = new string(getKeyWord());
		CharString *v = new CharString(); getCfgStream() >> *v;
		m.insert(pair<string, const char*>(*k, v->c_str()));
	}
	getNextEntry();
  }

  // So the loop will stop at the end of the file
  return getCfgState() != CFG_EOF;

  */

	// Loop thru section V3.16
  while ((cfgState != CFG_SECT_START) &&  // Not next section
	  (cfgState != CFG_EOF))          // End of config file
  {
	  if (!keyWord.icmp("jvmOption"))             // It matches
		  cfgStream >> jvmOption;                   // read the value
	  else if (!keyWord.icmp("userDir"))             // It matches
		  cfgStream >> jvmUserDir;                   // read the value
	  else if (!keyWord.icmp("classPath"))             // It matches
		  cfgStream >> jvmClassPath;                   // read the value
	  else if (!keyWord.icmp("libraryPath"))             // It matches
		  cfgStream >> jvmLibraryPath;                   // read the value
	  else if (!keyWord.icmp("configFile"))             // It matches
		  cfgStream >> jvmConfigFile;                   // read the value
	  else {
		  string *k = new string(keyWord);
		  CharString *v = new CharString(); cfgStream >> *v;
		  m.insert(pair<string, const char*>(*k, v->c_str()));
	  }
	  getNextEntry();
  }

  // So the loop will stop at the end of the file
  return cfgState != CFG_EOF;
}
