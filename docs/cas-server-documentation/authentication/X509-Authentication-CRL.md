---
layout: default
title: CAS - X.509 Authentication
category: Authentication
---
{% include variables.html %}

# X.509 Authentication - X509 CRL Fetching / Revocation

{% include_cached casproperties.html properties="cas.authn.x509.crl-,cas.authn.x509.revocation-checker,cas.authn.x509.cache-" %}

## X.509 Authentication - LDAP CRL Fetching
         
It also possible to integrate X.509 authentication with LDAP for CRL fetching. The integration here attempts
to build `X509CRL` objects from a configurable attribute defined in settings. The attribute value is expected to be a binary base64-encoded value.
To activate this mode, the CRL fetcher in CAS must be tuned in CAS settings to use `ldap`.

{% include_cached casproperties.html properties="cas.authn.x509.ldap" %}
