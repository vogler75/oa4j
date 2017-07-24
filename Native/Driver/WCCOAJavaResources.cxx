// Our Resource class.
// This class will interpret the command line and read the config file

#include "WCCOAJavaResources.hxx"
#include <ErrHdl.hxx>

CharString WCCOAJavaResources::jvmOption = "";
CharString WCCOAJavaResources::jvmUserDir = "";
CharString WCCOAJavaResources::jvmClassPath = "";
CharString WCCOAJavaResources::jvmLibraryPath = "";
CharString WCCOAJavaResources::jvmConfigFile = "";
CharString WCCOAJavaResources::drvDpName = CharString("_JavaDrv");

map<string, const char*> WCCOAJavaResources::m;

//-------------------------------------------------------------------------------
// init is a wrapper around begin, readSection and end

void WCCOAJavaResources::init(int &argc, char *argv[])
{
  // Prepass of commandline arguments, e.g. get the arguments needed to
  // find the config file.
  begin(argc, argv);

  // Read the config file
  while ( readSection() || generalSection() ) ;

  // Postpass of the commandline arguments, e.g. get the arguments that
  // will override entries in the config file
  end(argc, argv);

  // the user wants to know std. commandline options
  if (WCCOAJavaResources::getHelpFlag())
  {
	  WCCOAJavaResources::printHelp();
  }

  // the user wants to know std. debug flags
  if (WCCOAJavaResources::getHelpDbgFlag())
  {
	  WCCOAJavaResources::printHelpDbg();
  }

  // the user wants to know std. report flags
  if (WCCOAJavaResources::getHelpReportFlag())
  {
	  WCCOAJavaResources::printHelpReport();
  }
}

//-------------------------------------------------------------------------------

PVSSboolean WCCOAJavaResources::readSection()
{
  // Are we in our section ?
  if ( !isSection("javadrv") ) return PVSS_FALSE;

  // Read next entry
  getNextEntry();

  // Now read the section until new section or end of file
  while ( (cfgState != CFG_SECT_START) && (cfgState != CFG_EOF) )
  {
    // read from the config file
	if (!keyWord.icmp("jvmOption"))             
		cfgStream >> jvmOption;                  
	else if (!keyWord.icmp("userDir"))            
		cfgStream >> jvmUserDir;                   
	else if (!keyWord.icmp("classPath"))            
		cfgStream >> jvmClassPath;                  
	else if (!keyWord.icmp("libraryPath"))            
		cfgStream >> jvmLibraryPath;                  
	else if (!keyWord.icmp("configFile"))             
		cfgStream >> jvmConfigFile;                   
    else if ( !commonKeyWord() )
    {
		// store configs in a mapping
		string *k = new string(keyWord);
		CharString *v = new CharString(); cfgStream >> *v;
		m.insert(pair<string, const char*>(*k, v->c_str()));
		/*
		ErrHdl::error(ErrClass::PRIO_WARNING,    // Not that bad
					ErrClass::ERR_PARAM,       // Error is with config file, not with driver
					ErrClass::ILLEGAL_KEYWORD, // Illegal keyword in res.
					keyWord);

		// signal Error, so we stop later
		cfgError = PVSS_TRUE;
	    */
    }

    // get next entry
    getNextEntry();
  }

  // So the loop will stop at the end of the file
  return cfgState != CFG_EOF;
}

//-------------------------------------------------------------------------------
// Interface to internal Datapoints
// Get the number of names we need the DpId for

int WCCOAJavaResources::getNumberOfDpNames()
{
  // TODO if you use internal DPs
  return 0;
}

//-------------------------------------------------------------------------------
