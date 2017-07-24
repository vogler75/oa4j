#include <../LibJava/Java.hxx>
#include <WCCOAJavaDrv.hxx>
#include <WCCOAJavaResources.hxx>

#include <signal.h>
#include <stdlib.h>
#include <cstring>

#ifdef WIN32 
#define CLASS_PATH_SEPARATOR ";"
#else 
#define CLASS_PATH_SEPARATOR ":"
#endif

int main(int argc, char **argv)
{
	JavaVM *jvm;                      // Pointer to the JVM (Java Virtual Machine)
	JNIEnv *env;                      // Pointer to native interface

	// an instance of our own Resources class is needed
	WCCOAJavaResources resources;
	resources.init(argc, argv);

	// handle std. signals
	signal(SIGINT, Manager::sigHdl);
	signal(SIGTERM, Manager::sigHdl);

	//================== prepare loading of Java VM ============================
	JavaVMInitArgs vm_args;                        // Initialization arguments
	JavaVMOption* options = new JavaVMOption[99];   // JVM invocation options

	int idx = -1;

	int iUserDirSet = 0;
	int iLibPathSet = 0;
	int iClassPathSet = 0;

	// defaults 

	// user.dir
	{
		CharString *s = new CharString("-Duser.dir=" + WCCOAJavaResources::getProjDir());
		iUserDirSet = ++idx;
		options[iUserDirSet].optionString = (char*)s->c_str();
		std::cout << "default: " << options[idx].optionString << std::endl;
	}

	// java.library.path
	/*
	{
		CharString *s = new CharString("-Djava.library.path=" + WCCOAJavaResources::getProjBinDir());
		iLibPathSet = ++idx;
		options[iLibPathSet].optionString = (char*)s->c_str();
		std::cout << "default: " << options[idx].optionString << std::endl;
	}
	*/

	// java.class.path
	{
		CharString *s = new CharString(CharString("-Djava.class.path=bin") + CLASS_PATH_SEPARATOR + CharString("bin/winccoa-java.jar"));
		iClassPathSet = ++idx;
		options[iClassPathSet].optionString = (char*)s->c_str();
		std::cout << "default: " << options[idx].optionString << std::endl;
	}

	// config 

	// jvmOption e.g. -Xmx512m
	if (strlen(WCCOAJavaResources::getJvmOption().c_str()) > 0)
	{
		options[++idx].optionString = WCCOAJavaResources::getJvmOption();
		std::cout << "config: " << options[idx].optionString << "'" << std::endl;
	}

	// user.dir
	if (strlen(WCCOAJavaResources::getJvmUserDir().c_str()) > 0)
	{
		CharString *s = new CharString("-Duser.dir=" + WCCOAJavaResources::getJvmUserDir());
		if (iUserDirSet == 0) iUserDirSet = ++idx;
		options[iUserDirSet].optionString = *s; // (char*)s->c_str();
		std::cout << "config: " << options[iUserDirSet].optionString << std::endl;
	}

	// java.library.path
	if (strlen(WCCOAJavaResources::getJvmLibraryPath().c_str()) > 0)
	{
		CharString *s = new CharString("-Djava.library.path=" + WCCOAJavaResources::getJvmLibraryPath());
		if (iLibPathSet == 0) iLibPathSet = ++idx;
		options[iLibPathSet].optionString = *s; // (char*)s->c_str();
		std::cout << "config: " << options[iLibPathSet].optionString << std::endl;
	}

	// java.class.path
	if (strlen(WCCOAJavaResources::getJvmClassPath().c_str()) > 0)
	{
		CharString *s = new CharString("-Djava.class.path=" + WCCOAJavaResources::getJvmClassPath());
		if (iClassPathSet == 0) iClassPathSet = ++idx;
		options[iClassPathSet].optionString = *s; // (char*)s->c_str();
		std::cout << "config: " << options[iClassPathSet].optionString << std::endl;
	}

	// config file
	CharString fileName = Resources::getConfigDir();
	if ((strlen(WCCOAJavaResources::getJvmConfigFile().c_str()) > 0)) {
		fileName += WCCOAJavaResources::getJvmConfigFile();
		std::cout << "config file: " << fileName << std::endl;
		std::ifstream t(fileName);
		std::string line;
		while (std::getline(t, line) && idx < 99)
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
	for (int i = 0; i<argc; i++)
	{
		if (strcmp(argv[i], "-userdir") == 0 || strcmp(argv[i], "-ud") == 0) userDirIdx = i + 1;
		if (userDirIdx == i) {
			CharString *s = new CharString("-Duser.dir=" + CharString(argv[i]));
			if (iUserDirSet == 0) iUserDirSet = ++idx;
			options[iUserDirSet].optionString = *s; // (char*)s->c_str();
			std::cout << "argument: " << options[iUserDirSet].optionString << std::endl;
		}

		if (strcmp(argv[i], "-classpath") == 0 || strcmp(argv[i], "-cp") == 0) classPathIdx = i + 1;
		if (classPathIdx == i) {
			CharString *s = new CharString(CharString(options[iClassPathSet].optionString) + CLASS_PATH_SEPARATOR + CharString(argv[i]));
			//CharString *s = new CharString("-Djava.class.path=" + CharString(argv[i]));
			if (iClassPathSet == 0) iClassPathSet = ++idx;
			options[iClassPathSet].optionString = *s; // (char*)s->c_str();
			std::cout << "argument: " << options[iClassPathSet].optionString << std::endl;
		}
	}

	//=============== load and initialize Java VM and JNI interface =============
	vm_args.version = JNI_VERSION_1_8;             // minimum Java version
	vm_args.nOptions = idx + 1;                          // number of options
	vm_args.options = options;
	vm_args.ignoreUnrecognized = false;     // invalid options make the JVM init fail 

	jint rc = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	delete options;    // we then no longer need the initialisation options. 
	if (rc != JNI_OK) {
		exit(EXIT_FAILURE);
	}
	//=============== Display JVM version =======================================
	std::cout << "JVM load succeeded: Version ";
	jint ver = env->GetVersion();
	std::cout << ((ver >> 16) & 0x0f) << "." << (ver & 0x0f) << std::endl;

	//=============== Arguments ===========================================
	int i = 0;
	jstring str;
	jobjectArray jargv = env->NewObjectArray(argc + 7, env->FindClass("java/lang/String"), 0);

	str = env->NewStringUTF("-exe");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	int classIdx = -1;
	const char *className = 0;
	// pass arguments through
	for (int j = 0; j<argc; j++)
	{
		str = env->NewStringUTF(argv[j]);
		env->SetObjectArrayElement(jargv, i, str);

		if (strcmp(argv[j], "-class") == 0 || strcmp(argv[j], "-c") == 0) classIdx = i + 1;
		if (classIdx == i) className = argv[j];

		i++;
	}

	// add project name to argument list
	str = env->NewStringUTF("-proj");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	CharString projName = Resources::getProjectName();
	str = env->NewStringUTF(projName);
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	// add project dir to argument list
	str = env->NewStringUTF("-path");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	CharString projDir = Resources::getProjDir();
	str = env->NewStringUTF(projDir);
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	// add manager num to argument list
	str = env->NewStringUTF("-num");
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	CharString manNum = Resources::getManNum();
	str = env->NewStringUTF(manNum);
	env->SetObjectArrayElement(jargv, i, str);
	i++;

	// check if classname was given
	if (className == 0) {
		std::cout << "class Main will be used (no parameter -class given)" << std::endl;
		className = "Main";
	}

	jclass javaMainClass = env->FindClass(className);
	if (javaMainClass == 0) {
		std::cout << "class " << className << " not found!" << std::endl;
		return -2;
	}

	jmethodID javaMainMethod = env->GetStaticMethodID(javaMainClass, "main", "([Ljava/lang/String;)V");
	if (javaMainMethod == 0) {
		std::cout << "java main method not found!" << std::endl;
		return -3;
	}
	
	std::cout << "Java Driver..." << std::endl;
	env->CallStaticVoidMethod(javaMainClass, javaMainMethod, jargv);
	Java::CheckException(env, "Main Method Exception");

	jvm->DestroyJavaVM();

	return 0;
}
