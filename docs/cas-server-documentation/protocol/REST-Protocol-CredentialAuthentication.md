---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Credential Authentication - REST Protocol

Similar to asking for ticket-granting tickets, this endpoint allows 
one to only verify the validity of provided credentials as they are extracted from the request body:

```bash
POST /cas/v1/users HTTP/1.0

username=battags&password=password
```

You may also specify a `service` parameter to verify whether the authenticated 
user may be allowed to access the given service. While the above example 
shows `username` and `password` as the provided credentials, you are 
practically allowed to provide multiple sets and different types of 
credentials provided CAS is equipped to extract and recognize those 
from the request body.

A successful response will produce a `200 OK` status code along with 
a JSON representation of the authentication result, which may include 
the authentication object, authenticated principal along with 
any captured attributes and/or metadata fetched for the authenticated user.
