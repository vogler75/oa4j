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
#include <../LibJava/Java.hxx>
#include <WCCOAJavaManager.hxx>
#include <WCCOAJavaResources.hxx>
#include <at_rocworks_oa4j_jni_Manager.h>
#include <signal.h>
#include <cstring>
#include <fstream>
#include <sstream>
#include <vector>
#include <glob.h>
#include <sys/stat.h>

#ifdef WIN32
#define CLASS_PATH_SEPARATOR ";"
#else
#define CLASS_PATH_SEPARATOR ":"
#endif

//------------------------------------------------------------------------------------------------
// Split space-separated JVM options into individual options
// This allows options like "-Xmx256m -Xms128m" to be properly parsed
std::vector<std::string> splitJvmOptions(const std::string& optionsString) {
	std::vector<std::string> options;
	std::istringstream iss(optionsString);
	std::string option;
	while (iss >> option) {
		options.push_back(option);
	}
	return options;
}

//------------------------------------------------------------------------------------------------
// Strip leading and trailing quotes from a string
std::string stripQuotes(const std::string& str) {
	std::string result = str;
	// Remove leading quote
	if (!result.empty() && (result[0] == '"' || result[0] == '\'')) {
		result = result.substr(1);
	}
	// Remove trailing quote
	if (!result.empty() && (result.back() == '"' || result.back() == '\'')) {
		result.pop_back();
	}
	return result;
}

//------------------------------------------------------------------------------------------------
// Remove any trailing whitespace/newlines from a string
std::string trimTrailing(const std::string& str) {
	std::string result = str;
	while (!result.empty() && (result.back() == '\n' || result.back() == '\r' || result.back() == ' ' || result.back() == '\t')) {
		result.pop_back();
	}
	return result;
}

//------------------------------------------------------------------------------------------------
// Check if a file is a valid classpath entry (must be a regular file)
bool isValidClasspathFile(const std::string& path) {
	struct stat st;
	if (stat(path.c_str(), &st) != 0) {
		return false; // File doesn't exist
	}
	// Only accept regular files (not directories, symlinks, etc.)
	return S_ISREG(st.st_mode);
}

//------------------------------------------------------------------------------------------------
// Expand wildcard patterns in a single path entry
// Handles patterns like "lib/*.jar" and expands them to individual jar files
// Returns a string with expanded paths separated by the platform-specific separator
// Also returns the count of expanded files via the count parameter
std::string expandGlobPattern(const std::string& pattern, int& count) {
	glob_t glob_result;
	std::string result;
	count = 0;

	// Strip quotes if present
	std::string cleanPattern = stripQuotes(pattern);

	// Only attempt glob expansion if the pattern contains wildcard characters
	if (cleanPattern.find('*') == std::string::npos &&
		cleanPattern.find('?') == std::string::npos &&
		cleanPattern.find('[') == std::string::npos) {
		// No wildcards, return the pattern as-is
		result = cleanPattern;
		count = 1;
		return result;
	}

	// Try to expand the pattern using glob()
	// Use GLOB_MARK to append '/' to directory entries so we can filter them out
	int ret = glob(cleanPattern.c_str(), GLOB_NOSORT | GLOB_MARK, NULL, &glob_result);

	if (ret == 0 && glob_result.gl_pathc > 0) {
		// Pattern matched, filter and add valid classpath files
		int validCount = 0;
		for (size_t i = 0; i < glob_result.gl_pathc; i++) {
			std::string path = glob_result.gl_pathv[i];

			// Skip directories (marked with trailing slash by GLOB_MARK)
			if (!path.empty() && path.back() == '/') {
				continue;
			}

			// Only add regular files to classpath (skip symlinks, etc.)
			if (isValidClasspathFile(path)) {
				if (validCount > 0) {
					result += CLASS_PATH_SEPARATOR;
				}
				result += path;
				validCount++;
			}
		}
		count = validCount;
		globfree(&glob_result);
	} else {
		// Pattern didn't match, return the original pattern as-is
		result = cleanPattern;
		count = 1;
		if (glob_result.gl_pathc > 0) {
			globfree(&glob_result);
		}
	}

	return result;
}

