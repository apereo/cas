---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Service Ticket - REST Protocol

The below snippets show one might request a service ticket using the semantics of the CAS protocol:

```bash
POST /cas/v1/tickets/{TGT id} HTTP/1.0

service={form encoded parameter for the service url}
```

You may also specify a `renew` parameter to obtain a service ticket that can 
be accepted by a service that only wants tickets issued from the presentation 
of the user's primary credentials. In that case, user credentials have to be 
passed in the request, for example, as `username` and `password` parameters.

```bash
POST /cas/v1/tickets/{TGT id} HTTP/1.0

service={form encoded parameter for the service url}&renew=true&username=casuser&password=password
```

You may also submit service ticket requests using the semantics [SAML1 protocol](SAML-Protocol.html).

## Successful Response

```bash
200 OK
ST-1-FFDFHDSJKHSDFJKSDHFJKRUEYREWUIFSD2132
```

## JWT Service Tickets

Service tickets created by the REST protocol 
may be issued as JWTs instead. See [this guide](../installation/Configure-ServiceTicket-JWT.html) to learn more.

Support is enabled by including the following in your overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-rest-tokens" %}

{% include_cached casproperties.html properties="cas.authn.token" %}
