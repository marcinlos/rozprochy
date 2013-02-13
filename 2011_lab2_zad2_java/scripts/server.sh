#!/bin/bash

name=server
class=ServerMain


# Address placed in RMI stubs - turns out to be somewhat random
# unless enforced like this. 
if [[ ! $host ]]; then
    echo "Host name not set, using localhost"
    host=localhost
fi

# Ensure the code server (place where class definitions are accessible)
# is set 
if [[ ! $code ]]; then
    echo "Code server is not set, using host"
    code="${host}"
fi

p_hostname="-Djava.rmi.server.hostname=${host}"

options="${p_hostname}"

source defs.sh

echo
echo "Host: ${host}"

${command} $@
