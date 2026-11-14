# Security Configuration

Source: https://apereo.github.io/cas/development/installation/Configuring-SAML2-Security.html

There are several levels of configuration that control the security configuration of objects that are signed, encrypted, etc. These configurations include things like the keys to use, preferred/default algorithms, and algorithms to allow, enforce or reject.

The configurations are generally determined based on the following order:

- Per-service configuration overrides
- Service provider metadata (i.e. entity attributes, etc)
- Global CAS default settings
- OpenSAML initial defaults

In almost all cases, you should leave the defaults in place.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.algs.override-allowed-algorithms` (Optional; type `List<String>`): The Override white listed algorithms.
- `cas.authn.saml-idp.algs.override-allowed-signature-signing-algorithms` (Optional; type `List<String>`): The Override allowed signature signing algorithms.
- `cas.authn.saml-idp.algs.override-blocked-encryption-algorithms` (Optional; type `List<String>`): The Override black listed encryption algorithms.
- `cas.authn.saml-idp.algs.override-blocked-signature-signing-algorithms` (Optional; type `List<String>`): The Override blocked signature signing algorithms.
- `cas.authn.saml-idp.algs.override-data-encryption-algorithms` (Optional; type `List<String>`): The Override data encryption algorithms.
- `cas.authn.saml-idp.algs.override-key-encryption-algorithms` (Optional; type `List<String>`): The Override key encryption algorithms.
- `cas.authn.saml-idp.algs.override-signature-algorithms` (Optional; type `List<String>`): The Override signature algorithms.
- `cas.authn.saml-idp.algs.override-signature-canonicalization-algorithm` (Optional; type `String`): The Override signature canonicalization algorithm.
- `cas.authn.saml-idp.algs.override-signature-reference-digest-methods` (Optional; type `List<String>`): The Override signature reference digest methods.
- `cas.authn.saml-idp.algs.private-key-alg-name` (Optional; type `String`; default `RSA`): Algorithm name to use when generating or locating private key for signing operations..

## Encryption

The following examples demonstrate encryption security configuration overrides per service provider.

**CBC**

The following example demonstrates how to configure CAS to use `CBC` encryption for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "encryptAssertions" : true,
  "encryptionDataAlgorithms": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2001/04/xmlenc#aes128-cbc"
    ]
  ],
  "encryptionKeyAlgorithms": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p"
    ]
  ]
}
```

**GCM**

The following example demonstrates how to configure CAS to use `GCM` encryption for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "encryptAssertions" : true,
  "encryptionDataAlgorithms": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2009/xmlenc11#aes128-gcm"
    ]
  ],
  "encryptionKeyAlgorithms": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p"
    ]
  ]
}
```

Note that encryption operations may also be activated and controlled using SAML2 metadata entity attributes.

```xml
<Extensions>
    <mdattr:EntityAttributes>
        <saml:Attribute Name="http://shibboleth.net/ns/profiles/encryptAssertions" 
                        NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
            <saml:AttributeValue>true</saml:AttributeValue>
        </saml:Attribute>
    </mdattr:EntityAttributes>
</Extensions>
```

## Signing

The following examples demonstrate signing security configuration overrides per service provider.

**SHA-1**

The following example demonstrates how to configure CAS to use `SHA-1` signing and digest algorithms for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "signAssertions" : true,
  "signResponses" : true,
  "signingSignatureAlgorithms": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2000/09/xmldsig#rsa-sha1",
      "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1"
    ]
  ],
  "signingSignatureReferenceDigestMethods": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2000/09/xmldsig#sha1"
    ]
  ]
}
```

**SHA-256**

The following example demonstrates how to configure CAS to use `SHA-256` signing and digest algorithms for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "signAssertions" : true,
  "signResponses" : true,
  "signingSignatureAlgorithms": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
      "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"
    ]
  ],
  "signingSignatureReferenceDigestMethods": [
    "java.util.ArrayList",
    [
      "http://www.w3.org/2001/04/xmlenc#sha256"
    ]
  ]
}
```

Note that signing operations may also be activated and controlled using SAML2 metadata entity attributes.

```xml
<Extensions>
    <mdattr:EntityAttributes>
        <saml:Attribute Name="http://shibboleth.net/ns/profiles/saml2/sso/browser/signResponses" 
                    NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
            <saml:AttributeValue>true</saml:AttributeValue>
        </saml:Attribute>
        <saml:Attribute Name="http://shibboleth.net/ns/profiles/saml2/sso/browser/signAssertions" 
                        NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri">
            <saml:AttributeValue>true</saml:AttributeValue>
        </saml:Attribute>
    </mdattr:EntityAttributes>
</Extensions>
```

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
