
args=${@}

if (( "${#args}" < 1 )); then
    echo "Usage: ./keygen.sh <name>"
    exit -1
fi

basedir=$(dirname $(readlink -f $0))

NAME=${1}
SIGNER=${basedir}/../lab4_cert_signer/signer.sh

PKIDIR=${basedir}/pki
STORE=${PKIDIR}/bank/${NAME}.jks
CSR=${PKIDIR}/bank/${NAME}.csr
CRT=${PKIDIR}/bank/${NAME}.crt
CA=${PKIDIR}/sr2013ca.crt
ALIAS=key
PW=ala123

# Generate server key
keytool \
    -genkey                                                             \
    -dname "CN=MarcinLos, OU=IET, O=AGH, L=Krakow, S=Małopolska, C=PL"  \
    -alias ${ALIAS}                                                     \
    -keypass ${PW}                                                      \
    -keystore ${STORE}                                                  \
    -storepass ${PW}                                                    \
    -keyalg "RSA"                                                       \
    -validity 360

keytool \
    -certreq                                                            \
    -alias ${ALIAS}                                                     \
    -keystore ${STORE}                                                  \
	-file ${CSR}                                                        \
	-storepass ${PW}                                                    \
    

${SIGNER} ${CSR}

rm -f ${CSR}

keytool \
    -import -trustcacerts                                               \
    -alias root                                                         \
    -file ${CA}                                                         \
    -keystore ${STORE}                                                  \
    -storepass ${PW}                                                    \

keytool \
    -importcert                                                         \
    -alias ${ALIAS}                                                     \
    -file ${CRT}                                                        \
    -keystore ${STORE}                                                  \
    -storepass ${PW}                                                    \
