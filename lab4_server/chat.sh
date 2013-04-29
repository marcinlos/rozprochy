#!/bin/bash

CP=bin:${ICE_HOME}/lib/Ice.jar
MAIN=rozprochy.lab4.chat.client.Client
CONF=conf/chat_client.config

java -cp ${CP} ${MAIN} --Ice.Config=${CONF}