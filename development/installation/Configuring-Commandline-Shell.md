# Command-line Shell

Source: https://apereo.github.io/cas/development/installation/Configuring-Commandline-Shell.html

The CAS command-line shell provides the ability to query the CAS server for help on available settings/modules and various other utility functions.

To invoke and work with the utility, execute:

```bash
java -jar /path/to/cas-server-support-shell-$casVersion.jar
```

…where `$casVersion` needless to say is the CAS version that is deployed.

The interface that is next presented will guide you through with available parameters and methods of querying. You will learn how to launch into the interactive shell and query the CAS engine dynamically.

> **JCE Requirement**
>
> Make sure you have the proper JCE bundle installed in your Java environment that is used by CAS, specially if you need to use specific signing/encryption algorithms and methods. Be sure to pick the right version of the JCE for your Java version. Java versions can be detected via the `java -version` command.

Note that the [WAR Overlay deployment strategy](https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html) should already be equipped with this functionality. You should not have to do anything special and extra to interact with the shell. See the relevant overlay documentation for more info on how to invoke and work with the shell.

## Shell Commands

The following commands are available and exposed by the CAS command-line shell.

### cipher-text,encode-text

Sign and encrypt text data using keys

| Name | Description | Default |
| --- | --- | --- |
| `value` | `Value to put through the cipher` |  |
| `secretKeyEncryption` | `Encryption key` |  |
| `encryptionAlg` | `Encryption alg` | `A256CBC-HS512` |
| `secretKeySigning` | `Signing key` |  |
| `secretKeyEncryptionSize` | `Encryption key size` | `512` |
| `secretKeySigningSize` | `Signing key size` | `512` |
| `encryptionEnabled` | `Whether value should be encrypted` | `true` |
| `signingEnabled` | `Whether value should be signed` | `true` |

### generate-key

Generate signing/encryption crypto keys for CAS settings

| Name | Description | Default |
| --- | --- | --- |
| `key-size` | `Key size` | `256` |

### decipher-text,decode-text

Decrypt and verify text data using keys

| Name | Description | Default |
| --- | --- | --- |
| `value` | `Value to put through the cipher` |  |
| `secretKeyEncryption` | `Encryption key` |  |
| `encryptionAlg` | `Encryption alg` | `A256CBC-HS512` |
| `secretKeySigning` | `Signing key` |  |
| `secretKeyEncryptionSize` | `Encryption key size` | `512` |
| `secretKeySigningSize` | `Signing key size` | `512` |
| `encryptionEnabled` | `Whether value should be encrypted` | `true` |
| `signingEnabled` | `Whether value should be signed` | `true` |

### generate-ddl

Generate database DDL scripts

| Name | Description | Default |
| --- | --- | --- |
| `file` | `DDL file to contain to generated script` | `/etc/cas/config/cas-db-schema.sql` |
| `dialect` | `Database dialect class` | `HSQL` |
| `jdbcUrl` | `JDBC database connection URL` | `jdbc:hsqldb:mem:cas` |
| `delimiter` | `Delimiter to use for separation of statements when generating SQL` | `;` |
| `pretty` | `Format DDL scripts and pretty-print the output` | `false` |
| `dropSchema` | `Generate DROP SQL statements in the DDL` | `false` |
| `createSchema` | `Generate DROP SQL statements in the DDL` | `false` |
| `haltOnError` | `Halt if an error occurs during the generation process` | `false` |

### decrypt-value

Decrypt a CAS property value/setting via Jasypt

| Name | Description | Default |
| --- | --- | --- |
| `value` | `Value to decrypt` |  |
| `alg` | `Algorithm to use to decrypt` |  |
| `provider` | `Security provider to use to decrypt` |  |
| `password` | `Password (encryption key) to decrypt` |  |
| `initVector` | `Use initialization vector to encrypt` | `false` |
| `iterations` | `Key obtention iterations to decrypt, default 1000` |  |

### encrypt-value

Encrypt a CAS property value/setting via Jasypt

| Name | Description | Default |
| --- | --- | --- |
| `value` | `Value to encrypt` |  |
| `file` | `File to encrypt` |  |
| `alg` | `Algorithm to use to encrypt` |  |
| `provider` | `Security provider to use to encrypt` |  |
| `password` | `Password (encryption key) to encrypt` |  |
| `initVector` | `Use initialization vector to encrypt` | `false` |
| `iterations` | `Key obtention iterations to encrypt, default 1000` |  |

### jasypt-list-algorithms

List algorithms you can use with Jasypt for property encryption

| Name | Description | Default |
| --- | --- | --- |
| `includeBC` | `Include Bouncy Castle provider` | `false` |

### jasypt-list-providers

List encryption providers with PBE Ciphers you can use with Jasypt

| Name | Description | Default |
| --- | --- | --- |
| `includeBC` | `Include Bouncy Castle provider` | `false` |

### jasypt-test-algorithms

Test encryption algorithms you can use with Jasypt to make sure encryption and decryption both work

| Name | Description | Default |
| --- | --- | --- |

### generate-full-jwt

Generate JWT and sign it using a given keystore

