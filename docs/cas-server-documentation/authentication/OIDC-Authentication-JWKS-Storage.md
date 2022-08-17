---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWKS Storage - OpenID Connect Authentication

The following strategies can be used to generate, manage and storage a JSON Web keystore.

## Default

By default, a global keystore can be expected and defined via CAS properties as a path on the file system. The format 
of the keystore file is similar to the following:

```json
{
  "keys": [
    {
      "d": "...",
      "e": "AQAB",
      "use": "sig",
      "n": "...",
      "kty": "RSA",
      "kid": "cas",
      "state": 0
    }
  ]
}
```
 
The contents of the keystore may be encrypted via CAS 
configuration security [outlined here](../configuration/Configuration-Properties-Security-CAS.html).

<div class="alert alert-info"><strong>Clustered Deployments</strong><p>
When deploying CAS in a cluster, you must make sure all CAS server nodes have access to 
and share an <strong>identical and exact copy</strong> of the keystore file. Keystore differences
will lead to various validation failures and application integration issues.
</p></div>

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.file-system" %}

The keystore is automatically watched and monitored by CAS for changes. As changes are detected, CAS
will invalidate the cache and will reload the keystore once again.
 
## JPA
     
Keystore generation can be outsourced to an external relational database, such as MySQL, etc. 

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-hibernate" %}

To learn how to configure database drivers and JPA implementation options, please [review this guide](../installation/JDBC-Drivers.html).

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.jpa" %}

## MongoDb

Keystore generation can be outsourced to an external MongoDb instance.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mongo-core" %}

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.mongo" %}

## REST

Keystore generation can be outsourced to an external REST API. Endpoints must be designed to
accept/process `application/json` and generally should return a `2xx` response status code. 

The following requests are made by CAS to the endpoint:

| Operation | Parameters            | Description                             | Result                                             |
|-----------|-----------------------|-----------------------------------------|----------------------------------------------------|
| `GET`     | N/A                   | Retrieve the keystore, or generate one. | `2xx` status code; JWKS resource in response body. |
| `POST`    | JWKS in request body. | Store the keystore.                     | `2xx` status code.                                 |

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.rest" %}
  
## Groovy

Keystore generation can be outsourced to an external Groovy script whose body should be defined as such: 

```groovy
import org.apereo.cas.oidc.jwks.*
import org.jose4j.jwk.*

def run(Object[] args) {
    def logger = args[0]
    logger.info("Generating JWKS for CAS...")
    def jsonWebKeySet = "{ \"keys\": [...] }"
    return jsonWebKeySet
}

def store(Object[] args) {
    def jwks = args[0] as JsonWebKeySet
    def logger = args[1]
    logger.info("Storing JWKS for CAS...")
    return jwks
}

def find(Object[] args) {
    def logger = args[0]
    logger.info("Looking up JWKS...")
    return new JsonWebKeySet(...)
}
```

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.groovy" %}

## Custom

It is possible to design and inject your own keystore generation strategy into CAS using the following `@Bean`
that would be registered in a `@AutoConfiguration` class:

```java
@Bean(initMethod = "generate")
public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
    return new MyJsonWebKeystoreGeneratorService(...);
}
```

Your configuration class needs to be registered 
with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.
