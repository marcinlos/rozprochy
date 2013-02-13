
# name of jar containing classes needed by both programs
common=common

nonweb=.
web="${HOME}/public_html/classes"
http="http://${code}/~los/classes"

# classpath
cp="${nonweb}/${name}.jar:${nonweb}/${common}.jar:${web}/${name}_rmi.jar"

codebase="${http}/${name}_rmi.jar"
p_codebase="-Djava.rmi.server.codebase=${codebase}"
p_policy="-Djava.security.policy=${name}.policy"

pkg=rozprochy.rok2011.lab2.zad1

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

command="java -cp ${cp} ${p_codebase} ${p_policy} ${options} ${fullclass}"
