---
layout: default
title: CAS - SAML2 Service Management
category: Services
---

{% include variables.html %}

# SAML2 Service Provider Metadata Filtering

This document describes how SAML2 service providers registered with CAS can be tuned to conditionally filter and extract
entities from the supplied metadata. The volume of metadata loaded and processed by CAS has a direct impact on memory and CPU consumption
and it generally may be considered an appropriate practice to weed and tune out entities in metadata files and resources that are considered inapplicable
or unused by CAS. 

SAML2 metadata filtering is the process that allows CAS to determine which service provider metadata entities can be 
extracted, loaded and cached from metadata sources once after the authentication request is authorized. In the end, the SAML2 service provider
is seen as authorized to proceed to the next step if its registration record explicitly authorizes CAS to accept authentication
requests and there is sufficient metadata found and attached to the request, after the filtering process, that recognizes the service provider.
                 
SAML2 metadata filtering typically follows a directive to either *include* or *exclude* an entry that matches the assigned rules. The direction of 
the filtering process is controlled by the `metadataCriteriaDirection` attribute.

{% tabs metadatafilters %}

{% tab metadatafilters Entity Attributes %}

A registered SAML2 service provider can be instructed to extract and accept entities from its metadata source
when the metadata contains specific entity attributes. 

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "^https://.+",
  "metadataLocation": "/path/to/metadata.xml",
  "metadataCriteriaDirection": "include",
  "metadataCriteriaEntityAttributes":
  {
    "@class": "java.util.HashMap",
    "http://macedir.org/entity-category":
    [
      "java.util.ArrayList",
      [
        "http://id.incommon.org/category/research-and-scholarship",
        "http://refeds.org/category/research-and-scholarship"
      ]
    ]
  }
}
```

The above registration record allows CAS to load metadata for all SAML2 service providers only when:

- Their entity ID follows a pattern that begins with `https://`
- Their metadata contains the entity attribute `http://macedir.org/entity-category`
- ...and the entity attribute carries *all values* requested in the registration entry.

{% endtab %}

{% tab metadatafilters Entity ID %}

A registered SAML2 service provider can be instructed to extract and accept entities from its metadata source
when the metadata contains specific entity IDs.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": ".+",
  "metadataLocation": "/path/to/metadata.xml",
  "metadataCriteriaDirection": "include",
  "metadataCriteriaPattern": "^https://.+.example.org",
  "metadataCriteriaRoles": "SPSSODescriptor"
}
```

The above registration record allows CAS to load metadata for all SAML2 service providers only when:

- Their entity ID matches the pattern `^https://.+.example.org`
- The entity descriptor tag type is one that belongs to a service provider.

{% endtab %}

{% endtabs %}


