---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

#  Application JWKS Management - OpenID Connect Authentication

CAS operations that require access to an applications's JSON web keystore (i.e. JWKS) typically need to 
reference an endpoint or resource to retrieve the public keys for validation, etc. This is done inside
the application registration entry.

{% tabs clientjwks %}

{% tab clientjwks Static Resource %}
   
The JWKS can be stored as a static resource on the file system 
or classpath and referenced in the application registration entry as follows:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "jwks": "file:/path/to/application-keystore.jwks"
}
```

{% endtab %}

{% tab clientjwks URL Resource %}

The JWKS can be stored as a static resource on a remote server and referenced 
in the application registration entry as follows:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "jwks": "https://app.example.org/keystore.jwks"
}
```

{% endtab %}

{% endtabs %}

## Dynamic JWKS Registration

CAS does allow client applications and relying parties to register their JWKS dynamically separately and outside
the [OpenID Connect Dynamic Client Registration protocol](../authentication/OIDC-Authentication-Dynamic-Registration.html). 
This is useful in scenarios where the application may not have a static JWKS, or can only generate it dynamically at runtime,
or that the application is registered with CAS already and yet the *real client* is
in fact a mobile device whose keys are tied to the device and not the application itself. 

{% include_cached featuretoggles.html features="OpenIDConnect.client-jwks-registration" %}
      
Once the feature is enabled, clients that wish to use this API must first obtain an access token that contains
the scope `client_jwks_registration_scope`. This scope need to be enabled and registered with the CAS server. Once the token 
is obtained, to register public keys the client can then submit a `POST` request to the endpoint `/oidc/jwks/clients`, passing 
the access token as a `Bearer` token or a request parameter under `token` with the following payload:

```json
{
  "proof": "..."
}
```

The `proof` field **MUST** be a JWT whose header contains the public key information and is signed by the client. The contents
of the JWT itself are not relevant or used. The JWT is required to record its public key information inside the `jwk` header field.
Once the proof and the public key are successfully verified, CAS will store the public key in its own datastore
and remember it by its thumbprint for future lookup operations.

### JWKS Storage

By default public keys registered dynamically are stored inside a simple *in-memory* datastore. Production 
deployments of this feature should consider implementing a more robust and persistent storage mechanism by implementing 
the following bean definition:

```java
@Bean
public ClientJwksRegistrationStore clientJwksRegistrationStore() {
    return new MyFancyStore();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
