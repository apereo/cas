# Some sample commands to create certificates with openssl ca command

# Create the intermediate CA certificate
# NOTE: The -extensions v3_ca is the key to creating a CA cert
openssl ca -config openssl.cnf -name rootCA \
  -extensions v3_ca \
  -in cas-test-intermediate-ca.csr \
  -key intermediateCA/private/cakey.pem \
  -out intermediateCA/cacert.pem

# Create a new certificate issued by CAS Test User CA
openssl req -config openssl.cnf -new \
  -out user-valid.csr -key userCA/private/cert.key
openssl ca -config openssl.cnf -name userCA \
  -key userCA/private/cakey.key \
  -in user-valid.csr \
  -out user-valid.crt

# Revoke a certificate issued by CAS Test User CA
openssl ca -config openssl.cnf \
  -revoke userCA/newcerts/0CA7.pem \
  -crl_reason keyCompromise

# Generate a CRL for CAS Test User CA
openssl ca -config openssl.cnf -name userCA \
  -gencrl \
  -out userCA/crl/crl-`cat userCA/crlnumber`.pem
