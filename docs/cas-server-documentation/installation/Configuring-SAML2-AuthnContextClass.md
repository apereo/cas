---
layout: default
title: CAS - SAML2 Authentication Context Class
category: Protocols
---
{% include variables.html %}

# SAML2 Authentication Context Class

Each service may specify a required authentication class, which may overwrite the 
appropriate field in the ultimate SAML2 response that is sent back to the service provider. 

{% include_cached casproperties.html properties="cas.authn.saml-idp" includes=".core,.response" %}

{% tabs saml2authnctx %}

{% tab saml2authnctx Static %}
     
Always use the specified authentication context class in the final response.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "https://spring.io/security/saml-sp",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "requiredAuthenticationContextClass": "https://refeds.org/profile/mfa",
}
```

{% endtab %}

{% tab saml2authnctx Groovy %}

You can always manipulate the authentication context class in more dynamic ways using a Groovy script:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "https://spring.io/security/saml-sp",
  "name": "SAML",
  "id": 1,
  "metadataLocation": "/path/to/sp-metadata.xml",
  "requiredAuthenticationContextClass": "file:///path/to/GroovyScript.groovy",
}
```

The script itself may be designed as:

```groovy
import org.apereo.cas.support.saml.web.idp.profile.builders.*

def run(final Object... args) {
    def (samlContext,logger) = args
    
    logger.info("Building context for entity {}", samlContext.adaptor.entityId)
    /**
      This is where you calculate the final context class...
    */
    return "https://refeds.org/profile/mfa"
}
```

{% endtab %}

{% tab saml2authnctx Custom %}

It is possible to design and inject your authentication context class builder
into CAS using the following `@Bean` that would be registered in a `@AutoConfiguration` class:

```java
@Bean
public SamlProfileAuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
    return new MyBuilder();
}
```

Your configuration class needs to be registered with 
CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.

{% endtab %}

{% endtabs %}
