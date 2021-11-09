---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JSON Device Storage - Multifactor Authentication Trusted Device/Browser

Records may be kept inside a static json resource whose path is defined via CAS settings.
This is also most useful if you have a very small deployment with a small 
user base or if you wish to demo the functionality.

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.json" %}
