
function colon_join() {
    jar_list=$1
    cp=$(printf ":%s" "${jar_list[@]}")
    echo "${cp:1}"
}

function read_ior() {
    if [[ ${1} =~ http://.* ]]; then
        curl ${1}
    elif [[ ${1} =~ IOR:.* ]]; then
        echo ${1}
    else
        cat ${1}
    fi
}

dist="dist"
pkg="rozprochy.lab3"

address="corbaname::localhost:6666"
name="factory"

while [[ "$1" != "" ]]; do
    case $1 in
        "-h" | "--help")
            echo "Usage: script [options...]"
            echo "  --nameserv           address of IOR file"
            echo "  --IOR                IOR (string)"
            echo "  --name               name of the factory"
            echo "  --host               address exported in proxies"
            exit
            ;;
        "--ns")
            shift
            address=$1
            ;;
        "--IOR")
            shift
            address=$(read_ior $1)
            ;;
        "--host")
            shift
            host=$1
            ;;
        "--name")
            shift
            name=$1
            ;;
        *)
            echo "Unrecognized option: \`${1}'"
            shift
            ;;
    esac
    shift
done

echo "Address: ${address}"
echo "Name: ${name}"
