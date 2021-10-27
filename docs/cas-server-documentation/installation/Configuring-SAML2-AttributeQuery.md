---
layout: default
title: CAS - SAML2 Attribute Query
category: Protocols
---
{% include variables.html %}

# SAML2 Attribute Query

In order to allow CAS to support and respond to attribute queries, you need to make sure the generated metadata has
the `AttributeAuthorityDescriptor` element enabled, with protocol support enabled for `urn:oasis:names:tc:SAML:2.0:protocol`
and relevant binding that corresponds to the CAS endpoint(s). You also must ensure the `AttributeAuthorityDescriptor` tag lists all
`KeyDescriptor` elements and certificates that are used for signing as well as authentication, specially 
if the SOAP client of the service provider needs to cross-compare the certificate behind the CAS 
endpoint with what is defined for the `AttributeAuthorityDescriptor`. CAS by default
will always use its own signing certificate for signing of the responses generated as a result of an attribute query.

Also note that support for attribute queries need to be explicitly enabled and the behavior is off by default, given it imposes a burden on
CAS and the underlying ticket registry to keep track of attributes and responses as tickets and have them be later used and looked up.

{% include_cached casproperties.html properties="cas.authn.saml-idp.ticket.attribute-query" %}

## Attribute Release
   
This attribute release policy is only activated when the original request is attribute query request. The policy
authorizes the release of allowed attributes in response to an attribute query request.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "https://sp.example.org/shibboleth",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/metadata.xml",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.support.saml.services.AttributeQueryAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "employeeNumber", "mail", "sn" ] ]
  }
}
```
