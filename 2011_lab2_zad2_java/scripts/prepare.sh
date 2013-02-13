if (( $# < 1 )); then
    echo "Usage: ./prepare.sh dest_dir <codebase_prefix>"
    exit -1
fi

basedir=$(dirname $(readlink -f $0))
dest=$(readlink -f $1)


if (( $# >= 2 )); then
    prefix=$2
else
    prefix="${HOME}/public_html/classes"
fi

echo "Copying ordinary classes to ${dest}"

for jar in $(ls ${basedir}/../dist | grep -v _rmi); do
    echo "  copying ${jar} to ${dest}/${jar}"
    cp "${basedir}/../dist/${jar}" "${dest}/${jar}"
done

echo "Copying exported classes to codebase at ${prefix}"
for rmi in $(ls ${basedir}/../dist | grep _rmi); do
    echo "  copying ${rmi} to ${prefix}/${rmi}"
    cp "${basedir}/../dist/${rmi}" "${prefix}/${rmi}"
done

echo "Transforming run scripts..."
escaped=$(echo "${dest}" | sed 's/\//\\\//g')

for prog in ${basedir}/*.policy; do
    file=$(basename ${prog})
    echo "  ${file}"
    cp "${prog}" "${dest}/${file}"
    sed "s/@PATH@/${escaped}/" "${prog}" > "${dest}/${file}"
done

for s in "defs.sh" "client.sh" "server.sh"; do
    cp "${basedir}/${s}" "${dest}/${s}"
done

echo "Done"


