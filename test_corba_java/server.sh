
source common.sh

jars=(server.jar common.jar)

main="server.Server"

class="${pkg}.${main}"

jar_list=(${jars[@]/#/${dist}/})
cp=$(colon_join ${jar_list})

echo "Address: ${host}:${port}"


params="-ORBInitialHost ${host} -ORBInitialPort ${port}"
svhost="-Dcom.sun.CORBA.ORBServerHost=${host}"
cmd="java -cp ${cp} ${svhost} ${class} ${params}"
${cmd}


