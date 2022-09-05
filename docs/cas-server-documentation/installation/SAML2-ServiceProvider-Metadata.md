---
layout: default
title: CAS - SAML2 Service Management
category: Services
---

{% include variables.html %}

# SAML2 Service Provider Metadata

SAML2 service providers that are registered with CAS can be configured to present their metadata using the following options.

## Default
        
If the SAML2 service provider is able to produce valid metadata, you may register the metadata with CAS as either a URL 
or a path to the metadata XML file or a classpath resource noted by the appropriate prefix. Using this model, CAS will 
consume the metadata directly from a published URL and/or XML file on disk, and may optionally be allowed to verify 
the signature of the metadata as necessary.

Metadata location can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

{% tabs metadata %}
{% tab metadata URL %}
```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 1,
  "metadataLocation" : "https://url/to/metadata.xml"
}
```
{% endtab %}

{% tab metadata File %}
```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 1,
  "metadataLocation" : "/path/to/metadata.xml"
}
```
{% endtab %}

{% tab metadata Directory %}

This option fetches metadata from a local directory source as needed. You are responsible 
for populating the directory with metadata files, which may be done while CAS is running. 
New metadata will be seen automatically the first time it is requested.

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : ".+",
  "name" : "SAMLService",
  "id" : 1,
  "metadataLocation" : "/path/to/metadata/directory"
}
```

Metadata files in the specified directory location must be stored as the lower case 
hex-encoded SHA-1 digest of the service provider entity id suffixed with `.xml`. For example, a service provider
with the entity id `sp1:example` should be stored in as `3494744350abe1fd8efa68c5e2696dbbdca4c33a.xml`. 

{% endtab %}

{% endtabs %}

## Dynamic Metadata

If the SP you wish to integrate with does not produce SAML metadata, you may be able to
use [this service](https://www.samltool.com/sp_metadata.php) to create the metadata,
save it in an XML file and then reference and register it with CAS for the SP.

Alternatively, you may take advantage of a standalone `saml-sp-metadata.json` file that may be found in the same directory
as the CAS metadata artifacts. The contents of this file may be as follows:

```json
{
  "https://example.org/saml": {
    "entityId": "https://example.org/saml",
    "certificate": "MIIDUj...",
    "assertionConsumerServiceUrl": "https://example.org/sso/"
  }
}
```

Each entry in the file is identified by the service provider entity id, allowing CAS to dynamically locate and build the required metadata on the fly
to resume the authentication flow. This may prove easier for those service providers that only present a URL and a signing certificate for the
integration relieving you from creating and managing XML metadata files separately.

The service providers are registered with the CAS service registry as such:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "https://example.org/saml",
  "name" : "SAMLService",
  "id" : 10000003,
  "metadataLocation" : "json://"
}
```

<div class="alert alert-info"><strong>Metadata Location</strong><p>The metadata location 
in the registration record above needs to be specified as <code>json://</code> to signal 
to CAS that SAML metadata for registered service provider must be fetched from the designated JSON file.</p></div>
