#!/bin/bash

CP=bin:${ICE_HOME}/lib/Ice.jar
MAIN=rozprochy.lab4.server.Server
CONF=conf/server.config

java -cp ${CP} ${MAIN} --Ice.Config=${CONF}