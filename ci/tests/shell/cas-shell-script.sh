help
encrypt-value value SOMEVALUE password JASTYPTPW alg PBEWITHSHAAND256BITAES-CBC-BC provider BC
encrypt-value --value SOMEVALUE --password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC
encrypt-value --value SOMEVALUE --password JASTYPTPW --alg PBEWITHSHAAND256BITAES-CBC-BC --provider BC --initvector
decrypt-value value {cas-cipher}iARpnWTURDdiAhWdcHXxqJpncj4iRo3w9i2UT33stcs= password JASTYPTPW alg PBEWITHSHAAND256BITAES-CBC-BC provider BC
decrypt-value value {cas-cipher}Snynjp9UvY1VohHy+L5ig9ydKUw/E3yaEsWsxmS1eiQwxwJMPtjpuCNPaBOyPhQs password JASTYPTPW alg PBEWITHSHAAND256BITAES-CBC-BC provider BC --initvector
jasypt-test-algorithms
generate-idp-metadata --metadataLocation "./" --subjectAltNames "cas.example.com,cas.example.io,cas.example.net" --force
generate-key --key-size 256
export-props --dir /tmp
generate-ddl --dialect HSQL --file ./cas-db-schema.sql
generate-jwt --subject casuser
find --name cas.server.name
generate-anonymous-user --username casuser --service example --salt ythr91%^1
validate-endpoint --url https://google.com
quit
