*) Install WinCC OA 

*) Install Java JDK 
   http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

*) Add the path to the jvm.dll of your Java installation to the PATH environment variable 
   e.g. PATH=C:\Program Files\Java\jre1.8.0_<version>\bin\server
	
*) Download GIT
   https://git-scm.com/download/win
   mkdir C:\Tools, cd C:\Tools
   git clone https://github.com/vogler75/oa4j.git
   
*) Download Apache Maven
   http://mirror.klaus-uwe.me/apache/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.zip
   Copy content of zip to e.g. C:\Tools\apache-maven-3.5.0
   
*) Build MQTT Driver
   set PATH=%PATH%;C:\Tools\apache-maven-3.5.0
   set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_131
   set PROJ_HOME=C:\WinCC_OA_Proj\<project>
   cd C:\Tools\oa4j\Project\Drivers\Mqtt
   copy make.sh make.bat
   make.bat
   cp –r bin lib %PROJ_HOME%
   cp -r config panels scripts dplist %PROJ_HOME%   

*) Download oa4j binaries http://rocworks.at/oa4j/ or compile it (oa4j/Native)     
   Copy files to C:\Siemens\Automation\WinCC_OA\<version>\bin

*) Import the JavaDrv.dpl file with the ASCII Manager from the dplist directory 

*) Add the Java Driver Manager to the console with to following parameters and start it

-num 2 -cp bin/winccoa-mqtt-0.1.jar -url ssl://iot.eclipse.org:8883 -cid winccoa -json -clean [true|false]

-url ... mqtt broker endpoint
-cid ... client id of the driver
-json ... write and read integer & float's as a json value {value=1.2323}

+ Create some datapoint and add a peripherial address config "Sample Driver" to it
     - Reference: e.g. "rpi2/temp" => mqtt tag name
     - Driver-Number: 2 => the number of your java driver (-num 2)
     - Transformation: Float => values in mqtt can be stored as a json document "{Value: 5.2132412}"
     - Subindex: 0 => not used
     - Direction: Input or Output
     - Input-Mode: Spontaneous (Polling, Single-Query is not implemented)
     - Low-Level-Compression: Yes or No
     - Poll-Group: not used
     - Active: Yes   

*) If you get a message "MSVCR100.dll is missing", then you need to install
   Microsoft Visual C++ 2010 SP1 Redistributable Package (x64)
   http://www.microsoft.com/en-us/download/details.aspx?id=13523
