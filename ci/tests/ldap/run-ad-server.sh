#!/bin/bash

# This script does some things in order to work on docker for windows in msys2 bash (and maybe git bash)

# Passing true as first argument will reset directory config and data
RESET=${1:-false}

EXTERNAL_LDAP_PORT=10390
EXTERNAL_LDAPS_PORT=10636
EXTERNAL_GC_PORT=13268  # Microsoft Global Catalog
EXTERNAL_GCS_PORT=13269 # Microsoft Global Catalog with LDAP SSL
TMPDIR=/tmp # set this to /c/tmp if you are in windows and running tests outside of msys2
IMAGE_NAME=hdeadman/samba-domain
DOMAINPASS=M3110nM3110n#1
DEFAULT_TESTUSER_PASSWORD=P@ssw0rd # must be "complex"
HOST_IP=127.0.0.1

# Using variables to turn off msys2 bash on windows behavior of messing with anything resembling a path
export MSYS2_ARG_CONV_EXCL="*"
export MSYS_NO_PATHCONV=1

# Creating self signed certificate
DOMAIN=example.org
ORG=cas
SUBJECT="/C=US/ST=VA/O=Apereo/CN=$DOMAIN"

docker stop samba
docker rm samba
if [[ "${RESET}" == "true" ]] ; then
    docker volume rm samba_data
    docker volume rm samba_conf
fi

# create ssl cert for directory
# run "docker volume rm samba_tls" if you want to re-create
docker volume inspect samba_tls 2> /dev/null | grep samba_tls > /dev/null
if [[ $? -ne 0 ]] ; then
    docker volume create samba_tls
    # running openssl in container b/c command has options requiring version > 1.1.1
    docker run --rm \
        --mount "type=volume,source=samba_tls,destination=/etc/samba/tls" \
        --entrypoint openssl ${IMAGE_NAME} \
        req -x509 -sha256 \
            -newkey rsa:2048 \
            -nodes \
            -subj "${SUBJECT}" \
            -addext "subjectAltName = DNS:${DOMAIN},DNS:www.${DOMAIN},DNS:${ORG}.${DOMAIN},DNS:localhost,IP:127.0.0.1" \
            -keyout /etc/samba/tls/${ORG}.${DOMAIN}.key \
            -out /etc/samba/tls/${ORG}.${DOMAIN}.crt
fi

# these don't do anything if volumes exist
docker volume create samba_data
docker volume create samba_conf

# things might be easier if INSECURELDAP and NOCOMPLEXITY were true but this tests more paths
# Having complexity enabled could be used to test handling for password change errors
# Allowing INSECURELDAP so JndiProvider can be tested until JDK-8217606 is fixed
# This container only exposes ldap related ports but container also does kerberos, etc
docker run --detach \
    -e "DOMAIN=${ORG}.${DOMAIN}" \
    -e "DOMAINPASS=${DOMAINPASS}" \
    -e "DNSFORWARDER=NONE" \
    -e "HOSTIP=${HOST_IP}" \
    -e "INSECURELDAP=true" \
    -e "NOCOMPLEXITY=false" \
    -e "USEOWNCERTS=true" \
    -p ${EXTERNAL_LDAP_PORT}:389 \
    -p ${EXTERNAL_LDAP_PORT}:389/udp \
    -p ${EXTERNAL_LDAPS_PORT}:636 \
    -p ${EXTERNAL_GC_PORT}-${EXTERNAL_GCS_PORT}:3268-3269 \
    --mount "type=volume,source=samba_data,destination=/var/lib/samba" \
    --mount "type=volume,source=samba_conf,destination=/etc/samba/external" \
    --mount "type=volume,source=samba_tls,destination=/etc/samba/tls" \
    --add-host localdc.${ORG}.${DOMAIN}:${HOST_IP} \
    -h localdc \
    --name samba \
    --privileged \
    ${IMAGE_NAME}

sleep 15 # Give it time to come up before we create users
docker logs samba

# if we aren't setting up brand new instance these will fail if they already exist
echo Creating users for tests
docker exec samba bash -c "samba-tool user create admin $DEFAULT_TESTUSER_PASSWORD --given-name=Joe --surname=Admin --use-username-as-cn"
docker exec samba bash -c "samba-tool user create aburr $DEFAULT_TESTUSER_PASSWORD --given-name=Aaron --surname=Burr"
docker exec samba bash -c "samba-tool user create aham $DEFAULT_TESTUSER_PASSWORD --given-name=Alexander --surname=Hamilton"
docker exec samba bash -c "samba-tool user create expireduser $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn"
docker exec samba bash -c "samba-tool user create disableduser $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn"
docker exec samba bash -c "samba-tool user setexpiry --days 0 expireduser"
docker exec samba bash -c "samba-tool user disable disableduser"
docker exec samba bash -c "samba-tool user list"

echo Putting cert in trust store for use by unit test
docker cp samba:/etc/samba/tls/${ORG}.${DOMAIN}.crt ${ORG}.${DOMAIN}.crt

unset  MSYS2_ARG_CONV_EXCL
unset  MSYS_NO_PATHCONV
if [[ -f ${TMPDIR}/adcacerts.jks ]] ; then
    rm ${TMPDIR}/adcacerts.jks
fi
keytool -import -noprompt -trustcacerts -file ${ORG}.${DOMAIN}.crt -alias AD_CERT -keystore ${TMPDIR}/adcacerts.jks -storepass changeit
rm ${ORG}.${DOMAIN}.crt

# For windows users using msys2 so when unit test looks for file in /tmp it will be there in c:\tmp
if [[ -d /c/$TMPDIR ]] ; then
    cp ${TMPDIR}/adcacerts.jks /c/$TMPDIR
fi
