#!/bin/sh
# Runs jboss in debug mode


# Extract the directory and the program name
# takes care of symlinks
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="`dirname "$PRG"`/$link"
  fi
done
DIRNAME=`dirname "$PRG"`
PROGNAME=`basename "$PRG"`

DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
export JAVA_OPTS="${JAVA_OPTS:-} $DEBUG_OPTS"
echo "Invoking $DIRNAME/run.sh with $DEBUG_OPTS"
$DIRNAME/run.sh $@
