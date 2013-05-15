#!/bin/sh

CP=bin:lib/jgroups-3.0.10.Final.jar:lib/protobuf-java-2.4.1.jar
OPT=-Djava.net.preferIPv4Stack=true 

java -cp ${CP} ${OPT}  rozprochy.lab5.Application "$@"

