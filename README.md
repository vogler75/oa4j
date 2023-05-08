# Content

- [WinCC OA for Java](#wincc-oa-open-architecture-for-java)
- [Deploy Java Manager](#deploy-java-manager)
- [Start Java Manager](#start-java-manager)
- [Config Java Manager](#config-java-manager)
- [Compile Java Manager](#compile-java-manager)

# WinCC OA (Open Architecture) for Java
WinCC Open Architecture for Java is an API to connect WinCC OA to Java.<br>
It is based on the WinCC OA native API+JNI and works on Windows and Linux.<br>
Because it's using the WinCC OA API you need to have a valid WinCC OA API license.<br>
An example WinCC OA project is available in the project directory.<br>
An example for Scala can be found in the Scala directory.<br>
* Java: the java library for WinCC OA (winccoa-java-x.x.jar)
* Native: the native api which is used by the Java library
* Project: Example projects build with oa4j (drivers, ...)
* Scala: An example how oa4j can be used in Scala

## Manager (oa4j/Native/Manager <-> at.rocworks.oa4j.base)<br>
Examples can be found in the Java/src/test directory and in the project/example directory, please read the project/Example/Readme.txt. <br>
The JClient class is an easy to use static class. It should be thread safe and callback functions are processed in a separate thread, so that the main WinCC OA thread/loop will not be blocked by callback functions.<br>
```
// Example how to connect to WinCC OA and set some tags (datapoints)
JManager m = new JManager();
m.init(args).start(); 
ret = JClient.dpSet()
  .add("ExampleDP_Trend1.", Math.random())
  .add("ExampleDP_SumAlert.", "hello world")
  .await();
m.stop();
```

## Driver (oa4j/Native/Driver <-> at.rocworks.oa4j.driver)<br>
There is an API and framework to implement a WinCC OA driver program in Java. A driver program is used to connect WinCC OA to peripherial devices and exchange data. There is a driver class driver.JDriver and a driver.JDriverSimple class, which can be used to implement a driver. An example drivers can be found in Project/Drivers - Mqtt,  Apache Kafka, Epics (Experimental Physics and Industrial Control System), ...) <br>

## CtrlExt (oa4j\Native\CtrlExt <-> at.rocworks.oa4j.base.ExternHdl)<br>
It is possible to call a Java function from WinCC OA control language. 
The control extension JavaCtrlExt must be loaded and a Subclass of ExternHdlFunction must be implemented. This class can be used to execute a function. Executing the Java function in Control is done with the control function "javaCall" and "javaCallAsync". E.g.: javaCall("ApiTestExternHdl", "myFunTest", makeDynAnytype("hello", 1, true), out); A list of variables can be passed as input parameters and a list of values can be passed out by the Java function (it is the return value of the Java function). It is also possible to execute ctrl callback functions from Java. An example can be found in project/example/panels/JavaCtrlExt.pnl and the Java source src/test/java/ApiTestExternHdl.java. For each javaCall a new Java object of the given class is created (where a reference pointer to the WaitCond object in C++ is stored in the case of an async call).<br>
```
    // Java Code (Class ApiTestExternHdl)
    public DynVar execute(String function, DynVar parameter) {
        Debug.out.log(Level.INFO, "execute function={0} parameter={1}", new Object[] { function, parameter.formatValue() });
        return new DynVar(new IntegerVar(0));
    }

    // Control Code
    dyn_anytype out;
    dyn_anytype in = makeDynAnytype("hallo", 1, getCurrentTime());
    int ret = javaCall("ApiTestExternHdl", "myFunTest", in, out);
```

# Deploy Java Manager

Compiled versions can be downloaded from here: http://rocworks.at/oa4j/

```
set VERS=3.18
set PROJ=Test
set OA4J=C:\Tools\oa4j
```
Unzip compiled versions and deploy it to WinCC Open Architecure:
```
copy WCCOAjava.* C:\Siemens\Automation\WinCC_OA\%VERS%\bin
copy winccoa-java-1.0-SNAPSHOT.jar C:\WinCC_OA_Proj\%PROJ%\bin
```

Start a test manger to check if it works:  
```
cd C:\WinCC_OA_Proj\%PROJ%
java -Djava.library.path=C:\Siemens\Automation\WinCC_OA\%VERS%\bin -cp C:\WinCC_OA_Proj\%PROJ%\bin;C:\WinCC_OA_Proj\%PROJ%\bin\winccoa-java-1.0-SNAPSHOT.jar;%OA4J%\Java\target\test-classes ApiTestDpConnect -proj Test
```
Logs are written to a file named "WCCILjava1.0.log" to the "log" directory from where the java program has been started! If there is no "log" directory, the log is written to the current directory.

# Start Java Manager
Add the following lines to the WinCC OA message catalog "managers.cat". You will find this file here C:/Siemens/Automation/WinCC_OA/%VERS%/msg/de_AT.utf8/managers.cat. Change it for all languages.
```
WCCOAjava,Java Manager  
WCCOAjavadrv,Java Driver  
```
Add a java section to your project config file 
```
[java]
classPath = "bin;bin/winccoa-java-1.0-SNAPSHOT.jar;C:/Tools/oa4j/Java/target/test-classes"
```

Add a new manager "WCCOAjava" in the console with the argument "-class ApiTestDpConnect".   
The program is connected to "ExampleDP_Trend1" and "ExampleDP_Trend1" and it writes a sum to ExampleDP_Trend3.".  

Or ou can start the manager from commandline:
```
set VERS=3.18
set PROJ=Test
set OA4J=C:\Tools\oa4j
java -Djava.library.path=C:/Siemens/Automation/WinCC_OA/%VERS%/bin -cp C:/WinCC_OA_Proj/%PROJ%/bin;C:/WinCC_OA_Proj/%PROJ%/bin/winccoa-java-1.0-SNAPSHOT.jar;%OA4J%/Java/target/test-classes ApiTestDpConnect -proj %PROJ%
```

If you get a message "MSVCR100.dll is missing", then you need to install Microsoft Visual C++ 2010 SP1 Redistributable Package (x64) http://www.microsoft.com/en-us/download/details.aspx?id=13523  

# Config Java Manager
```
[java]
# java user dir (-Duser.dir), defaults to project directory
#userDir = "<project-dir>"

# java library path (-Djava.library.path), defaults to the project bin directory, WCCOAjava.dll must be located there. Defaults to WinCC OA bin directory.
#libraryPath = "C:/Siemens/Automation/WinCC_OA/3.18/bin" 

# java class path (-Djava.class.path), defaults to project/bin directory
classPath = "bin;bin/winccoa-java-1.0-SNAPSHOT.jar"

# if you have long class paths you can use a file
#configFile = "config.java"
```

# Compile Java Manager
* Install WinCC OA 

* Install Java JDK  
  http://www.oracle.com/technetwork/java/javase/downloads/dk8-downloads-2133151.html  
  or  
  https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html

* Add the path to the jvm.dll of your Java installation to the PATH environment variable  
  e.g. PATH=C:\Program Files\Java\jre1.8.0_<version>\bin\server
	
* Download GIT  
  https://git-scm.com/download  
  mkdir C:\Tools, cd C:\Tools  
  git clone https://github.com/vogler75/oa4j.git  
   
* Download Apache Maven
  https://maven.apache.org/download.cgi
  Copy content of zip to e.g. C:\Tools\apache-maven-3.5.0
   
* Build Java Library  
  set VERS=3.18  
  set PROJ=Test  
  set PATH=%PATH%;C:\Tools\apache-maven-3.5.0\bin  
  set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_131  
  set PROJ_HOME=C:\WinCC_OA_Proj\%PROJ%  
  cd C:\Tools\oa4j\Project\Java  
  copy make.sh make.bat  
  make.bat  
  copy winccoa-java-1.0-SNAPSHOT.jar %PROJ_HOME%  

* Download oa4j binaries http://rocworks.at/oa4j/.

* Or compile oa4j from the Native/Tools directory.   
  Open vc-start_3.XX.bat to start Visual Studio.  
  <br>
  Open the solution from Native/Manager/build and build it.  
  &rarr; use the solution in dir "build" for WinCC OA Version >= 3.18  
  &rarr; use the solution in dir "build-pre-318" for WinCC OA Version < 3.18  
  <br>
  Then copy bin\WCCOAjava.dll & bin\WCCOAjava.exe files to C:\Siemens\Automation\WinCC_OA\%VERS%\bin  

* Add the following lines to the WinCC OA message catalouge "managers.cat"  
  WCCOAjava,Java Manager  
  WCCOAjavadrv,Java Driver  




