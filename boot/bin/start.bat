set JAR_NAME=boot.jar
set xmx64=512m
set xms64=512m
set xmn64=256m
set permsize64=128m
set xss64=256k
set xmselse=256m
set xmxelse=256m
set permsizeelse=128m
set largepagesize=128m
set CMSInitiatingOccupancyFraction=70
set SurvivorRatio=2

if "%1" == "debug" (
    set JAVA_DEBUG_OPTS= -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
    goto debug
)
if "%1" == "jmx" (
     set JAVA_JMX_OPTS= -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
     goto jmx
)
set JAVA_MEM_OPTS= -server -Xmx%xmx64% -Xms%xms64% -Xmn%xmn64% -XX:PermSize=%permsize64% -Xss%xss64% -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=%largepagesize% -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=%CMSInitiatingOccupancyFraction% -XX:+CMSClassUnloadingEnabled
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
