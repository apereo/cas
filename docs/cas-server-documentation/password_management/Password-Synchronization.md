---
layout: default
title: CAS - Password Synchronization
category: Authentication
---
{% include variables.html %}

# Password Synchronization

CAS presents the ability to synchronize and update the account password in a variety of
destinations as part of the authentication event. If the authentication attempt is successful,
CAS will attempt to capture the provided password and update destinations that are specified
in CAS settings. Failing to synchronize an account password generally produces errors in the logs
and the event is not considered a catastrophic failure.

## LDAP

Synchronize account passwords with one or more LDAP servers. Support is enabled by including the 
following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap" %}

{% include_cached casproperties.html properties="cas.authn.password-sync.ldap" %}

## REST

Synchronize account passwords with an external REST API. Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pm-webflow" bundled="true" %}

<div class="alert alert-info">:information_source: <strong>YAGNI</strong><p>You do not need to explicitly include this module
in your configuration and overlays. This is just to tell you that it exists.</p></div>

Once this feature is turned on, CAS will reach to a remote REST endpoint passing the following response body as a `POST`: 

```json
{
    "username": "...",
    "password": "..."
}
```

{% include_cached casproperties.html properties="cas.authn.password-sync.rest" %}
