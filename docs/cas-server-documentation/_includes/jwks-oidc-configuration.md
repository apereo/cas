
```properties
# cas.authn.oidc.jwks.jwks-cache-in-minutes=60
# cas.authn.oidc.jwks.jwks-key-size=2048
# cas.authn.oidc.jwks.jwks-type=RSA|EC
```                         

You can manage the JSON web keyset for OpenID Connect as a static file resource.

```properties 
# cas.authn.oidc.jwks.jwks-file=file:/etc/cas/config/keystore.jwks
```

You can also allow CAS to reach out to an external REST API to ask for 
the JSON web keyset. The expected response code is `200`
where the response body should contain the contents of the JSON web keyset.

{% include {{ version }}/rest-integration.md configKey="cas.authn.oidc.jwks.rest" %}
