---
layout: default
title: CAS - GUA Authentication
category: Authentication
---
{% include variables.html %}


# LDAP Graphical User Authentication

CAS may also be allowed to locate a binary image attribute for the user from LDAP. The binary 
attribute value is then loaded as the user identifier onto the login flow.

{% include casproperties.html properties="cas.authn.gua.ldap" %}