//------------------------------------------------------------------------------------------------
// Expand wildcard patterns in a classpath string
// Handles multiple entries separated by CLASS_PATH_SEPARATOR
// Each entry can be expanded individually
std::string expandClassPathEntries(const std::string& classPath, int& totalCount) {
	std::string result;
	std::string separator(CLASS_PATH_SEPARATOR);
	size_t start = 0;
	size_t pos = 0;
	totalCount = 0;

	// Process each path entry separated by CLASS_PATH_SEPARATOR
	while ((pos = classPath.find(separator, start)) != std::string::npos) {
		std::string entry = classPath.substr(start, pos - start);
		entry = trimTrailing(entry);  // Trim trailing whitespace
		int count = 0;
		std::string expanded = expandGlobPattern(entry, count);
		totalCount += (count > 0) ? count : 1; // Count as 1 if no glob expansion

		if (!result.empty()) {
			result += separator;
		}
		result += expanded;

		start = pos + separator.length();
	}

	// Process the last entry
	std::string lastEntry = classPath.substr(start);
	if (!lastEntry.empty()) {
		// Trim the last entry to remove any trailing whitespace
		lastEntry = trimTrailing(lastEntry);
		int count = 0;
		std::string expanded = expandGlobPattern(lastEntry, count);
		totalCount += (count > 0) ? count : 1; // Count as 1 if no glob expansion
		if (!result.empty()) {
			result += separator;
		}
		result += expanded;
	}

	return result;
}

//------------------------------------------------------------------------------------------------

