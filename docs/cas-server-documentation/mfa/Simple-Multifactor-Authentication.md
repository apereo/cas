---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication

Allow CAS to act as a multifactor authentication provider on its own, issuing tokens 
and sending them to end-users via pre-defined communication channels such as email or text messages.

## Configuration

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-simple-mfa" %}

{% include_cached casproperties.html properties="cas.authn.mfa.simple" excludes=".token,.mail,.sms,.bucket4j" %}

## Registration

Registration is expected to have occurred as an out-of-band process. Ultimately,
CAS expects to fetch the necessary attributes from configured attribute sources to determine communications channels for
email and/or sms. The adopter is expected to have populated user records with enough information to indicate a phone number and/or email
address where CAS could then be configured to fetch and examine those attributes to share generated tokens.

## Communication Strategy

Users may be notified of CAS-issued tokens via text messages and/or email. The
authenticated CAS principal is expected to carry enough attributes, configurable via CAS settings, in order for CAS to properly send text messages
and/or email to the end-user. Tokens may also be shared via notification strategies back by platforms such as Google Firebase, etc.

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html), or [this guide](../notifications/Notifications-Configuration.html).

{% include_cached casproperties.html properties="cas.authn.mfa.simple.mail,cas.authn.mfa.simple.sms" %}

## Rate Limiting

CAS is able to support rate-limiting for token requests based on the token-bucket
algorithm, via the [Bucket4j](https://bucket4j.com/) project. This means that token requests that reach a certain configurable capacity within
a time window may either be blocked or _throttled_ to slow down. This is done to
protect the system from overloading, allowing you to introduce a scenario to allow CAS `120` token requests per minute with a refill rate of `10` requests per
second that would continually increase in the capacity bucket. Please note that the bucket allocation strategy is specific to the client IP address.

{% include_cached casproperties.html properties="cas.authn.mfa.simple.bucket4j" %}

## Token Management

Token management and issuance can be handled by CAS directly, or can be outsources to external systems and services.

### Default

By default, tokens issued by CAS are tracked using the [ticket registry](../ticketing/Configuring-Ticketing-Components.html)
and are assigned a configurable expiration policy controlled via CAS settings. In this option, CAS itself is in charge of
managing and validating tokens using pre-configured policies and components.

{% include_cached casproperties.html properties="cas.authn.mfa.simple.token.core" %}

### REST

Token validation and management can also be outsources to an external REST API. 

{% include_cached casproperties.html properties="cas.authn.mfa.simple.token.rest" %}

The API service is primarily response for two operations: issuing tokens so they may be 
shared with the end-user and validating tokens once the end-user provides them back to CAS.

#### Generating Tokens

When tokens need to be generated, this API endpoint would be invoked via a `GET` to create the ticket identifier. The body of 
the request will contain the authenticated principal that is put through the multifactor authentication flow,
and the requesting application for which the token should be generated is passed to the API via a `service` parameter. 

The endpoint is expected to respond to token generation requests at a `/new` URL path suffix, and should produce a `2xx`
status code where the response body is expected to contain the token identifier.
                                                                 
#### Storing Tokens

Generated tokens, that are shared with end-users, can be stored via the REST API endpoint using a `POST`. The body of the request
would contain the actual token definition and details that should be stored. The API service should produce a `2xx` 
status code on successful operations.

#### Validating Tokens

Generated tokens are passed to this API to validation where the token is appended to the URL endpoint and acts as a path variable. The response 
that is returned to a `GET` call must be accompanied by a `2xx` status code where the body should contain `id` and `attributes` fields, the 
latter being optional, which represent the authenticated principal for CAS:

```json
{
  "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
  "id": "casuser",
  "attributes": {
    "@class": "java.util.LinkedHashMap",
    "names": [
      "java.util.List", ["cas", "user"]
    ]
  }
}
```
