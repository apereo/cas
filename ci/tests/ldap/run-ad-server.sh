#!/bin/bash

# This script starts up a SAMBA domain controller that looks and acts very much like a Windows Active Directory
# domain controller. In this script, only the LDAP related ports are exposed but SAMBA also supports other
# protocols such as Kerberos that might be relevant for CAS tests.
# This docker image used is a fork of the well documented https://github.com/Fmstrat/samba-domain.
# This script does some things in order to work on docker for windows in msys2 bash (and maybe git bash)
#
# There is a section below where samba-tool is used to create users for various tests.
# If more users are needed one can exec into the container and explore the command line options of samba-tool.
# After running this script (so samba container running), execute the following:
# docker exec samba bash -c "samba-tool user create --help"
# To go into the container and explore, run:
# docker exec -it samba /bin/bash
# The container also contains the ldap-utils package so users could be imported via LDIF files.

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

# Certain things might be easier if INSECURELDAP and NOCOMPLEXITY were true but this tests more paths.
# Having complexity enabled could be used to test handling for password change errors
# Currently allowing INSECURELDAP so JndiProvider can be tested until JDK-8217606 is fixed
# This container only exposes LDAP related ports but container also does kerberos, etc
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

#Disable password history at the domain level.
docker exec samba bash -c "samba-tool domain passwordsettings set --history-length=0"

#Disable password min-age at the domain level
docker exec samba bash -c "samba-tool domain passwordsettings set --min-pwd-age=0"

# Create users that can be used by various tests (e.g. authentication tests, password change tests, etc.
# If we aren't setting up brand new instance these will fail if they already exist but that is OK.
echo Creating users for tests

docker exec samba bash -c "samba-tool user setpassword --filter=samAccountName=Administrator --newpassword=$DOMAINPASS"

docker exec samba bash -c "samba-tool user create admin $DEFAULT_TESTUSER_PASSWORD --given-name=Joe --surname=Admin --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=cn=admin --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user create aburr $DEFAULT_TESTUSER_PASSWORD --given-name=Aaron --surname=Burr --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=samAccountName=aburr --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user create aham $DEFAULT_TESTUSER_PASSWORD --given-name=Alexander --surname=Hamilton --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=samAccountName=aham --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user create expireduser $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=cn=expireduser --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user create disableduser $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=cn=disableduser --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user create changepassword $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn --mail-address=changepassword@example.org --telephone-number=1234567890 --department='DepartmentQuestion' --company='CompanyAnswer' --description='DescriptionQuestion' --physical-delivery-office=PhysicalDeliveryOfficeAnswer "
docker exec samba bash -c "samba-tool user setpassword --filter=cn=changepassword --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user create changepasswordnoreset $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=cn=changepasswordnoreset --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool user setexpiry --days 0 expireduser"
docker exec samba bash -c "samba-tool user disable disableduser"
docker exec samba bash -c "samba-tool user list"
docker exec samba bash -c "samba-tool group addmembers 'Account Operators' admin"

# create a special password policy and apply it to a user
docker exec samba bash -c "samba-tool user create expirestomorrow $DEFAULT_TESTUSER_PASSWORD --use-username-as-cn"
docker exec samba bash -c "samba-tool user setpassword --filter=cn=expirestomorrow --newpassword=$DEFAULT_TESTUSER_PASSWORD"

docker exec samba bash -c "samba-tool domain passwordsettings pso create expirepasswordsoon 10 --max-pwd-age=2"
docker exec samba bash -c "samba-tool domain passwordsettings pso apply expirepasswordsoon expirestomorrow"


# Copying certificate out of the container so it can be put in a Java certificate trust store.
echo Putting cert in trust store for use by unit test
docker cp samba:/etc/samba/tls/${ORG}.${DOMAIN}.crt ${ORG}.${DOMAIN}.crt

unset  MSYS2_ARG_CONV_EXCL
unset  MSYS_NO_PATHCONV
if [[ -f ${TMPDIR}/adcacerts.jks ]] ; then
    rm ${TMPDIR}/adcacerts.jks
fi
echo Creating truststore: ${TMPDIR}/adcacerts.jks
keytool -import -noprompt -trustcacerts -file ${ORG}.${DOMAIN}.crt -alias AD_CERT -keystore ${TMPDIR}/adcacerts.jks -storepass changeit
rm ${ORG}.${DOMAIN}.crt

# For windows users using msys2 so when unit test looks for file in /tmp it will be there in c:\tmp
if [[ -d /c/$TMPDIR ]] ; then
    cp ${TMPDIR}/adcacerts.jks /c/$TMPDIR
fi
