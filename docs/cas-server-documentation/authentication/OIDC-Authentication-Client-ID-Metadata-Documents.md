---
layout: default
title: CAS - Client ID Metadata Documents
category: Protocols
---
{% include variables.html %}

# Client ID Metadata Documents

CAS can accept OAuth and OpenID Connect clients that it has never seen before and that identify themselves with
a [client ID metadata document](https://datatracker.ietf.org/doc/draft-ietf-oauth-client-id-metadata-document/): the
`client_id` is an HTTPS URL, and the JSON document published at that URL describes the client. This is how
[MCP clients](../installation/Admin-MCP-Server.html#oauth-access-tokens) connect to servers they have no prior
relationship with, and the MCP authorization specification recommends it over dynamic client registration.

Support is turned off by default and is enabled and tuned through CAS settings. When it is enabled, the discovery document
advertises `client_id_metadata_document_supported`.

{% include_cached casproperties.html properties="cas.authn.oidc.registration.client-id-metadata-document" %}

An example document, published at `https://app.example.org/oauth/client.json`:

```json
{
  "client_id": "https://app.example.org/oauth/client.json",
  "client_name": "Example MCP Client",
  "client_uri": "https://app.example.org",
  "logo_uri": "https://app.example.org/logo.png",
  "redirect_uris": [
    "http://127.0.0.1:3000/callback",
    "https://app.example.org/callback"
  ],
  "grant_types": [
    "authorization_code",
    "refresh_token"
  ],
  "response_types": [
    "code"
  ],
  "token_endpoint_auth_method": "none"
}
```

## How CAS Uses The Document

When a request to an OpenID Connect endpoint names a `client_id` that is an HTTPS URL, CAS fetches the document,
checks it and registers the client in the service registry, the same way a dynamically registered client is registered.
The client is then an ordinary public client of CAS: it must use PKCE, and its redirect URIs are matched exactly.

- The client id must use `https`, contain a path, and carry no user information, query, fragment or dot segments.
- The document's `client_id` must be identical to the URL, and it must carry `client_name` and `redirect_uris`.
- Redirect URIs must use `https`, or `http` on `localhost`, `127.0.0.1` or `[::1]`, and have no fragment.
- The client may not use a shared secret: `token_endpoint_auth_method` is `none`, its default, or `private_key_jwt`
  with keys published inline in `jwks`. `client_secret` and `client_secret_expires_at` are refused.
- Only the `authorization_code` and `refresh_token` grants and the `code` response type are accepted.
- Metadata that would make CAS call URLs chosen by the client from the server side is refused: `jwks_uri`,
  `sector_identifier_uri`, `backchannel_logout_uri`, `frontchannel_logout_uri`, `initiate_login_uri`, `request_uris` and
  the CIBA notification endpoint. Post-logout redirect URIs are ignored.

These clients are evaluated after every other registered service, so they never take precedence over an application
an administrator defined. A registered client whose client id happens to be a URL, but that was not registered from a
document, is left alone.

## Fetching Documents

Documents are fetched with the following protections:

- The host is resolved once, and every address it resolves to must be public. Loopback, private, link-local,
  carrier-grade NAT, unique local, multicast, documentation and reserved addresses are always refused, and the
  connection is made to the addresses that were checked.
- Redirects are not followed, only `200` responses with a JSON body are accepted, and documents larger than 5 KB are refused.
- The request has a timeout, and TLS follows the CAS HTTP client settings, such as its trust store.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
An outbound HTTP proxy configured for the CAS server resolves the host itself, so the address checks no longer
apply to the connection it makes. Do not route these requests through such a proxy.
</p></div>

## Refresh And Cleanup

CAS uses a fetched document for as long as its `Cache-Control` header allows, within the bounds defined in CAS settings,
and fetches it again on the next request after that. If the document cannot be fetched again, CAS keeps using the last
valid one. Each successful fetch moves the expiration of the client to the retention period from then; clients that are
not used for that long expire and are removed from the service registry. The number of clients registered from
documents is capped, and new ones are refused once the cap is reached.

## Consent

Nobody has reviewed these clients, so CAS always asks for consent, even when consent would otherwise be bypassed for the
application or globally. The consent screen shows the host of the client id and the host the person is sent back to, next
to the name the client chose for itself, and warns when that host is on the person's own computer, since any program
running there could claim such an address.
