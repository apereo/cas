---
layout: default
title: CAS - OAuth Protocol Flow - Device Authorization
category: Authentication
---
{% include variables.html %}

# OAuth Protocol Flow - Device Authorization

The OAuth 2.0 Device Authorization Grant (formerly known as the Device Flow) is an OAuth 2.0 extension that enables 
devices with no browser or limited input capability to obtain an access token. This is commonly seen on 
Apple TV apps, or devices like hardware encoders that can stream video to a YouTube channel.

| Endpoint                | Parameters                                                    | Response                                             |
|-------------------------|---------------------------------------------------------------|------------------------------------------------------|
| `/oauth2.0/accessToken` | `response_type=device_code&client_id=<ID>`                    | Device authorization url, device code and user code. |
| `/oauth2.0/accessToken` | `response_type=device_code&client_id=<ID>&code=<DEVICE_CODE>` | New access token once the user code is approved.     |

While the device polls for an access token, CAS answers with the errors defined by [RFC 8628](https://www.rfc-editor.org/rfc/rfc8628#section-3.5):
`authorization_pending` until the user code is approved, `slow_down` when the device polls faster than the configured refresh interval,
and `expired_token` once the device code or its user code has expired. A device code that is unknown, already redeemed or issued
to another client is refused with `invalid_grant`. An expired device code is only recognized as such while CAS still holds it;
once the ticket registry has removed it, it is reported as unknown.
