#!/bin/bash

name=client
class=ClientMain

if [[ ! $code ]]; then
    echo "Code server is not set, using localhost"
    code=localhost
fi


source defs.sh

${command} $@
