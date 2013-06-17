#!/bin/sh

############################################################
# Batch Script
# usage : laucher.sh linux deploy \ startup \ shutdown
############################################################

export arg1=$1
export arg2=$2

if [ "$arg1" = "" ]; then
	arg1='linux' 
fi
if [ "$arg2" = "" ]; then
	arg2='deploy' 
fi
echo $arg1 $arg2

export LANG=ko_KR.UTF-8
export BASE_DIR=`pwd`
#export JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64
#export JAVA_HOME=/DATA1/jre1.6.0_38
export JAVA_HOME=/DATA1/jdk1.7.0_21
export HOME_DIR=/DATA2/lhf.launcher
export WORK_DIR=$HOME_DIR/src

export GROOVY_HOME=/DATA1/groovy-2.1.3
export ADDED_LIB=$HOME_DIR/lib

export GROOVY_SHELL=groovy
export MAIN_CLASS=lhf/launcher/cmd/LaunchCmd.groovy
export PATH=$PATH:$GROOVY_HOME/bin

#======= working dir =================
cd $WORK_DIR

#======= CLASSPATH =================

export CLASSPATH=.
for f in `find $GROOVY_HOME/lib -type f -name "*.jar"`
do
   CLASSPATH=$CLASSPATH:$f
done

for f in `find $ADDED_LIB -type f -name "*.jar"`
do
   CLASSPATH=$CLASSPATH:$f
done

export CLASSPATH=$CLASSPATH:$HOME_DIR/bin

echo $ADDED_LIB

#======= =================
echo $GROOVY_SHELL $MAIN_CLASS $arg1 $arg2 $3 $4 $5
$GROOVY_SHELL $MAIN_CLASS $arg1 $arg2 $3 $4 $5

cd $BASE_DIR

echo "finished!!"
