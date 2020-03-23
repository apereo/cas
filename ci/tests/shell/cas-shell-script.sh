help
encrypt-value value SOMEVALUE password JASTYPTPW alg PBEWITHSHAAND256BITAES-CBC-BC provider BC
encrypt-value --value SOMEVALUE --password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC
decrypt-value value {cas-cipher}iARpnWTURDdiAhWdcHXxqJpncj4iRo3w9i2UT33stcs= password JASTYPTPW alg PBEWITHSHAAND256BITAES-CBC-BC provider BC
jasypt-test-algorithms
generate-idp-metadata --metadataLocation "./" --subjectAltNames "cas.example.com,cas.example.io,cas.example.net" --force
generate-key --key-size 256
list-undocumented
generate-ddl --dialect HSQL --file ./cas-db-schema.sql
generate-jwt --subject casuser
find --name cas.server.name
generate-anonymous-user --username casuser --service example --salt ythr91%^1
validate-endpoint --url https://google.com
quit
