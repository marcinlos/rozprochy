#!/bin/bash

CP=bin:${ICE_HOME}/lib/Ice.jar
MAIN=rozprochy.lab4.bank.client.Client
CONF=conf/bank_client.config

java -cp ${CP} ${MAIN} --Ice.Config=${CONF}
