---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# OAuth Authentication - CSRF Cookie
      
Intercept the OAuth authentication request and URLs to generate CSRF tokens. This allows CAS to 
generate a CSRF token and save it as a `pac4jCsrfToken` request attribute and in the `pac4jCsrfToken` cookie.

{% include_cached casproperties.html properties="cas.authn.oauth.csrf-cookie" %}