| Name | Description | Default |
| --- | --- | --- |
| `jwks` | `Path to the JWKS file used to sign the token` |  |
| `iss` | `Issuer` | `https://localhost:8443/cas/oidc` |
| `claims` | `JWT claims as JSON` | `{}` |
| `aud` | `Audience` | `CAS` |
| `exp` | `Expiration in seconds` | `300` |
| `sub` | `Subject` |  |

### generate-jwt

Generate a JWT with given size and algorithm for signing and encryption.

| Name | Description | Default |
| --- | --- | --- |
| `signingSecretSize` | `Size of the signing secret` | `256` |
| `encryptionSecretSize` | `Size of the encryption secret` | `48` |
| `signingAlgorithm` | `Algorithm to use for signing` | `HS256` |
| `encryptionAlgorithm` | `Algorithm to use for encryption` | `dir` |
| `encryptionMethod` | `Method to use for encryption` | `A192CBC-HS384` |
| `subject` | `Subject to use for the JWT` |  |

### generate-oidc-jwks

Generate OIDC JSON Web Keystore

| Name | Description | Default |
| --- | --- | --- |
| `jwksFile` | `Location of the JSON web keystore file.` | `/etc/cas/config/keystore.jwks` |
| `jwksKeyId` | `The key identifier to set for the generated key in the keystore.` | `cas` |
| `jwksKeySize` | `The key size (algorithm-specific) for the generated jwks.` | `2048` |
| `jwksKeyType` | `The type of the JWKS used to handle signing/encryption of authentication tokens.` | `RSA` |

### add-properties

Add properties associated with a CAS group/module to a Properties/Yaml configuration file.

| Name | Description | Default |
| --- | --- | --- |
| `file` | `Path to the CAS configuration file` | `/etc/cas/config/cas.properties` |
| `group` | `Group/module whose associated settings should be added to the CAS configuration file` |  |

### convert-props

Convert CAS properties to YAML file at the same location.

| Name | Description | Default |
| --- | --- | --- |
| `properties` | `Path to a properties file that contains CAS settings` | `/etc/cas/config/cas.properties` |

### export-props

Export CAS properties and settings from configuration metadata.

| Name | Description | Default |
| --- | --- | --- |
| `dir` | `Path to a directory where reference configuration files would be exported.` | `./etc/cas/config` |

### find

Look up properties associated with a CAS group/module.

| Name | Description | Default |
| --- | --- | --- |
| `name` | `Property name regex pattern` | `.+` |
| `strict` | `Whether pattern should be done in strict-mode which means the matching engine tries to match the entire region for the query.` | `false` |
| `summary` | `Whether results should be presented in summarized mode` | `false` |

### generate-idp-metadata

Generate SAML2 IdP Metadata

| Name | Description | Default |
| --- | --- | --- |
| `metadataLocation` | `Directory location to hold metadata and relevant keys/certificates` | `/etc/cas/saml` |
| `entityId` | `Entity ID to use for the generated metadata` | `cas.example.org` |
| `serverPrefix` | `CAS server prefix to be used at the IdP host name when generating metadata` | `https://cas.example.org/cas` |
| `scope` | `Scope to use when generating metadata` | `example.org` |
| `force` | `Force metadata generation (XML only, not certs), overwriting anything at the specified location` |  |
| `subjectAltNames` | `Comma separated list of other subject alternative names for the certificate (besides entityId)` |  |

### generate-anonymous-user

Generate an anonymous (persistent) username identifier

| Name | Description | Default |
| --- | --- | --- |
| `username` | `Authenticated username` |  |
| `service` | `Service application URL for which CAS may generate the identifier` |  |
| `salt` | `Salt used to generate and encode the anonymous identifier` |  |

### generate-yaml

Generate a YAML registered service definition

| Name | Description | Default |
| --- | --- | --- |
| `file` | `Path to the JSON service definition file` |  |
| `destination` | `Path to the destination YAML service definition file` |  |

### validate-service

Validate a given JSON/YAML service definition by path or directory

| Name | Description | Default |
| --- | --- | --- |
| `file` | `Path to the JSON/YAML service definition file` |  |
| `directory` | `Path to the JSON/YAML service definitions directory` |  |

### validate-endpoint

Test connections to an endpoint to verify connectivity, SSL, etc

| Name | Description | Default |
| --- | --- | --- |
| `url` | `Endpoint URL to test` |  |
| `proxy` | `Proxy address to use when testing the endpoint url` |  |
| `timeout` | `Timeout to use in milliseconds when testing the url` | `5000` |

### validate-ldap

Test connections to an LDAP server to verify connectivity, SSL, etc

| Name | Description | Default |
| --- | --- | --- |
| `url` | `LDAP URL to test, comma-separated.` |  |
| `bindDn` | `bindDn to use when testing the LDAP server` |  |
| `bindCredential` | `bindCredential to use when testing the LDAP server` |  |
| `baseDn` | `baseDn to use when testing the LDAP server, searching for accounts (i.e. OU=some,DC=org,DC=edu)` |  |
| `searchFilter` | `Filter to use when searching for accounts (i.e. (&(objectClass=*) (sAMAccountName=user)))` |  |
| `userPassword` | `Password for the user found in the search result, to attempt authentication` |  |
| `userAttributes` | `User attributes, comma-separated, to fetch for the user found in the search result` |  |
