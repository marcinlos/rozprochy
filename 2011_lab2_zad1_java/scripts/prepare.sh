if (( $# < 1 )); then
    echo "Usage: ./prepare.sh dest_dir <codebase_prefix>"
    exit -1
fi

dest=$(readlink -f $1)

if (( $# >= 2 )); then
    prefix=$2
else
    prefix="${HOME}/public_html/classes"
fi

echo "Copying ordinary classes to ${dest}"

for jar in $(ls ../dist | grep -v _rmi); do
    echo "  copying ${jar} to ${dest}/${jar}"
    cp "../dist/${jar}" "${dest}/${jar}"
done

echo "Copying exported classes to codebase at ${prefix}"
for rmi in $(ls ../dist | grep _rmi); do
    echo "  copying ${rmi} to ${prefix}/${rmi}"
    cp "../dist/${rmi}" "${prefix}/${rmi}"
done

echo "Transforming run scripts..."
escaped=$(echo "${dest}" | sed 's/\//\\\//g')

for prog in *.policy; do
    echo "  ${prog}"
    cp "${prog}" "${dest}/${prog}"
    sed "s/@PATH@/${escaped}/" "${prog}" > "${dest}/${prog}"
done

for s in "defs.sh" "client.sh" "server.sh"; do
    cp "${s}" "${dest}/${s}"
done

echo "Done"


