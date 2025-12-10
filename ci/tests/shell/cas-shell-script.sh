clear
help
version
encrypt-value --value=SOMEVALUE --password=P@$$w0rd --alg=PBEWITHSHAAND40BITRC4 --provider=BC
encrypt-value --value=SOMEVALUE --password=P@$$w0rd --alg=PBEWITHSHAAND40BITRC4 --provider=BC
encrypt-value --value=SOMEVALUE --password=P@$$w0rd --alg=PBEWITHSHAAND40BITRC4 --provider=BC --initVector=false
decrypt-value --value={cas-cipher}iARpnWTURDdiAhWdcHXxqJpncj4iRo3w9i2UT33stcs= --password=JASTYPTPW --alg=PBEWITHSHAAND40BITRC4 --provider=BC
decrypt-value --value={cas-cipher}BvHnbgPin/9TaT4fgctwmtrZzwdRQWGUolr3dS1peGETCWFJOVYgu/Fkg+lxm6QX --password=P@$$w0rd --alg=PBEWITHSHAAND40BITRC4 --provider=BC --initVector=false
jasypt-test-algorithms
generate-idp-metadata --metadataLocation=$PWD/saml --subjectAltNames=cas.example.com,cas.example.io,cas.example.net --force=true
generate-key --key-size 256
export-props --dir=/tmp
generate-ddl --dialect=MYSQL --file=./cas-db-schema.sql
generate-jwt --subject=casuser
find --name=cas.server.name
generate-anonymous-user --username=casuser --service=example --salt=ythr91%^1
validate-endpoint --url=https://apereo.github.io
quit
