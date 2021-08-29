---
layout: default
title: CAS - GUA Authentication
category: Authentication
---
{% include variables.html %}


# Graphical User Authentication

Graphical user authentication, sometimes also known as 'login images' are a form of login 
verification (i.e. second factor) where a site presents the user with an image previously 
selected by the user at the time the account is created. It is an "account secret" tied 
to the username that should not be easily reproduced by a phishing campaign attempting to impersonate a legitimate website.

In practice, CAS prompts the user for only their username and responds with a page 
displaying what should be the user's pre-selected image along with a password field 
to complete their authentication. The user in turn is to be trained to refuse 
submitting the rest of their login credentials to a site posing to be legitimate if CAS fails to present the correct image.

## Overview

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gua" %}

### Resource

Please [see this guide](GUA-Authentication-Storage-Resource.html).

### LDAP

Please [see this guide](GUA-Authentication-Storage-LDAP.html).
