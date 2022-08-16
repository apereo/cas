---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# LDAP Throttling Authentication Attempts

Throttled authentication attempts may be handed down to an LDAP server that would attempt to
update the user record to reflect the status of the locked account. This capability would attempt to 
locate the throttled account in LDAP to update the appropriate attribute that represents the account-locked
status. Presumably, the user account would then be blocked from future LDAP authentication attempts until
the throttled authentication period expires or the lock from account status is removed. 

<div class="alert alert-info"><strong>Usage</strong><p>
To locate the user account, the throttled submission must be able to locate the username
from the authentication request first before an LDAP search can find the user entry. This indicates
that CAS throttling must be taught to locate the username from the request using CAS settings, and
it would not be enough to relying on throttling strategies that only deal with client IP addresses, etc.</p></div>

Enable the following module in your overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-ldap" %}

{% include_cached casproperties.html properties="cas.authn.throttle.ldap" %}
