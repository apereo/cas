---
layout: default
title: CAS - OAuth Protocol Flow - Refresh Token
category: Authentication
---
{% include variables.html %}

# OAuth Protocol Flow - Refresh Token

The refresh token grant type retrieves a new access token from a refresh token (emitted for a previous access token),
when this previous access token is expired.

| Endpoint                | Parameters                                                                                        | Response              |
|-------------------------|---------------------------------------------------------------------------------------------------|-----------------------|
| `/oauth2.0/accessToken` | `grant_type=refresh_token&client_id=<ID>`<br/>`&client_secret=SECRET&refresh_token=REFRESH_TOKEN` | The new access token. |
