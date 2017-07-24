*) Install WinCC OA 

*) Install Java JDK 
   http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

*) Add the path to the jvm.dll of your Java installation to the PATH environment variable 
   e.g. PATH=C:\Program Files\Java\jre1.8.0_<version>\bin\server
	
*) Download GIT
   https://git-scm.com/download
   mkdir C:\Tools, cd C:\Tools
   git clone https://github.com/vogler75/oa4j.git
   
*) Download Apache Maven
   https://maven.apache.org/download.cgi
   Copy content of zip to e.g. C:\Tools\apache-maven-3.5.0
   
*) Build MQTT Driver
   set PATH=%PATH%;C:\Tools\apache-maven-3.5.0\bin
   set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_131
   set PROJ_HOME=C:\WinCC_OA_Proj\<project>
   cd C:\Tools\oa4j\Project\Java
   copy make.sh make.bat
   make.bat
   cp –r bin lib %PROJ_HOME%
   cp -r config dplist msg panels %PROJ_HOME%   

*) Download oa4j binaries http://rocworks.at/oa4j/ or compile it (oa4j/Native)   
   Copy files to C:\Siemens\Automation\WinCC_OA\<version>\bin

5) Add a new manager "WCCOAjava" in the console with the parameter "-class ApiTestDpConnect"
   or start the java program as a normal java program, see bin/Example.bat
   (there needs to be a directory "log" below your working directory)
   The program is connected to "ExampleDP_Trend1." and prints the value to the log.

*) If you get a message "MSVCR100.dll is missing", then you need to install
   Microsoft Visual C++ 2010 SP1 Redistributable Package (x64)
   http://www.microsoft.com/en-us/download/details.aspx?id=13523

*) Add the following lines to the WinCC OA message catalouge "managers.cat"
   WCCOAjava,Java Manager
   WCCOAjavadrv,Java Driver 