int main(int argc, char *argv[])
{
	JavaVM *jvm;                      // Pointer to the JVM (Java Virtual Machine)
	JNIEnv *env;                      // Pointer to native interface

	// Find "--" separator: arguments before go to WinCC OA, arguments after go to Java
	// Must be done BEFORE WCCOAJavaResources::init() which modifies argv
	int separatorIdx = -1;
	for (int i = 1; i < argc; i++) {
		if (strcmp(argv[i], "--") == 0) {
			separatorIdx = i;
			break;
		}
	}

	// Save Java arguments before WCCOAJavaResources::init() modifies argv
	int javaArgStart = (separatorIdx > 0) ? separatorIdx + 1 : argc;
	std::vector<std::string> javaArgs;
	for (int i = javaArgStart; i < argc; i++) {
		javaArgs.push_back(argv[i]);
	}

	// Also save -class argument from WinCC OA args (before "--")
	int oaArgc = (separatorIdx > 0) ? separatorIdx : argc;
	const char* className = nullptr;
	for (int i = 1; i < oaArgc; i++) {
		if ((strcmp(argv[i], "-class") == 0 || strcmp(argv[i], "-c") == 0) && i + 1 < oaArgc) {
			className = strdup(argv[i + 1]);
			break;
		}
	}

	WCCOAJavaResources::init(oaArgc, argv);

	ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Runtime Version ") + PVSS_VERSION).c_str());

	//================== prepare loading of Java VM ============================
	JavaVMInitArgs vm_args;                        // Initialization arguments
	JavaVMOption* options = new JavaVMOption[99];   // JVM invocation options

	int idx = -1;

	int iUserDirSet = 0;
	int iLibPathSet = 0;
	int iClassPathSet = 0;	

    ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, "JVM Configuration...");

	// defaults 

	// user.dir
	{
		CharString *s = new CharString("-Duser.dir=" + WCCOAJavaResources::getProjDir());
		iUserDirSet = ++idx;
		options[iUserDirSet].optionString = strdup(s->c_str());
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Default: ") + options[idx].optionString).c_str());
	}

	// java.library.path
	{
		CharString *s = new CharString("-Djava.library.path=" + WCCOAJavaResources::getProjBinDir() + CLASS_PATH_SEPARATOR + WCCOAJavaResources::getBinDir());
		iLibPathSet = ++idx;
		options[iLibPathSet].optionString = strdup(s->c_str());
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Default: ") + options[idx].optionString).c_str());
	}

	// java.class.path
	{		
		CharString *s = new CharString(CharString("-Djava.class.path=bin") /*+ CLASS_PATH_SEPARATOR + CharString("bin/winccoa-java.jar")*/);
		iClassPathSet = ++idx;
		options[iClassPathSet].optionString = strdup(s->c_str());
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Default: ") + options[idx].optionString).c_str());
	}

	// config

	// jvmOption e.g. -Xmx512m or multiple options like "-Xmx512m -Xms256m"
	if (strlen(WCCOAJavaResources::getJvmOption().c_str()) > 0)
	{
		std::vector<std::string> jvmOpts = splitJvmOptions(WCCOAJavaResources::getJvmOption().c_str());
		for (const auto& opt : jvmOpts) {
			if (idx < 98) {  // Ensure we don't exceed the options array size
				options[++idx].optionString = strdup(opt.c_str());
				ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Configs: ") + options[idx].optionString).c_str());
			}
		}
	}

	// user.dir
	if (strlen(WCCOAJavaResources::getJvmUserDir().c_str()) > 0)
	{
		CharString *s = new CharString("-Duser.dir=" + WCCOAJavaResources::getJvmUserDir());
		if (iUserDirSet == 0) iUserDirSet = ++idx;
		options[iUserDirSet].optionString = strdup(s->c_str()); 
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Configs: ") + options[iUserDirSet].optionString).c_str());
	}

	// java.library.path
	if (strlen(WCCOAJavaResources::getJvmLibraryPath().c_str()) > 0)
	{
		CharString *s = new CharString("-Djava.library.path=" + WCCOAJavaResources::getJvmLibraryPath());
		if (iLibPathSet == 0) iLibPathSet = ++idx;
		options[iLibPathSet].optionString = strdup(s->c_str());
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Configs: ") + options[iLibPathSet].optionString).c_str());
	}

	// java.class.path
	if (strlen(WCCOAJavaResources::getJvmClassPath().c_str()) > 0)
	{
		int fileCount = 0;
		std::string expandedPath = expandClassPathEntries(WCCOAJavaResources::getJvmClassPath().c_str(), fileCount);
		CharString *s = new CharString("-Djava.class.path=" + expandedPath);
		if (iClassPathSet == 0) iClassPathSet = ++idx;
		options[iClassPathSet].optionString = strdup(s->c_str());
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Configs: classpath has ") + CharString(fileCount) + CharString(" entries")).c_str());
	}

	// config file
	CharString fileName = Resources::getConfigDir();
    if ((strlen(WCCOAJavaResources::getJvmConfigFile().c_str()) > 0)) {
		fileName += WCCOAJavaResources::getJvmConfigFile();
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Config file: ") + fileName).c_str());
		std::ifstream is(fileName.c_str());
		std::string line;
		while (std::getline(is, line) && idx < 99)
		{
			// Trim trailing whitespace and newlines from config file line
			line = trimTrailing(line);

			ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString(fileName.c_str()) + line.c_str()).c_str());
			char * cstr = new char[line.length() + 1];
			std::strcpy(cstr, line.c_str());

			if (line.find("-Duser.dir") == 0)
				options[iUserDirSet].optionString = cstr;
			else if (line.find("-Djava.class.path") == 0) {
				// Expand glob patterns in config file classpath
				std::string pathPart = line.substr(18); // Skip "-Djava.class.path="
				pathPart = trimTrailing(pathPart);  // Remove any trailing whitespace
				int fileCount = 0;
				std::string expandedPath = expandClassPathEntries(pathPart, fileCount);
				std::string expanded = "-Djava.class.path=" + expandedPath;
				options[iClassPathSet].optionString = strdup(expanded.c_str());
				ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Config file: classpath has ") + CharString(fileCount) + CharString(" entries")).c_str());
			}
			else if (line.find("-Djava.library.path") == 0)
				options[iLibPathSet].optionString = cstr;
			else
				options[++idx].optionString = cstr;
		}
	}

	//=============== Arguments (only process args before "--") ===================
	int userDirIdx = -1;
	int classPathIdx = -1;
    int libPathIdx = -1;
    int debugFlag = 0; // off
	for (int i = 0; i < oaArgc; i++)
	{
		if (strcmp(argv[i], "-userdir") == 0 || strcmp(argv[i], "-ud") == 0) userDirIdx = i + 1;
		if (userDirIdx == i) {
			CharString *s = new CharString("-Duser.dir=" + CharString(argv[i]));
			if (iUserDirSet == 0) iUserDirSet = ++idx;
			options[iUserDirSet].optionString = strdup(s->c_str());
			ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Argument: ") + options[iUserDirSet].optionString).c_str());
		}

		if (strcmp(argv[i], "-classpath") == 0 || strcmp(argv[i], "-cp") == 0) classPathIdx = i + 1;
		if (classPathIdx == i) {
			// Expand glob patterns in command-line classpath
			int fileCount = 0;
			std::string expandedArg = expandClassPathEntries(argv[i], fileCount);
			CharString *s = new CharString(CharString(options[iClassPathSet].optionString) + CLASS_PATH_SEPARATOR + expandedArg);
			//CharString *s = new CharString("-Djava.class.path=" + CharString(argv[i]));
			if (iClassPathSet == 0) iClassPathSet = ++idx;
			options[iClassPathSet].optionString = strdup(s->c_str());
			ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Argument: classpath has ") + CharString(fileCount) + CharString(" entries")).c_str());
		}

        if (strcmp(argv[i], "-libpath") == 0 || strcmp(argv[i], "-lp") == 0) libPathIdx = i + 1;
		if (libPathIdx == i) {
			CharString *s = new CharString(CharString(options[iLibPathSet].optionString) + CLASS_PATH_SEPARATOR + CharString(argv[i]));
			if (iLibPathSet == 0) iLibPathSet = ++idx;
			options[iLibPathSet].optionString = strdup(s->c_str());
			ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Argument: ") + options[iLibPathSet].optionString).c_str());
		}

        if (strcmp(argv[i], "-debug") == 0) debugFlag = 1;
	}

	//=============== Debug: Print final classpath option ========================
	if (iClassPathSet >= 0 && iClassPathSet < idx + 1) {
		std::string cpOption = options[iClassPathSet].optionString;
		size_t eqPos = cpOption.find('=');
		if (eqPos != std::string::npos) {
			std::string pathPart = cpOption.substr(eqPos + 1);
			// Count separators to see how many entries we have
			int sepCount = 0;
			for (char c : pathPart) {
				if (c == ':' || c == ';') sepCount++;
			}
			ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, (CharString("Final classpath option length: ") + CharString((int)cpOption.length()) + CharString(" bytes, ") + CharString(sepCount + 1) + CharString(" paths")).c_str());

			// Write classpath to file for debugging
			std::ofstream debugFile("/tmp/oa4j_classpath_debug.txt");
			if (debugFile.is_open()) {
				debugFile << "Full classpath option:\n" << cpOption << "\n\n";
				debugFile << "Path part only:\n" << pathPart << "\n";
				debugFile.close();
			}
		}
	}

	//=============== load and initialize Java VM and JNI interface =============
	vm_args.version = JNI_VERSION_1_8;             // minimum Java version
	vm_args.nOptions = idx + 1;                          // number of options
	vm_args.options = options;
	vm_args.ignoreUnrecognized = true;     // invalid options make the JVM init fail 

	jint rc = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	delete[] options;    // we then no longer need the initialisation options. 
	if (rc != JNI_OK) {
		exit(EXIT_FAILURE);
	}

	//=============== Display JVM version =======================================
	jint ver = env->GetVersion();
	char verBuf[64];
	sprintf(verBuf, "JVM Load Succeeded: Version %d.%d", ((ver >> 16) & 0x0f), (ver & 0x0f));
	ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, verBuf);

	//=============== Arguments for Java (only args after "--") ====================
	int i;
	jstring str;

	// Use saved javaArgs (captured before WCCOAJavaResources::init modified argv)
	int totalJavaArgs = (int)javaArgs.size() + 8;  // Plus our 8 extra args (-proj, -path, -num, -noinit, -debug)

	jobjectArray jargv = env->NewObjectArray(totalJavaArgs, env->FindClass("java/lang/String"), 0);

	// Pass saved Java arguments (from after "--")
	i = 0;
	for (const auto& arg : javaArgs) {
		str = env->NewStringUTF(arg.c_str());
		env->SetObjectArrayElement(jargv, i, str);
		i++;
	}

	// [1] add project name to argument list
	str = env->NewStringUTF("-proj");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

    // [2]
	CharString projName = Resources::getProjectName();
	str = env->NewStringUTF(projName);
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	// [3] add project dir to argument list
	str = env->NewStringUTF("-path");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

    // [4]
	CharString projDir = Resources::getProjDir();
	str = env->NewStringUTF(projDir);
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	// [5] add manager num to argument list
	str = env->NewStringUTF("-num");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

    // [6]
	CharString manNum = CharString(Resources::getManNum());
	str = env->NewStringUTF(manNum);
	env->SetObjectArrayElement(jargv, i, str);
	i++;

    // [7] add -noinit to argument list
    str = env->NewStringUTF("-noinit");
    env->SetObjectArrayElement(jargv, i, str);
    i++;

    // [8] add -debug to argument list
    str = env->NewStringUTF(debugFlag ? "-debug" : "-nodebug");
    env->SetObjectArrayElement(jargv, i, str);
    i++;

    // Increase totalJavaArgs if new elements are added here!


	//=============== Call Main Method ==========================================
	// check if classname was given (className was saved before init)
	if (className == nullptr) {
		ErrHdl::error(ErrClass::PRIO_INFO, ErrClass::ERR_SYSTEM, 0, "class Main will be used (no parameter -class given)");
		className = "Main";
	}

	jclass javaMainClass = env->FindClass(className);
	if (javaMainClass == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_CONTROL, ErrClass::UNEXPECTEDSTATE, "WCCOAjava", "main", (CharString("class ") + className + " not found!").c_str());
		return -2;
	}

	jmethodID javaMainMethod = env->GetStaticMethodID(javaMainClass, "main", "([Ljava/lang/String;)V");
	if (javaMainMethod == NULL) {
		ErrHdl::error(ErrClass::PRIO_SEVERE, ErrClass::ERR_CONTROL, ErrClass::UNEXPECTEDSTATE, "WCCOAjava", "main", "main method not found!");
		return -3;
	}

	env->CallStaticVoidMethod(javaMainClass, javaMainMethod, jargv);
	Java::CheckException(env, "Main Method Exception");

	jvm->DestroyJavaVM();

	return 0;
}

