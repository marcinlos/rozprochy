#!/bin/bash

CP=bin:lib/*:${ICE_HOME}/lib/Ice.jar
MAIN=pl.edu.agh.iosr.bank.server.Server
CONF=conf/server.config

${JAVA_HOME}/bin/java -cp ${CP} ${MAIN} --Ice.Config=${CONF}
