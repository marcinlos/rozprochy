if [[ $# > 1 ]]; then
    host=$1
    port=$2
else
    echo "usage: runorbd host port" 1>&2
    exit -1
fi

orbd -ORBInitialPort ${port} -J-Dcom.sun.CORBA.ORBServerHost=${host}
