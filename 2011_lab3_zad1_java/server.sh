
source common.sh

device_jars=( dist/device/** )

jars=( server.jar common.jar )

main="server.ServerMain"

class="${pkg}.${main}"

jar_list=(${jars[@]/#/${dist}/} ${device_jars[@]} )
cp=$(colon_join ${jar_list})

echo "cp = ${cp}"

echo "Address: ${host}:${port}"


params="-ORBInitRef NameService=corbaname::${host}:${port}"
svhost="-Dcom.sun.CORBA.ORBServerHost=${host}"
cmd="java -cp ${cp} ${svhost} ${class} ${params}"
${cmd}


