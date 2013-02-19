
source common.sh

jars=(client.jar common.jar)

main="client.Client"

class="${pkg}.${main}"

jar_list=(${jars[@]/#/${dist}/})
cp=$(colon_join ${jar_list})

connection="-ORBInitialHost ${host} -ORBInitialPort ${port}"
cmd="java -cp ${cp} ${class} ${connection}"
${cmd}


