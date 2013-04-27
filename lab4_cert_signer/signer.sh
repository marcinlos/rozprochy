#! /bin/bash

args=${@}

if (( "${#args}" < 1 )); then
    echo "Usage: ./prepare.sh dest_dir <codebase_prefix>"
    exit -1
fi

basedir=$(dirname $(readlink -f $0))
dest=$(readlink -f $1)

CP=${basedir}/bin:${ICE_HOME}/lib/Ice.jar
MAIN=rozprochy.lab4.signer.Main
CONF=${basedir}/conf/signer.config

java -cp ${CP} ${MAIN} --Ice.Config=${CONF} ${dest}
