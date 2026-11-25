# PAR - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-PAR.html

[Pushed Authorization Request (PAR)](https://tools.ietf.org/html/draft-ietf-oauth-par) allows clients to push the payload of an OIDC authorization request to CAS via a direct request. The result of this push provides clients with a request URI that is used as reference to the data in a subsequent call to the authorization endpoint via the user-agent.

PAR fosters security by providing clients a simple means for a confidential and integrity protected authorization request. Clients requiring an even higher security level, especially cryptographically confirmed non-repudiation, are able to use JWT-based request objects in conjunction with a pushed authorization request.

PAR allows CAS to authenticate the client before any user interaction happens. The increased confidence in the identity of the client during the authorization process allows the authorization server to refuse illegitimate requests much earlier in the process, which can prevent attempts to spoof clients or otherwise tamper with or misuse an authorization request.

A typical exchange would allow the client to *push* the authorization request via `POST` to the `oidcPushAuthorize` endpoint. The result of this request typically would produce a `request_uri` as such:

```json
{
  "expires_in": 30,
  "request_uri": "OPAR-1-..."
}
```

The `request_uri` parameter could then be submitted back to CAS’ authorization endpoint to restore and resume the request.

A pushed authorization request may bind the authorization code to the client’s DPoP key, with the `dpop_jkt` parameter or a `DPoP` proof header. See [DPoP authorization code binding](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-DPoP.html#authorization-code-binding).

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.par.max-time-to-live-in-seconds` (Optional; type `String`; default `PT30S`): Hard timeout to kill the PAR token and expire it.
- `cas.authn.oidc.par.number-of-uses` (Optional; type `Long`; default `1`): Controls number of times a request can be used within CAS server.
- `cas.authn.oidc.par.storage-name` (Optional; type `String`; default `oidcPushedAuthzRequestsCache`): The storage object name used and created by CAS to hold PARs in the backing ticket registry implementation.
