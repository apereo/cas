---
layout: default
title: CAS - GUA Authentication
category: Authentication
---
{% include variables.html %}


# Static Resource Graphical User Authentication

Primarily useful for demo and testing purposes, this option allows CAS to load a global and static image resource
as the user identifier onto the login flow.
       
{% include {{ version }}/static-gua-authentication-configuration.md %}

### LDAP

CAS may also be allowed to locate a binary image attribute for the user from LDAP. The binary attribute value is then loaded
as the user identifier onto the login flow.

{% include {{ version }}/ldap-gua-authentication-configuration.md %}
