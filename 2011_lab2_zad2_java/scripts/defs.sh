
# name of jar containing classes needed by both programs
common=common

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

nonweb=.
web="${HOME}/public_html/classes"
http="http://${code}/~los/classes"

# classpath
cp="${nonweb}/${name}.jar:${nonweb}/${common}.jar:${web}/${name}_rmi.jar"

codebase="${http}/${name}_rmi.jar"
p_codebase="-Djava.rmi.server.codebase=${codebase}"
p_policy="-Djava.security.policy=${name}.policy"
p_hostname="-Djava.rmi.server.hostname=${host}"

pkg=rozprochy.rok2011.lab2.zad2

fullclass="${pkg}.${name}.${class}"

# transforms colon-separated classpath into bash array
cp_array=${cp//:/ }


echo "classpath:"
for item in ${cp_array[@]}; do
    echo " - ${item}"
done

echo
echo "codebase: ${codebase}"
echo
echo "class: ${fullclass}"
echo
echo "Host: ${host}"

sys_props="${p_codebase} ${p_policy} ${p_hostname}"

command="java -cp ${cp} ${sys_props} ${options} ${fullclass}"

