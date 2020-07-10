set JAR_NAME=imcontainer
set JAR_VERSION=1.0
set xmx64=2000m
set xms64=2000m
set G1NewSizePercent=80
set G1MaxNewSizePercent=90
set MaxGCPauseMillis=20
set rmiThreads=200
set binFile=bin
cd ..
call mvn.cmd -s src/main/resources/config/mvnsettings.xml clean install -Dmaven.test.skip=true -f bin/pom.xml
call rd /s /q "libs/groovycloud"
call mvn.cmd -s src/main/resources/config/mvnsettings.xml clean install -Dmaven.test.skip=true -f src/main/resources/config/basepom.xml
call xcopy libs/groovycloud/%JAR_NAME%/%JAR_VERSION%/%JAR_NAME%-%JAR_VERSION%.jar" .
cd %binFile%
if "%1" == "debug" (
    set JAVA_DEBUG_OPTS= -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
    goto debug
)
if "%1" == "jmx" (
     set JAVA_JMX_OPTS= -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
     goto jmx
)
set JAVA_MEM_OPTS= -server --add-exports java.base/jdk.internal.ref=ALL-UNNAMED -Dsun.rmi.transport.tcp.maxConnectionThreads=%rmiThreads% -Xmx%xmx64% -Xms%xms64% -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=%G1NewSizePercent% -XX:G1MaxNewSizePercent=%G1MaxNewSizePercent% -XX:MaxGCPauseMillis=%MaxGCPauseMillis% -Djava.awt.headless=true
set CONFIG_FILES= -Xbootclasspath/a:../src/main/resources/

call java %JAVA_MEM_OPTS% %JAVA_DEBUG_OPTS% %JAVA_JMX_OPTS% %CONFIG_FILES% -jar ../%JAR_NAME%-%JAR_VERSION%.jar
goto end
:debug
echo "debug"
call java %JAVA_MEM_OPTS% %JAVA_DEBUG_OPTS% %CONFIG_FILES% -jar ../%JAR_NAME%
goto end
:jmx
echo "jmx"
call java %JAVA_MEM_OPTS% %JAVA_JMX_OPTS% %CONFIG_FILES% -jar ../%JAR_NAME%
goto end
:end
pause
