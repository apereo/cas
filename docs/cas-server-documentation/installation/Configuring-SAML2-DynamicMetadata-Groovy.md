---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Groovy - SAML2 Metadata Management

A metadata location for a SAML service definition may point to an external Groovy script, allowing the script to programmatically
determine and build the metadata resolution machinery to be added to the collection of the existing resolvers.

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "description" : "A Groovy-based metadata resolver",
  "metadataLocation" : "file:/etc/cas/config/groovy-metadata.groovy"
}
```

The outline of the script may be as follows:

```groovy
import java.util.*
import org.apereo.cas.support.saml.*
import org.apereo.cas.support.saml.services.*
import org.opensaml.saml.metadata.resolver.*

def run(final Object... args) {
    def (registeredService,samlConfigBean,samlProperties,criteriaSet,logger) = args
    /*
      This is where you build the relevant metadata resolver instance(s).
    */
    def metadataResolver = ...
    return metadataResolver
}
```

The parameters passed are as follows:

| Parameter           | Description                                                                                                        |
|---------------------|--------------------------------------------------------------------------------------------------------------------|
| `registeredService` | The object representing the corresponding service definition in the registry.                                      |
| `samlConfigBean`    | The object representing the OpenSAML configuration class holding various builder and marshaller factory instances. |
| `samlProperties`    | The object responsible for capturing the CAS SAML IdP properties defined in the configuration.                     |
| `criteriaSet`       | The object responsible for capturing the criteria for metadata solution, if any.                                   |
| `logger`            | The object responsible for issuing log messages such as `logger.info(...)`.                                        |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
