---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Ticket-Granting Ticket REST Protocol

Ticket-granting tickets can be issued by the REST protocol:

```bash
POST /cas/v1/tickets HTTP/1.0
'Content-type': 'Application/x-www-form-urlencoded'
username=battags&password=password&additionalParam1=paramvalue
```

You may also specify a `service` parameter to verify whether the 
authenticated user may be allowed to access the given service.

## Successful Response

```bash
201 Created
Location: http://www.whatever.com/cas/v1/tickets/{TGT id}
```

## Unsuccessful Response

If incorrect credentials are sent, CAS will respond with a `401 Unauthorized`. A `400 Bad Request` error 
will be sent for missing parameters, etc. If you send a media type it does not 
understand, it will send the `415 Unsupported Media Type`.

## JWT Ticket Granting Tickets

Ticket-granting tickets created by the REST protocol may be issued as 
JWTs instead. Support is enabled by including the following in your overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-rest-tokens" %}

To request a ticket-granting ticket as JWT next, ensure the `POST` request matches the following:

```bash
POST /cas/v1/tickets HTTP/1.0

username=battags&password=password&token=true&additionalParam1=paramvalue
```

The `token` parameter may either be passed as a request parameter or a request 
header. The body of the response will include the ticket-granting ticket as 
a JWT. Note that JWTs created are typically signed and encrypted by default with pre-generated keys. 

{% include casproperties.html properties="cas.authn.token" %}

## X509 Authentication

The feature extends the CAS REST API communication model to non-interactive X.509 authentication
where REST credentials may be retrieved from a certificate embedded in the request rather than
the usual and default username/password.

This pattern may be of interest in cases where the internal network architecture hides
the CAS server from external users behind firewall, reverse proxy, or a messaging bus and
allows only trusted applications to connect directly to the CAS server.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>The X.509 feature over REST
using a body parameter or a http header provides a tremendously convenient target for claiming
user identities or obtaining TGTs without proof of private key ownership.
To securely use this feature, network configuration <strong>MUST</strong> allow connections
to the CAS server only from trusted hosts which in turn have strict security limitations and
logging. It is also recommended to make sure that the body parameter or the http header can only come
from trusted hosts and not from the original authenticating client.</p></div>

It is also possible to let the servlet container validate the TLS client key / X.509 certificate
during TLS handshake, and have CAS server retrieve the certificate from the container.

Support is enabled by including the following in your overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-rest-x509" %}

{% include casproperties.html properties="cas.rest.x509" %}

### Body Parameter

```bash
POST /cas/v1/tickets HTTP/1.0
cert=<ascii certificate>
```
