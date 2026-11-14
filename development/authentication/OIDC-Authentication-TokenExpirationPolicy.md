# Token Expiration Policy - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-TokenExpirationPolicy.html

The expiration policy for OpenID Connect tokens is controlled by CAS settings and properties. These settings generally are the same as those that are defined for the OAuth Protocol. Expiration policies may also be controlled and defined for each application registered with CAS.

[See this guide](https://apereo.github.io/cas/development/authentication/OAuth-Authentication-TokenExpirationPolicy.html) for more info.

## ID Tokens

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.id-token.collector-script.location` (Optional; type `Resource`)
- `cas.authn.oidc.id-token.generate-for-jwt-bearer-grant-type` (Optional; type `Boolean`; default `false`): Flag to indicate whether the JWT bearer grant type is supported for the purposes of requesting ID tokens. Setting this to true will force to generate ID tokens when the JWT bearer grant type is used.
- `cas.authn.oidc.id-token.include-id-token-claims` (Optional; type `Boolean`; default `true`): As per OpenID Connect Core section 5.4, "The Claims requested by the `profile`, `email`, `address`, and `phone` scope values are returned from the userinfo endpoint", except for `response_type`=`id_token`, where they are returned in the id_token (as there is no access token issued that could be used to access the userinfo endpoint). The Claims requested by the profile, email, address, and phone scope values are returned from the userinfo endpoint when a `response_type` value is used that results in an access token being issued. However, when no access token is issued (which is the case for the `response_type` value `id_token`), the resulting Claims are returned in the ID Token. Setting this flag to true will force CAS to include claims in the ID token regardless of the response type. Note that this setting **MUST ONLY** be used as a last resort, to stay compliant with the specification as much as possible. ** DO NOT** use this setting without due consideration. Note that this setting is set to `true` by default mainly provided to preserve backward compatibility with previous CAS versions that included claims into the ID token without considering the response type. The behavior of this setting may change and it may be removed in future CAS releases.
- `cas.authn.oidc.id-token.max-time-to-live-in-seconds` (Optional; type `String`; default `PT8H`): Hard timeout to kill the ID token and expire it.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

### Per Service

The expiration policy of certain ID tokens can be conditionally decided on a per-application basis. The candidate service whose id token expiration policy is to deviate from the default configuration must be designed as the following snippets demonstrate.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "Service",
  "id" : 1,
  "idTokenExpirationPolicy": {
    "@class": "org.apereo.cas.oidc.services.DefaultRegisteredServiceOidcIdTokenExpirationPolicy",
    "timeToKill": "PT60S"
  }
}
```
