
source common.sh

jars=(client.jar common.jar)
jar_list=(${jars[@]/#/${dist}/})
cp=$(colon_join ${jar_list})

main="client.ClientMain"
class="${pkg}.${main}"

params="-ORBInitRef NameService=${address}"
vmparams="-Dfactory.name=${name}"
cmd="java -cp ${cp} ${vmparams} ${svhost} ${class} ${params}"
${cmd}


