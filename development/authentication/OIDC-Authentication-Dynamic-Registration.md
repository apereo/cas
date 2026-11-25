# Dynamic Client Registration - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Dynamic-Registration.html

Please study [the specification](https://openid.net/specs/openid-connect-registration-1_0.html) to learn more about dynamic client registration. Clients may also identify themselves with [client ID metadata documents](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Client-ID-Metadata-Documents.html), without registering first.

The registration endpoint accepts `POST` requests where the body of the request is to contain the application registration record. By default, CAS operates in a `PROTECTED` mode where the registration endpoint requires user authentication.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.registration.client-id-metadata-document.enabled` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.registration.client-id-metadata-document.maximum-cache-duration` (Optional; type `String`; default `PT24H`)
- `cas.authn.oidc.registration.client-id-metadata-document.maximum-clients` (Optional; type `Integer`; default `1000`)
- `cas.authn.oidc.registration.client-id-metadata-document.minimum-cache-duration` (Optional; type `String`; default `PT5M`)
- `cas.authn.oidc.registration.client-id-metadata-document.retention` (Optional; type `String`; default `P30D`)
- `cas.authn.oidc.registration.client-id-metadata-document.timeout` (Optional; type `String`; default `PT5S`)
- `cas.authn.oidc.registration.client-secret-expiration` (Optional; type `String`; default `0`): When client secret is issued by CAS, this is the period that gets added to the current time measured in UTC to determine the client secret's expiration date. An example value would be `P14D` forcing client applications to expire their client secret in 2 weeks after the registration date. Expired client secrets can be updated using the client configuration endpoint. A value of `0` indicates that client secrets would never expire.
- `cas.authn.oidc.registration.dynamic-client-registration-enabled` (Optional; type `Boolean`; default `true`): Whether dynamic client registration is enabled or not.
- `cas.authn.oidc.registration.dynamic-client-registration-mode` (Optional; type `OidcClientRegistrationProperties.DynamicClientRegistrationModes`; default `PROTECTED`): Whether dynamic registration operates in `OPEN` or `PROTECTED` mode. Available values are as follows: - `OPEN`: Registration is open to all. In a situation where CAS is supporting open Client registration, it will check to see if the `logo_uri` and `policy_uri` have the same host as the hosts defined in the array of `redirect_uris`. - `PROTECTED`: registration is protected for all.
- `cas.authn.oidc.registration.initial-access-token-password` (Optional; type `String`): The password used in a basic-auth scheme to request an initial access token that would then be used to dynamically register clients in `DynamicClientRegistrationModes#PROTECTED` mode.
- `cas.authn.oidc.registration.initial-access-token-user` (Optional; type `String`): The username used in a basic-auth scheme to request an initial access token that would then be used to dynamically register clients in `DynamicClientRegistrationModes#PROTECTED` mode.

The registration endpoint’s response on a successful operation will contain information about the registered entity along with an access token found at `registration_access_token` which can be used to update or retrieve the registered entity found at `registration_client_uri`.

The redirect URIs of a registered client are matched exactly, case included: characters such as `.` and `?` stand for themselves, and any number of redirect URIs can be registered. Clients registered with an earlier version keep the pattern they were registered with until they update their registration.

## Protected Dynamic Client Registration

By default, CAS operates in a `PROTECTED` mode where the registration endpoint requires user authentication via a specially-created *master* access token that contains the necessary scopes that would allow the registration to proceed. Before you can register clients, you will need to obtain this access token by sending a `GET` request to the `/oidc/initToken` endpoint. This endpoint is protected with a Basic authentication scheme and will authenticate the request using the credentials that are specified in CAS configuration for dynamic registration. The access token that is received in the response should be passed as a `Bearer` authorization header to all dynamic registration requests.

The registration and client configuration endpoints answer as protected resources do, per [RFC 6750](https://www.rfc-editor.org/rfc/rfc6750#section-3.1): a request without an access token receives a `401` with a `WWW-Authenticate: Bearer` challenge, an unknown or expired token a `401` with `invalid_token`, and a token without the required scope a `403` with `insufficient_scope`. A [DPoP-bound](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-DPoP.html) access token is accepted only with the `DPoP` scheme and its proof, and a certificate-bound token only over a connection that presents its certificate.

## Client Configuration

Application definitions that are registered with CAS dynamically may be retrieved or updated using the `/oidc/clientConfig` endpoint. This endpoint supports `GET` and `PATCH` requests for read and update operations. Each operation expects a `client_id` parameter that allows CAS to locate the previously-registered entity for processing. Update requests using `PATCH` may also specify the updated registration requests in the request body. Update requests may also update the client’s secrets and generate a new one if the client secret is determined to be expired.
