---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWKS - OpenID Connect Authentication

The JWKS (JSON Web Key Set) endpoint and functionality returns a JWKS containing public keys that enable 
clients to validate a JSON Web Token (JWT) issued by CAS as an OpenID Connect Provider.

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.core" %}

This keystore is cached and is reloaded at defined intervals periodically.

## Keystores
       
CAS expects a single global keystore to load and use for signing and encryption operations of various tokens. 
In addition to the global keystore, each [registered application in CAS](OIDC-Authentication-Clients.html) 
can *optionally* contain its own keystore as a `jwks` resource.

CAS will attempt to auto-generate a keystore if it can't find one at the location specified in settings. If
you wish to generate one manually, a JWKS file can be generated using [this tool](https://mkjwk.org/)
or [this tool](http://connect2id.com/products/nimbus-jose-jwt/generator).

The following strategies can be used to generate a keystore.

### Default

By default, a global keystore can be expected and defined via CAS properties as a path on the file system. The format 
of the keystore file is similar to the following:

```json
{
  "keys": [
    {
      "d": "...",
      "e": "AQAB",
      "n": "...",
      "kty": "RSA",
      "kid": "cas"
    }
  ]
}
```

<div class="alert alert-info"><strong>Clustered Deployments</strong><p>
When deploying CAS in a cluster, you must make sure all CAS server nodes have access to 
and share an <strong>identical and exact copy</strong> of the keystore file. Keystore differences
will lead to various validation failures and application integration issues.
</p></div>

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.file-system" %}

The keystore is automatically watched and monitored by CAS for changes. As changes are detected, CAS
will invalidate the cache and will reload the keystore once again.
 
### JPA
     
Keystore generation can be outsourced to an external relational database, such as MySQL, etc. 

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-hibernate" %}

To learn how to configure database drivers and JPA implementation options, please [review this guide](../installation/JDBC-Drivers.html).

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.jpa" %}

### REST

Keystore generation can be outsourced to an external REST API. Endpoints must be designed to
accept/process `application/json` and generally should return a `2xx` response status code. 

The following requests are made by CAS to the endpoint:

| Operation        | Parameters      | Description      | Result
|------------------|-----------------|------------------|----------------------------------------------------
| `GET`            | N/A             | Retrieve the keystore, or generate one.  | `2xx` status code; JWKS resource in response body.
| `POST`           | JWKS in request body. | Store the keystore.  | `2xx` status code.

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.rest" %}
  
### Groovy

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

### Custom

It is possible to design and inject your own keystore generation strategy into CAS using the following `@Bean`
that would be registered in a `@Configuration` class:

```java
@Bean(initMethod = "generate")
public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
    return new MyJsonWebKeystoreGeneratorService(...);
}
```

Your configuration class needs to be registered 
with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.

## Key Rotation

Key rotation is when a key is retired and replaced by generating a 
new cryptographic key. Rotating keys on a regular basis is an industry 
standard and follows cryptographic best practices.

You can manually rotate keys periodically to change the JSON web key (JWK) key, or you can configure the appropriate schedule
in CAS configuration so it would automatically rotate keys for you. 

<div class="alert alert-info"><strong>Rotation Guidance</strong><p>
NIST guidelines seem to recommend a rotation schedule of at least once every two years. 
In practice, modest CAS deployments in size and scale tend to rotate keys once every six months, either 
manually or automatically on a schedule.
</p></div>

Keys that are generated by CAS and put into the keystore carry an extra `state` parameter that indicates
the lifecycle status of the assigned key. The following values are accepted lifecycle states:

| Value          | Description
|----------------|---------------------------------------------------
| `0`            | The key is active and current, used for required operations.
| `1`            | The key is will be the next key used during key rotation.
| `2`            | The key is no longer used and active, and will be removed after revocation operations.

CAS always signs with only one signing key at a time, typically the *very first key* listed and loaded from the keystore,
that is deemed active and current judging by the `state` parameter. For backward compatibility, the absence of this
parameter indicates that the key is active and current.

The dynamic discovery endpoint will always include both the current key and the next key, and it may also 
include the previous key(s) if the previous key has not yet been revoked. To provide a seamless experience in 
case of an emergency, client applications should be able to use any of the keys specified in the discovery document. 

{% include_cached casproperties.html properties="cas.authn.oidc.jwks" includes=".revocation,.rotation" %}

### Custom

It is possible to design and inject your own key rotation and revocation 
strategy into CAS using the following `@Bean` that would be registered in a `@Configuration` class:

```java
@Bean
public OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService() {
    return new MyJsonWebKeystoreRotationService();
}
```

Your configuration class needs to be registered
with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="oidcJwks" %}
