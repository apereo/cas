---
layout: default
title: Views - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# User Interface - Custom Fields

CAS allows on the ability to dynamically extend the login form by including additional 
fields, to be populated by the user. Such fields are taught to CAS using settings and are then 
bound to the authentication flow and made available to all authentication handlers that wish to 
impose additional processes and rules using said fields.

{% include_cached casproperties.html properties="cas.view.custom-login-form-fields" %}
