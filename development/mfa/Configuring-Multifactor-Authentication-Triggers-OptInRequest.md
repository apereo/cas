# Opt-In Request Parameter/Header - Multifactor Authentication Triggers

Source: https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers-OptInRequest.html

MFA can be triggered for a specific authentication request, provided the initial request to the CAS `/login` endpoint contains a parameter/header that indicates the required MFA authentication flow. The parameter/header name is configurable, but its value must match the authentication provider id of an available MFA provider described above.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.triggers.http.request-header` (Optional; type `String`; default `authn_method`): MFA can be triggered for a specific authentication request, provided the initial request to the CAS /login endpoint contains a request header that indicates the required MFA authentication flow. The header name is configurable, but its value must match the authentication provider id of an available MFA provider.
- `cas.authn.mfa.triggers.http.request-parameter` (Optional; type `String`; default `authn_method`): MFA can be triggered for a specific authentication request, provided the initial request to the CAS /login endpoint contains a parameter that indicates the required MFA authentication flow. The parameter name is configurable, but its value must match the authentication provider id of an available MFA provider.
- `cas.authn.mfa.triggers.http.session-attribute` (Optional; type `String`; default `authn_method`): MFA can be triggered for a specific authentication request, provided the request contains a session/request attribute that indicates the required MFA authentication flow. The attribute name is configurable, but its value must match the authentication provider id of an available MFA provider.

An example request that triggers an authentication flow based on a request parameter would be:

```bash
https://.../cas/login?service=...&<PARAMETER_NAME>=<MFA_PROVIDER_ID>
```

The same strategy also applied to triggers that are based on request/session attributes, which tend to get used for internal communications between APIs and CAS components especially when designing extensions.
