This Data Logger is a write only data logger for the SCADA System WinCC OA to write value changes and alerts to different kind of databases. In this example MongoDB is used to store the data, other database backends like Elasticsearch, InfluxDB, Kafka or Cassandra are also available. The Logger consists of a Frontend and a Backend Manager. The Frontend Manager will collect the values from WinCC OA by simple dpQueryConnects (which can be configured in the frontend.config file, also multiple connects can be definied). It will pack single value changes to blocks and transfer that blocks by ZeroMQ to Backend Managers. The Backend Manager(s) will get the blocks from ZeroMQ from the Frontend and will write the values to the database. Multiple Backend Managers can be connected to one single Frontend Manager. The Backend Manager will buffer values in memory and/or on disk if the database is partly too slow or unavailable.

                WinCC OA ==> Frontend Manager ==> Backend Manager ==> Database

Offering values by ZeroMQ can be replaced by writing values directly to a database. ZeroMQ is just one possible Plugin as Data Sink for the Frontend Manager. Other plugin's, which are also used by the Backend Manager, can be used in the Frontend Manager to write data directly to a database. The advantage of having Frontend and Backend managers is that many Backends can be connected to one Frontend manager, WinCC OA will only be affected by one manager instead of multiple managers.

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
   cd C:\Tools\oa4j\Project\Logger
   copy make.sh make.bat
   make.bat
   cp –r bin lib config %PROJ_HOME%

*) Download oa4j binaries http://rocworks.at/oa4j/ or compile it (oa4j/Native)
   Copy files to C:\Siemens\Automation\WinCC_OA\<version>\bin

*) Configs:
   - config/config.level => see section java_10 (Frontend) and java_11 (Backend MongoDB)
   - config/frontend.config
   - config/backend-mongo.config => set the mongodb.server.0.url to your MongoDB instance

*) Start Frontend Manager
   WCCOAjava -num 10 -c at/rocworks/oa4j/logger/logger/Frontend -logger config/frontend.config

*) Create MongoDB Collection
    use pvss
    db.createCollection("events");
    db.events.createIndex({ts:1});
    db.events.createIndex({tag:1,ns:1},{unique: true});
    db.events.createIndex({tag:1,ts:1},{unique: false});  

   Note: ns ... nanoseconds since epoch
         ts ... json/bson date datatype
   ns is needed because ts can not store nano seconds

*) Start Backend Manager
   can be started from the console:
   WCCOAjava -num 11 -c at/rocworks/oa4j/logger/logger/Backend -logger config/backend-mongo.config   

   or the backend can be started as an OA independent program from command line
   CMD> Backend-MongoDB.bat
