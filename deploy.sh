#!/bin/bash

declare -A remotes
remotes[student]=student.agh.edu.pl
remotes[jagular]=jagular.iisg.agh.edu.pl

cmd_file=".dupaa"

path='~/rozprochy'


# Functions performing various operations on the projects
build_c() { make -C $1; }
clean_c() { make -C $1 clean; }

build_java() { ant -f "$1/build.xml" jar; }
clean_java() { ant -f "$1/build.xml" clean; }

prepare_c() { clean_c $1; }
prepare_java() { build_java $1; }


build_dir() {
    if [[ "$1" =~ "_java" ]]; then
        build_java $1
    elif [[ "$1" =~ "_c" ]]; then
        build_c $1
    fi
}

clean_dir() {
    if [[ "$1" =~ "_java" ]]; then
        clean_java $1
    elif [[ "$1" =~ "_c" ]]; then
        clean_c $1
    fi
}

prepare_dir() {
    if [[ "$1" =~ "_java" ]]; then
        prepare_java $1
    elif [[ "$1" =~ "_c" ]]; then
        prepare_c $1
    fi
}

op="$1"
shift

# Choose affected directories
if [[ "$@" == "all" ]]; then
    echo "All directories"
    dirs=($(ls | egrep '[0-9]+_lab[0-9]+_zad[0-9]+'))
else
    echo "$# directories..."
    dirs=($@)
fi


case ${op} in
    "build" ) action=build_dir ;;
    "clean" ) action=clean_dir ;;
    "prepare" | "deploy" ) action=prepare_dir ;;
esac

for dir in ${dirs[*]}; do
    echo "Inside \`${dir}'"
    ${action} ${dir}
done


if [[ "$op" == "deploy" ]]; then

    touch "${cmd_file}"
    echo "cd ${path}" >> "${cmd_file}"

    for dir in ${dirs[*]}; do
        if [[ "${dir}" =~ "_java" ]]; then
            echo "meeeh"
            #echo "ant -f ${dir}/build.xml" >> "${cmd_file}"    
        elif [[ "${dir}" =~ "_c" ]]; then
            echo "make -C ${dir}" >> "${cmd_file}"
        fi
    done

    for remote in "${!remotes[@]}"; do
        echo "${remote}:"
        scp -r "${dirs[@]}" "${remotes[${remote}]}:${path}"
        ssh  "${remotes[${remote}]}" 'bash -s' < "${cmd_file}"
    done

    rm "${cmd_file}"
fi
