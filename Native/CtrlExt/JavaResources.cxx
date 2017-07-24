// Our Resources administration
#include <JavaResources.hxx>
#include <ErrHdl.hxx>
#include <Config.hxx>

// Our static Variable
CharString JavaResources::jvmOption = "";
CharString JavaResources::jvmUserDir = "";
CharString JavaResources::jvmClassPath = "";
CharString JavaResources::jvmLibraryPath = "";
CharString JavaResources::jvmConfigFile = "";


// Wrapper to read config file
void  JavaResources::init()
{
	// we check for either [httpServer] or [httpServer_<manNum>] section

	int type = Resources::getManType();
	const CharString SECTION = (type==4 ? "javaui" : type==5 ? "javactrl" : "java");
	const CharString SECTION_NUM = SECTION + "_" + CharString(static_cast<int>(Resources::getManNum()));

	// default
	Config config;
	config.open();

	bool sectionFound = false;
	if (config.find(SECTION_NUM) == 0)
		sectionFound = true;
	else
	{
		config.first();  // rewind
		if (config.find(SECTION) == 0)
			sectionFound = true;
	}

	if (sectionFound)
	{
		CharString key, value;

		for (config.next(); (config.read(key, value) == 0) && (key[0] != '['); config.next())
		{
			if (!key.icmp("jvmOption"))
				jvmOption = (value.dup());
			else if (!key.icmp("userDir"))
				jvmUserDir = (value.dup());
			else if (!key.icmp("classPath"))
				jvmClassPath = (value.dup());
			else if (!key.icmp("libraryPath"))
				jvmLibraryPath = (value.dup());
			else if (!key.icmp("configFile"))
				jvmConfigFile = (value.dup());
		}
	}
	config.close();
}


