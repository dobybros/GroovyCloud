#!/bin/bash
cd `dirname $0`
 BIN_DIR=`pwd`
cd ..
JAR_NAME='container.jar'
#最大堆大小(64)
xmx64='512m'
#初始堆大小(64)
xms64='512m'  
#年轻代大小(64) 
xmn64='256m'
#设置永久代初始值(64)
permsize64='128m'
#每个线程的堆栈大小(64)
xss64='256k'
#初始堆大小(else)
xmselse='256m'
#最大堆大小(else)
xmxelse='256m'
#设置永久代初始值(else)
permsizeelse='128m'
#内存页的大小
largepagesize='128m'
#使用cms作为垃圾回收,使用百分之70后开始收集
CMSInitiatingOccupancyFraction='70'
#2个Survivor区和Eden区的比值
SurvivorRatio='2'
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
 if [ -n "$BITS" ]; then
 #Xms(初始堆大小),Xmx(最大堆大小),Xmn(年轻代大小)，-XX:PermSize(设置永久代初始值)，-Xss(每个线程的堆栈大小),-XX:+DisableExplicitGC(关闭System.gc()),-XX:+UseConcMarkSweepGC(使用CMS内存收集),XX:+CMSParallelRemarkEnabled(降低标记停顿),XX+UseCMSCompactAtFullCollection(在full gc时，对老年代压缩),XX:LargePageSizeInBytes(内存页的大小),-XX:+UseFastAccessorMethods(原始类型的快速优化),
 #-XX:+UseCMSInitiatingOccupancyOnly(使用手动定义初始化定义开始CMS收集),-XX:CMSInitiatingOccupancyFraction=70(使用cms作为垃圾回收,使用百分之70后开始收集)
      JAVA_MEM_OPTS=" -server -Xmx$xmx64 -Xms$xms64 -Xmn$xmn64 -XX:PermSize=$permsize64 -Xss$xss64 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=$largepagesize -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=$CMSInitiatingOccupancyFraction -XX:+CMSClassUnloadingEnabled"
 else
     JAVA_MEM_OPTS=" -server -Xms$xmselse -Xmx$xmxelse -XX:PermSize=$permsizeelse -XX:SurvivorRatio=$SurvivorRatio -XX:+UseParallelGC "
 fi
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
