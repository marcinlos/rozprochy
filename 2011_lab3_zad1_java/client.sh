
source common.sh

jars=(client.jar common.jar)

main="client.ClientMain"

class="${pkg}.${main}"

jar_list=(${jars[@]/#/${dist}/})
cp=$(colon_join ${jar_list})

params="-ORBInitRef NameService=corbaname::${host}:${port}"
cmd="jaco -DORBInitRef.InterfaceRepository=http://localhost/~los/ir/dupa.ir -cp ${cp} ${class} ${params}"
${cmd}


