set JAR_NAME=container.jar
set xmx64=2000m
set xms64=2000m
set G1NewSizePercent=80
set G1MaxNewSizePercent=90
set MaxGCPauseMillis=20
set rmiThreads=200
set logsFile=logs
set binFile=bin
cd ..
if not exist %logsFile%(mkdir %logsFile%)
cd %binFile%
if "%1" == "debug" (
    set JAVA_DEBUG_OPTS= -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
    goto debug
)
if "%1" == "jmx" (
     set JAVA_JMX_OPTS= -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
     goto jmx
)
set JAVA_MEM_OPTS= -server -Dsun.rmi.transport.tcp.maxConnectionThreads=%rmiThreads% -Xmx%xmx64% -Xms%xms64% -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=%G1NewSizePercent% -XX:G1MaxNewSizePercent=%G1MaxNewSizePercent% -XX:MaxGCPauseMillis=%MaxGCPauseMillis% -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -Xloggc:gcc.log -Djava.awt.headless=true
set CONFIG_FILES= -Xbootclasspath/a:../conf/
echo "Starting the %JAR_NAME%"
java %JAVA_MEM_OPTS% %JAVA_DEBUG_OPTS% %JAVA_JMX_OPTS% %CONFIG_FILES% -jar ../%JAR_NAME%
goto end
:debug
echo "debug"
java %JAVA_MEM_OPTS% %JAVA_DEBUG_OPTS% %CONFIG_FILES% -jar ../%JAR_NAME%
goto end
:jmx
echo "jmx"
java %JAVA_MEM_OPTS% %JAVA_JMX_OPTS% %CONFIG_FILES% -jar ../%JAR_NAME%
goto end
:end
pause
