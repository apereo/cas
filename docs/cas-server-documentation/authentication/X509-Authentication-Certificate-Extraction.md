---
layout: default
title: CAS - X.509 Authentication
category: Authentication
---
{% include variables.html %}

# X.509 Authentication - Certificate Extraction

These settings can be used to turn on and configure CAS to
extract an X509 certificate from a base64 encoded certificate
on an HTTP request header (placed there by a proxy in front of CAS).
If this is set to true, it is important that the proxy cannot
be bypassed by users and that the proxy ensures the header
never originates from the browser.

{% include_cached casproperties.html properties="cas.authn.x509.extract-cert,cas.authn.x509.ssl-header" %}

The specific parsing logic for the certificate is compatible
with the Apache Tomcat `SSLValve` which can work with headers set by
Apache HTTPD, Nginx, Haproxy, BigIP F5, etc.
