
function colon_join() {
    jar_list=$1
    cp=$(printf ":%s" "${jar_list[@]}")
    echo "${cp:1}"
}

dist="dist"

if [[ $# > 0 ]]; then
    dist=$1
fi

if [[ $# > 2 ]]; then
    host=$2
    port=$3
else
    host="localhost"
    port="6666"
fi

pkg="rozprochy.corba.test"
