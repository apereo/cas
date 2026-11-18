# X.509 Authentication - Certificate Extraction

Source: https://apereo.github.io/cas/development/authentication/X509-Authentication-Certificate-Extraction.html

These settings can be used to turn on and configure CAS to extract an X509 certificate from a base64 encoded certificate on an HTTP request header (placed there by a proxy in front of CAS). If this is set to true, it is important that the proxy cannot be bypassed by users and that the proxy ensures the header never originates from the browser.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.extract-cert` (Optional; type `Boolean`; default `false`): Whether to extract certificate from request. The default implementation extracts certificate from header via Tomcat SSLValve parsing logic and using the `#DEFAULT_CERT_HEADER_NAME` header. Must be false by default because if someone enables it they need to make sure they are behind proxy that won't let the header arrive directly from the browser.
- `cas.authn.x509.ssl-header-name` (Optional; type `String`; default `ssl_client_cert`): The name of the header to consult for an X509 cert (e.g. when behind proxy).

The specific parsing logic for the certificate is compatible with the Apache Tomcat `SSLValve` which can work with headers set by Apache HTTPD, Nginx, Haproxy, BigIP F5, etc.
