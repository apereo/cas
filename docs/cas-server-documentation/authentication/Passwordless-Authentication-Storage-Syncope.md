---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Apache Syncope - Passwordless Authentication Storage

[Apache Syncope](../authentication/Syncope-Authentication.html) can also act as a passwordless account store.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-syncope-authentication" %}

{% include_cached casproperties.html properties="cas.authn.passwordless.accounts.syncope" %}

Once enabled, CAS will contact Apache Syncope to query for passwordless user accounts, and
will populate the email address and phone number fields of the passwordless account.
