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

- Per-service configuration overrides
- Service provider metadata (i.e. entity attributes, etc)
- Global CAS default settings
- OpenSAML initial defaults

In almost all cases, you should leave the defaults in place.

{% include_cached casproperties.html properties="cas.authn.saml-idp" includes=".algs" %}

## Encryption

The following examples demonstrate encryption security configuration overrides per service provider.

{% tabs saml2encryption %}
             
{% tab saml2encryption CBC %}

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

{% endtab %}

{% tab saml2encryption GCM %}

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

{% endtab %}

{% endtabs %}
    
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

{% tabs saml2signing %}

{% tab saml2signing SHA-1 %}

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

{% endtab %}

{% tab saml2signing SHA-256 %}

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

{% endtab %}

{% endtabs %}

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
