---
layout: default
title: CAS - REST Authentication
category: Authentication
---

# REST Authentication

<div class="alert alert-warning"><strong>Be Careful</strong><p>This documentation describes
how to delegate and submit authentication requests to a remote REST endpoint. It has nothing
to do with the native CAS REST API, whose configuration and caveats are
<a href="../protocol/REST-Protocol.html">documented here</a>.</p></div>

REST authentication is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-rest-authentication</artifactId>
    <version>${cas.version}</version>
</dependency>
```

This allows the CAS server to reach to a remote REST endpoint via a `POST` for verification of credentials.
Credentials are passed via an `Authorization` header whose value is `Basic XYZ` where XYZ is a
Base64 encoded version of the credentials.

The response that is returned must be accompanied by a `200`
status code where the body should contain `id` and `attributes` fields, the latter being optional,
which represent the authenticated principal for CAS:

```json
{"@class":"org.apereo.cas.authentication.principal.SimplePrincipal","id":"casuser","attributes":{}}
```

Expected responses from the REST endpoint are mapped to CAS as such:

| Code                   | Result
|------------------------|---------------------------------------------
| `200`          | Successful authentication.
| `403`          | Produces a `AccountDisabledException`
| `404`          | Produces a `AccountNotFoundException`
| `423`          | Produces a `AccountLockedException`
| `412`          | Produces a `AccountExpiredException`
| `428`          | Produces a `AccountPasswordMustChangeException`
| Other          | Produces a `FailedLoginException`

## Warnings

The remote REST endpoint can send warnings back to the CAS server using custom headers.
If the authentication is successful, these warnings will be shown to the user directly after the login.

### `X-CAS-Warning`

For each `X-CAS-Warning` header present in the response, a corresponding message will be shown to the user. The header value can either be the key for a [localized message](../ux/User-Interface-Customization-Localization.html) or the message itself.

### `X-CAS-PasswordExpirationDate`

If this header is present in the response and contains a `RFC1123 date` a special message will be shown
to warn the user about the expiring password. If a [password management provider](../password_management/Password-Management.html) is configured,
the user will be able to directly change the password.

## Configuration

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#rest-authentication).
