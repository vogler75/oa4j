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
#include <version.hxx>
#include <../LibJava/Java.hxx>
#include <WCCOAJavaManager.hxx>
#include <WCCOAJavaResources.hxx>
#include <at_rocworks_oa4j_jni_Manager.h>
#include <signal.h>
#include <cstring>
#include <fstream>

#ifdef WIN32 
#define CLASS_PATH_SEPARATOR ";"
#else 
#define CLASS_PATH_SEPARATOR ":"
#endif

//------------------------------------------------------------------------------------------------

int main(int argc, char *argv[])
{
	JavaVM *jvm;                      // Pointer to the JVM (Java Virtual Machine)
	JNIEnv *env;                      // Pointer to native interface

	WCCOAJavaResources::init(argc, argv);

    std::cout << "Runtime Version " << PVSS_VERSION << std::endl;

	//================== prepare loading of Java VM ============================
	JavaVMInitArgs vm_args;                        // Initialization arguments
	JavaVMOption* options = new JavaVMOption[99];   // JVM invocation options

	int idx = -1;

	int iUserDirSet = 0;
	int iLibPathSet = 0;
	int iClassPathSet = 0;	

    std::cout << "JVM Configuration... " << std::endl;

	// defaults 

	// user.dir
	{
		CharString *s = new CharString("-Duser.dir=" + WCCOAJavaResources::getProjDir());
		iUserDirSet = ++idx;
		options[iUserDirSet].optionString = strdup(s->c_str());
		std::cout << "default: " << options[idx].optionString << std::endl;
	}

	// java.library.path
	{
		CharString *s = new CharString("-Djava.library.path=" + WCCOAJavaResources::getProjBinDir() + CLASS_PATH_SEPARATOR + WCCOAJavaResources::getBinDir());
		iLibPathSet = ++idx;
		options[iLibPathSet].optionString = strdup(s->c_str());
		std::cout << "default: " << options[idx].optionString << std::endl;
	}

	// java.class.path
	{		
		CharString *s = new CharString(CharString("-Djava.class.path=bin") /*+ CLASS_PATH_SEPARATOR + CharString("bin/winccoa-java.jar")*/);
		iClassPathSet = ++idx;
		options[iClassPathSet].optionString = strdup(s->c_str());
		std::cout << "default: " << options[idx].optionString << std::endl;
	}

	// config 

	// jvmOption e.g. -Xmx512m
	if (strlen(WCCOAJavaResources::getJvmOption().c_str()) > 0)
	{
		options[++idx].optionString = strdup(WCCOAJavaResources::getJvmOption().c_str());
		std::cout << "configs: " << options[idx].optionString << "'" << std::endl;
	}

	// user.dir
	if (strlen(WCCOAJavaResources::getJvmUserDir().c_str()) > 0)
	{
		CharString *s = new CharString("-Duser.dir=" + WCCOAJavaResources::getJvmUserDir());
		if (iUserDirSet == 0) iUserDirSet = ++idx;
		options[iUserDirSet].optionString = strdup(s->c_str()); 
		std::cout << "configs: " << options[iUserDirSet].optionString << std::endl;
	}

	// java.library.path
	if (strlen(WCCOAJavaResources::getJvmLibraryPath().c_str()) > 0)
	{
		CharString *s = new CharString("-Djava.library.path=" + WCCOAJavaResources::getJvmLibraryPath());
		if (iLibPathSet == 0) iLibPathSet = ++idx;
		options[iLibPathSet].optionString = strdup(s->c_str());
		std::cout << "configs: " << options[iLibPathSet].optionString << std::endl;
	}

	// java.class.path
	if (strlen(WCCOAJavaResources::getJvmClassPath().c_str()) > 0)
	{
		CharString *s = new CharString("-Djava.class.path=" + WCCOAJavaResources::getJvmClassPath());
		if (iClassPathSet == 0) iClassPathSet = ++idx;
		options[iClassPathSet].optionString = strdup(s->c_str());
		std::cout << "configs: " << options[iClassPathSet].optionString << std::endl;
	}

	// config file
	CharString fileName = Resources::getConfigDir();
    if ((strlen(WCCOAJavaResources::getJvmConfigFile().c_str()) > 0)) {
		fileName += WCCOAJavaResources::getJvmConfigFile();
		std::cout << "config file: " << fileName << std::endl;
		std::ifstream is(fileName.c_str());
		std::string line;
		while (std::getline(is, line) && idx < 99)
		{
			std::cout << "config.java: " << line << std::endl;
			char * cstr = new char[line.length() + 1];
			std::strcpy(cstr, line.c_str());

			if (line.find("-Duser.dir") == 0) 
				options[iUserDirSet].optionString = cstr;
			else if (line.find("-Djava.class.path") == 0) 
				options[iClassPathSet].optionString = cstr;
			else if (line.find("-Djava.library.path") == 0)
				options[iLibPathSet].optionString = cstr;
			else 
				options[++idx].optionString = cstr;
		}
	}

	//=============== Arguments ===========================================
	int userDirIdx = -1;
	int classPathIdx = -1;
    int libPathIdx = -1;
    int debugFlag = 0; // off
	for (int i = 0; i<argc; i++)
	{
		if (strcmp(argv[i], "-userdir") == 0 || strcmp(argv[i], "-ud") == 0) userDirIdx = i + 1;
		if (userDirIdx == i) {
			CharString *s = new CharString("-Duser.dir=" + CharString(argv[i]));
			if (iUserDirSet == 0) iUserDirSet = ++idx;
			options[iUserDirSet].optionString = strdup(s->c_str());
			std::cout << "argument: " << options[iUserDirSet].optionString << std::endl;
		}

		if (strcmp(argv[i], "-classpath") == 0 || strcmp(argv[i], "-cp") == 0) classPathIdx = i + 1;
		if (classPathIdx == i) {
			CharString *s = new CharString(CharString(options[iClassPathSet].optionString) + CLASS_PATH_SEPARATOR + CharString(argv[i]));
			//CharString *s = new CharString("-Djava.class.path=" + CharString(argv[i]));
			if (iClassPathSet == 0) iClassPathSet = ++idx;
			options[iClassPathSet].optionString = strdup(s->c_str());
			std::cout << "argument: " << options[iClassPathSet].optionString << std::endl;
		}

        if (strcmp(argv[i], "-libpath") == 0 || strcmp(argv[i], "-lp") == 0) libPathIdx = i + 1;
		if (libPathIdx == i) {
			CharString *s = new CharString(CharString(options[iLibPathSet].optionString) + CLASS_PATH_SEPARATOR + CharString(argv[i]));
			if (iLibPathSet == 0) iLibPathSet = ++idx;
			options[iLibPathSet].optionString = strdup(s->c_str());
			std::cout << "argument: " << options[iLibPathSet].optionString << std::endl;
		}

        if (strcmp(argv[i], "-debug") == 0) debugFlag = 1;
	}

	//=============== load and initialize Java VM and JNI interface =============
	vm_args.version = JNI_VERSION_1_8;             // minimum Java version
	vm_args.nOptions = idx + 1;                          // number of options
	vm_args.options = options;
	vm_args.ignoreUnrecognized = true;     // invalid options make the JVM init fail 

	jint rc = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	delete options;    // we then no longer need the initialisation options. 
	if (rc != JNI_OK) {
		exit(EXIT_FAILURE);
	}

	//=============== Display JVM version =======================================
	std::cout << "JVM Load Succeeded: Version ";
	jint ver = env->GetVersion();
	std::cout << ((ver >> 16) & 0x0f) << "." << (ver & 0x0f) << std::endl;

	//=============== Arguments ===========================================   
	int i;
	jstring str;
	jobjectArray jargv = env->NewObjectArray(argc + 8, env->FindClass("java/lang/String"), 0);
	int classIdx = -1;
	const char *className = 0;

	// pass arguments through
	for (i = 0; i<argc; i++)
	{
		str = env->NewStringUTF(argv[i]);
		env->SetObjectArrayElement(jargv, i, str);

		if (strcmp(argv[i], "-class") == 0 || strcmp(argv[i], "-c") == 0) classIdx = i + 1;
		if (classIdx == i) className = argv[i];
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
	CharString manNum = Resources::getManNum();
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

    // Increase java array size (jargv) if new elements are added here!


	//=============== Call Main Method ==========================================
	// check if classname was given
	if (className == NULL) {
		std::cout << "class Main will be used (no parameter -class given)" << std::endl;
		className = "Main";
	}

	jclass javaMainClass = env->FindClass(className);
	if (javaMainClass == NULL) {
		std::cout << "class " << className << " not found!" << std::endl;
		return -2;
	}

	jmethodID javaMainMethod = env->GetStaticMethodID(javaMainClass, "main", "([Ljava/lang/String;)V");
	if (javaMainMethod == NULL) {
		std::cout << "main method not found!" << std::endl;
		return -3;
	}

	env->CallStaticVoidMethod(javaMainClass, javaMainMethod, jargv);
	Java::CheckException(env, "Main Method Exception");

	jvm->DestroyJavaVM();

	return 0;
}

