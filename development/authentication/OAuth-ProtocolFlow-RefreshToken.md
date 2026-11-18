# OAuth Protocol Flow - Refresh Token

Source: https://apereo.github.io/cas/development/authentication/OAuth-ProtocolFlow-RefreshToken.html

The refresh token grant type retrieves a new access token from a refresh token (emitted for a previous access token), when this previous access token is expired.

| Endpoint | Parameters | Response |
| --- | --- | --- |
| `/oauth2.0/accessToken` | `grant_type=refresh_token&client_id=<ID>` `&client_secret=SECRET&refresh_token=REFRESH_TOKEN` | The new access token. |
