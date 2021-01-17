---
layout: default
title: CAS - Configuring Authentication Components
category: Authentication
---
{% include variables.html %}


# Authentication Principal Resolution

In the event that a separate resolver is put into place, control how the final principal should be constructed by default.

{% include casproperties.html properties="cas.person-directory" %}

## Principal Transformation

Authentication handlers that generally deal with username-password credentials
can be configured to transform the user id prior to executing the authentication sequence.
Each authentication strategy in CAS provides settings to properly transform the principal.
Refer to the relevant settings for the authentication strategy at hand to learn more.

By default, CAS is configured to accept a pre-defined set of credentials 
that are supplied via the CAS configuration. The following section demonstrates how this
particular method of authentication can be assigned a group of settings that affect
the principal transformation process.

{% include casproperties.html properties="cas.authn.accept" %}
