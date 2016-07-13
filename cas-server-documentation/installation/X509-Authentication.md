---
layout: default
title: CAS - X.509 Authentication
---

# X.509 Authentication
CAS X.509 authentication components provide a mechanism to authenticate users who present client certificates during
the SSL/TLS handshake process. The X.509 components require configuration outside the CAS application since the
SSL handshake happens outside the servlet layer where the CAS application resides. There is no particular requirement
on deployment architecture (i.e. Apache reverse proxy, load balancer SSL termination) other than any client
certificate presented in the SSL handshake be accessible to the servlet container as a request attribute named
`javax.servlet.request.X509Certificate`. This happens naturally for configurations that terminate SSL connections
directly at the servlet container and when using Apache/mod_jk; for other architectures it may be necessary to do
additional work.


## Overview
X.509 support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-x509-webflow</artifactId>
  <version>${cas.version}</version>
</dependency>
```

CAS provides an X.509 authentication handler, a handful of X.509-specific principal resolvers, some certificate
revocation machinery, and some Webflow actions to provide for non-interactive authentication.

The X.509 handler technically performs additional checks _after_ the real SSL client authentication process performed
by the Web server terminating the SSL connection. Since an SSL peer may be configured to accept a wide range of
certificates, the CAS X.509 handler provides a number of properties that place additional restrictions on
acceptable client certificates.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## Certificate Revocation Checking

CAS provides a flexible policy engine for certificate revocation checking. This facility arose due to lack of
configurability in the revocation machinery built into the JSSE.

The following configuration is shared by all components:

| Field                             | Description
|-----------------------------------+---------------------------------------------------------
| `unavailableCRLPolicy`    | Policy applied when CRL data is unavailable upon fetching. (default=`DenyRevocationPolicy`)
| `expiredCRLPolicy`        | Policy applied when CRL data is expired. (default=`ThresholdExpiredCRLRevocationPolicy`)

The following policies are available by default:

| Policy
|--------------------------------------
| `allowRevocationPolicy`
| `denyRevocationPolicy`
| `thresholdExpiredCRLRevocationPolicy`

### Fixed Resource
Performs a certificate revocation check against a CRL hosted at a fixed location. The CRL is fetched at periodic intervals and cached.

```xml
<alias name="resourceCrlRevocationChecker" alias="x509RevocationChecker" />
<util:set id="x509CrlResources" />
...
<alias name="allowRevocationPolicy" alias="x509ResourceUnavailableRevocationPolicy" />
<alias name="thresholdExpiredCRLRevocationPolicy" alias="x509ResourceExpiredRevocationPolicy" />
```

### CRL URL(s)
Performs certificate revocation checking against the CRL URI(s) mentioned in the certificate _cRLDistributionPoints_
extension field. The component leverages a cache to prevent excessive IO against CRL endpoints; CRL data is fetched
if does not exist in the cache or if it is expired.

```xml
<alias name="crlDistributionPointRevocationChecker" alias="x509RevocationChecker" />
...
<alias name="allowRevocationPolicy" alias="x509CrlUnavailableRevocationPolicy" />
<alias name="thresholdExpiredCRLRevocationPolicy" alias="x509CrlExpiredRevocationPolicy" />
```

## CRL Fetching
By default, all revocation checks use the `ResourceCRLFetcher` component to fetch the CRL resource from the specified location.

```xml
<alias name="resourceCrlFetcher" alias="x509CrlFetcher" />
```

The following alternatives are available:

### LDAP Attribute
Fetches a CRL resource from a preconfigured attribute, in the event that the CRL resource is an LDAP instance.

```xml
<alias name="ldaptiveResourceCRLFetcher" alias="x509CrlFetcher" />
<alias name="customLdapSearchExecutor" alias="ldaptiveResourceCRLSearchExecutor" />
<alias name="customLdapConnectionConfig" alias="ldaptiveResourceCRLConnectionConfig" />
```

### Pooling LDAP Attribute
Fetches a CRL resource from a preconfigured attribute, in the event that the CRL resource is an LDAP instance. 
This component is able to use connection pooling.

```xml
<alias name="poolingLdaptiveResourceCRLFetcher" alias="x509CrlFetcher" />
<alias name="customLdapConnectionPool" alias="poolingLdaptiveConnectionPool" />
<alias name="customLdapSearchExecutor" alias="poolingLdaptiveResourceCRLSearchExecutor" />
<alias name="customLdapConnectionConfig" alias="poolingLdaptiveResourceCRLConnectionConfig" />
```

## Web Server Configuration

X.509 configuration requires substantial configuration outside the CAS Web application. The configuration of Web
server SSL components varies dramatically with software and is outside the scope of this document. We offer some
general advice for SSL configuration:

* Configuring SSL components for optional client certificate behavior generally provides better user experience.
Requiring client certificates prevents SSL negotiation in cases where the certificate is not present, which prevents
user-friendly server-side error messages.
* Accept certificates only from trusted issuers, generally those within your PKI.
* Specify all certificates in the certificate chain(s) of allowed issuers.

## X.509 Webflow

Replace all instances of the `initializeLoginForm` transition in other states with `startX509Authenticate`.
