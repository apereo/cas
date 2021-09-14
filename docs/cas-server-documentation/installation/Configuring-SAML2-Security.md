---
layout: default
title: CAS - SAML2 Security Configuration
category: Protocols
---
{% include variables.html %}


# Security Configuration

There are several levels of configuration that control the security configuration
of objects that are signed, encrypted, etc. These configurations include things
like the keys to use, preferred/default algorithms, and algorithms to allow, enforce or reject.

The configurations are generally determined based on the following order:

- Service provider metadata
- Per-service configuration overrides
- Global CAS default settings
- OpenSAML initial defaults

In almost all cases, you should leave the defaults in place.

{% include_cached casproperties.html properties="cas.authn.saml-idp.algs,cas.authn.saml-idp.logout,cas.authn.saml-idp.profile,cas.authn.saml-idp.response,cas.authn.saml-idp.ticket" %}

## Encryption

The following examples demonstrate encryption security configuration overrides per service provider.

### CBC

The following example demonstrates how to configure CAS to use `CBC` encryption for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
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

### GCM

The following example demonstrates how to configure CAS to use `GCM` encryption for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
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

## Signing

The following examples demonstrate signing security configuration overrides per service provider.

### SHA-1

The following example demonstrates how to configure CAS to use `SHA-1` signing and digest algorithms for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
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

### SHA-256

The following example demonstrates how to configure CAS to use `SHA-256` signing and digest algorithms for a particular service provider:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "sp.example.org",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
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

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>

<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```
