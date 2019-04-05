https://gnupg.org/download/
gpg --gen-key

%HOMEPATH%\.m2\settings.xml:
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
	   <server>
		  <id>ossrh</id>
		  <username>vogler</username>
		  <password></password>
	   </server>
	</servers>
</settings>

mvn deploy -Dmaven.test.skip=true