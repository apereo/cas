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
