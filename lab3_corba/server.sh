
source common.sh

jars=( server.jar common.jar )
jar_list=(${jars[@]/#/${dist}/})
cp=$(colon_join ${jar_list})

main="server.ServerMain"
class="${pkg}.${main}"

params="-ORBInitRef NameService=${address}"
vmparams="-Dfactory.name=${name}"
svhost="-Dcom.sun.CORBA.ORBServerHost=${host}"
cmd="java -cp ${cp} ${vmparams} ${svhost} ${class} ${params}"
${cmd}


