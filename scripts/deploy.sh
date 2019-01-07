#!/usr/bin/env bash

cd ~/ufag

UFAG_PID_FILE=ufag.pid
UFAG_JAR=$(ls dist | sort | tail -n1)
UFAG_OLD_PID=$(cat ${UFAG_PID_FILE})

# SEND kill signal to current bot
kill ${UFAG_OLD_PID}

# WAIT for process to exit
tail --pid=${UFAG_OLD_PID} -f /dev/null

# START new bot
java -jar -Dconfig.file=${HOME}/ufag/application.conf dist/${UFAG_JAR} &

# SAVE pid of current launching
echo -n $! > ${UFAG_PID_FILE}

# fixme: remove old dists
