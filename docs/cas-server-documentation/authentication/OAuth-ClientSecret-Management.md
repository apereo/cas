---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Client Secret Management - OAuth Authentication
                        
By default, client secrets are directly assigned to the client application:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "name" : "...",
  "clientId": "...",
  "clientSecret": "your-client-secret",
  "serviceId" : "...",
}
```

The above setup indicates that the application has a simple static client secret assigned without an expiration date. Also,
note that client secret values can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

<div class="alert alert-info">:information_source: <strong>Note</strong><p>
The client secret construct above is mainly kept for backward compatibility reasons.
Internally, CAS translates this client secret value to a polymorphic type that
is outlined below. You may continue using this <i>legacy</i> setup if you have no need for additional
behavior that can be attached to client secrets, such as expirations and ability to rotate them.
</p></div>

## Multiple Client Secrets

You may also define multiple client secrets for a client application
and assign them each a dedicated (and yet optional) `expiration` value:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "name" : "...",
  "serviceId" : "...",
  "clientSecrets" : [ "java.util.ArrayList", [ 
    {
      "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret",
      "value" : "<secret-value1>",
      "expiration" : 1234567890
    },
    {
      "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret",
      "value" : "<secret-value2>",
      "expiration" : "09/21/2026"
    },
  ] ],
  "clientId" : "..."
}
```
      
When processing requests, CAS will iterate over the list of client secrets, disregarding those 
that might be expired already. The request is allowed to proceed if a valid non-expiring client 
secret from the assigned list can be authenticated.
  
## Client Secret Expiration

When a client secret defines an expiration value, CAS ultimately treats and transforms that value as a 
Unix epoch timestamp in seconds. The timestamp is converted into a `UTC` timezone 
and truncated to second precision if needed. The current `UTC` time is also truncated to second 
precision so both values are compared consistently. If the current time is after the configured 
expiration time, the client secret is considered expired and rejected. 
                             
A shortcut for calculating a client secret set to expire `2` weeks from now would be:

```python
#!/usr/bin/env python3

from datetime import datetime, timedelta, timezone

expires_at = datetime.now(timezone.utc) + timedelta(weeks=2)
print(int(expires_at.timestamp()))
```
                    
...or perhaps in bash:

```bash
# On macOS/BSD
date -u -v+2w +%s
# On Linux/GNU
date -u -d '+2 weeks' +%s
```

The `expiration` field may be specified as either a `UTC` epoch timestamp in seconds or 
as a date/time string. Supported formats include:

| Format                                 | Example                |
|----------------------------------------|------------------------|
| Epoch seconds                          | `2051223845`           |
| ISO local date/time                    | `2035-01-02T03:04:05`  |
| ISO zoned date/time                    | `2035-01-02T03:04:05Z` |
| 12-hour date/time with padded hour     | `01/02/2035 03:04 AM`  |
| 12-hour date/time with non-padded hour | `01/02/2035 3:04 AM`   |
| 24-hour date/time                      | `01/02/2035 03:04`     |
| Date only                              | `01/02/2035`           |
| ISO date only                          | `2035-01-02`           |

Epoch values are interpreted as seconds since the Unix epoch is in `UTC`. Date/time values 
that do not include a timezone are interpreted in `UTC`. Formats that do not include 
seconds are resolved at minute precision. 

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="oauthClientSecrets"  %}

## Encryptable Client Secrets

Client secrets for OAuth relying parties may be defined as encrypted values prefixed with `{cas-cipher}`:

```json
{
  "@class": "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "{cas-cipher}eyJhbGciOiJIUzUxMiIs...",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name": "Sample",
  "id": 100
}
```

Client secrets may be encrypted using CAS-provided cipher operations
either manually or via the [CAS Command-line shell](../installation/Configuring-Commandline-Shell.html).

{% include_cached casproperties.html properties="cas.authn.oauth" %}
