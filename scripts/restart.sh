#!/usr/bin/env bash

cd ~/ufag

UFAG_PID_FILE=ufag.pid
UFAG_JAR=$(ls dist | sort | tail -n1)
UFAG_OLD_PID=$(cat ${UFAG_PID_FILE})

# REMOVE old dists
# storing only 5 last ones (to be able to rollback to one manually).

cd ./dist
rm $(ls | head -n -5)
cd ..

# APPLY sql migrations
java -cp dist/${UFAG_JAR} -Dconfig.file=${HOME}/ufag/application.conf LiquibaseRunner

# SEND kill signal to current bot
kill ${UFAG_OLD_PID}

# WAIT for process to exit
tail --pid=${UFAG_OLD_PID} -f /dev/null

# START new bot
java -jar -Dconfig.file=${HOME}/ufag/application.conf dist/${UFAG_JAR} &

# SAVE pid of current launching
echo -n $! > ${UFAG_PID_FILE}
