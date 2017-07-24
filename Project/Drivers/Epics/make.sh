mvn package dependency:copy-dependencies dependency:build-classpath -Dmdep.outputFile=classpath.txt -Dmdep.prefix="lib" -Dmaven.test.skip=true
