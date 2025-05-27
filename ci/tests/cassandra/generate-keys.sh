#!/bin/bash

############################
# See https://ralph.blog.imixs.com/2020/06/22/setup-a-public-cassandra-cluster-with-docker/
###########################

#############################################################################
#############################################################################
#
# Cassandra Key Generator v1.0
#
# This script can be used to generate a key pair to be used to encrypt the
# node-to-node communictaion within a cassandra cluster.
#
# The generated certificate can also be used for a client-to-node cummunication.
#
# run: ./cassandra-key-generator.sh  [CLUSTER-NAME] [PASSWORD]
# run: ./cassandra-key-generator.sh cas password
#############################################################################
#############################################################################


# Check if cluster-name and password are provided as params....
#if [ $# -le 1 ]
#  then
#    echo "*** Error: cluster-name and password is missing! "
#    echo "*** Usage: cassandra-key-generator.sh [CLUSTER-NAME] [PASSWORD])"
#    exit 0
#fi

#PASSWORD=cassandra
#CLUSTER_NAME=test
CLUSTER_NAME="cas"
PASSWORD="password"
ORGANISATION="O=Imixs, L=MUC, ST=BAY, C=DE"

KEY_STORE_PATH="$PWD/ci/tests/cassandra/security"
rm -rf "$KEY_STORE_PATH" && mkdir -p "$KEY_STORE_PATH"
KEY_STORE="$KEY_STORE_PATH/cassandra.keystore"
PKS_KEY_STORE="$KEY_STORE_PATH/cassandra.keystore.pks12"
TRUST_STORE="$KEY_STORE_PATH/cassandra.truststore"

CLUSTER_PUBLIC_CERT="$KEY_STORE_PATH/${CLUSTER_NAME}.cer"


echo "**************************************************"
echo "generating keystore and certificates...."
echo "**************************************************"


### Cluster key setup.
# Create the cluster key for cluster communication.
keytool -genkey -keyalg RSA -alias "${CLUSTER_NAME}_cluster" -keystore "$KEY_STORE" -storepass "$PASSWORD" -keypass "$PASSWORD" \
-dname "CN=$CLUSTER_NAME cluster, $ORGANISATION" \
-validity 36500

# Create the public key for the cluster which is used to identify nodes.
keytool -export -alias "${CLUSTER_NAME}_cluster" -file "$CLUSTER_PUBLIC_CERT" -keystore "$KEY_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

# Import the identity of the cluster public cluster key into the trust store so that nodes can identify each other.
keytool -import -v -trustcacerts -alias "${CLUSTER_NAME}_cluster" -file "$CLUSTER_PUBLIC_CERT" -keystore "$TRUST_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt


###
# Note:
# Optional you can also generate a second key pair for the Client
###
# Create the client key for CQL.
#keytool -genkey -keyalg RSA -alias "${CLUSTER_NAME}_client" -keystore "$KEY_STORE" -storepass "$PASSWORD" -keypass "$PASSWORD" \
#-dname "CN=$CLUSTER_NAME client, $ORGANISATION" \
#-validity 36500

# Create the public key for the client to identify itself.
#keytool -export -alias "${CLUSTER_NAME}_client" -file "$CLIENT_PUBLIC_CERT" -keystore "$KEY_STORE" \
#-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

# Import the identity of the client pub  key into the trust store so nodes can identify this client.
#keytool -importcert -v -trustcacerts -alias "${CLUSTER_NAME}_client" -file "$CLIENT_PUBLIC_CERT" -keystore "$TRUST_STORE" \
#-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt


# Create a pks12 keystore file
keytool -importkeystore -srckeystore "$KEY_STORE" -destkeystore "$PKS_KEY_STORE" -deststoretype PKCS12 \
-srcstorepass "$PASSWORD" -deststorepass "$PASSWORD"

# create openssl cer.pem and key.pem file
openssl pkcs12 -in "$PKS_KEY_STORE" -nokeys -out "${KEY_STORE_PATH}/${CLUSTER_NAME}.cer.pem" -passin pass:${PASSWORD}
echo "${CLUSTER_NAME}.cer.pem file created"
openssl pkcs12 -in "$PKS_KEY_STORE" -nodes -nocerts -out "${KEY_STORE_PATH}/${CLUSTER_NAME}.key.pem" -passin pass:${PASSWORD}
echo "${CLUSTER_NAME}.key.pem file created"

echo "**************************************************"
echo "...completed"
echo "**************************************************"
