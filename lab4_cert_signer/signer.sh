#! /bin/sh

CP=bin
MAIN=rozprochy.lab4.signer.Main
CONF=conf/signer.config

java -cp ${CP} ${MAIN} --Ice.Config=${CONF} "$@"