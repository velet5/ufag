#!/usr/bin/env bash

cd ~/ufag
UFAG_JAR=$(ls dist | sort | tail -n1) # xxx: remove old dists
pkill java # xxx: replace with more appropriate way to kill current instance
java -jar dist/$UFAG_JAR &
