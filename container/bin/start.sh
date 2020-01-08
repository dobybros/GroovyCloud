#!/bin/bash
cd `dirname $0`
 BIN_DIR=`pwd`
cd ..
JAR_NAME='container.jar'
#最大堆大小(64)
xmx64='2000m'
#初始堆大小(64)
xms64='2000m'
#初始年轻代大小占用堆的百分比
G1NewSizePercent='80'
#年轻代最大大小占用堆的百分比
G1MaxNewSizePercent='90'
#gc最大暂停时间（单位:毫秒）
MaxGCPauseMillis='20'
#rmi线程数量
rmiThreads='200'
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf
LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
  mkdir $LOGS_DIR
fi
LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Duser.timezone=Asia/Shanghai -Dfile.encoding=utf-8"
JAVA_DEBUG_OPTS=""
if [ "-x$1" = "-x--debug" ]; then
    JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n "
fi
JAVA_JMX_OPTS=""
if [ "$1" = "jmx" ]; then
     JAVA_JMX_OPTS=" -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
 fi
 JAVA_MEM_OPTS=""
 BITS=`java -version 2>&1 | grep -i 64-bit`
 JAVA_MEM_OPTS=" -server --add-exports java.base/jdk.internal.ref=ALL-UNNAMED -Dsun.rmi.transport.tcp.maxConnectionThreads=$rmiThreads -Xmx$xmx64 -Xms$xms64 -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=$G1NewSizePercent -XX:G1MaxNewSizePercent=$G1MaxNewSizePercent -XX:MaxGCPauseMillis=$MaxGCPauseMillis -Djava.awt.headless=true"
CONFIG_FILES=" -Xbootclasspath/a:$CONF_DIR"
 nohup java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS $CONFIG_FILES -jar $DEPLOY_DIR/$JAR_NAME &>/dev/null 2>&1 &

 COUNT=0
 while [ $COUNT -lt 1 ]; do
     echo -e ".\c"
     sleep 1

    COUNT=`ps -ef | grep java | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l`

     if [ $COUNT -gt 0 ]; then
         break
    fi
 done

 echo "OK!"
 PIDS=`ps -ef | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
 echo "PID: $PIDS"
 #echo "STDOUT: $STDOUT_FILE"
 #mvn install -DskipTests
