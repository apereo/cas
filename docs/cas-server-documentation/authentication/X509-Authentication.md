---
layout: default
title: CAS - X.509 Authentication
category: Authentication
---
{% include variables.html %}

# X.509 Authentication

CAS X.509 authentication components provide a mechanism to authenticate users who present client certificates during
the SSL/TLS handshake process. The X.509 components require configuration outside the CAS application since the
SSL handshake happens outside the servlet layer where the CAS application resides. There is no particular requirement
on deployment architecture (i.e. Apache reverse proxy, load balancer SSL termination) other than any client
certificate presented in the SSL handshake be accessible to the servlet container as a request attribute named
`jakarta.servlet.request.X509Certificate`. This happens naturally for configurations that terminate SSL connections
directly at the servlet container and when using `Apache/mod_jk`; for other architectures it may be necessary to do
additional work.

CAS can be configured to extract an X509 certificate from a header created by a proxy running in front of CAS.

## Overview

Certificates are exchanged as part of the SSL (also called TLS) initialization that 
occurs when any browser connects to an `https` website.
A certain number of public CA certificates are preinstalled in each browser. It is assumed that:

- Your organization is already able to generate and distribute certificates that a user can install in their browser
- Somewhere in that certificate there is a field that contains the Principal name or can be 
easily mapped to the Principal name that CAS can use.

The remaining problem is to make sure that the browsers, servers and Java are all prepared to support 
these institutional certificates and, ideally,
that these institutional certificates will be the only ones exchanged when a browser connects to CAS.

## Flow

When a browser connects to CAS over an https: URL, the server identifies itself by sending its own certificate. The 
browser must already have installed a certificate identifying and trusting the CA that issued the CAS Server certificate. 
If the browser is not already prepared to trust the CAS server, then an error message pops up saying the server is not trusted.

After the Server sends the certificate that identifies itself, it can then send a list of names of Certificate
Authorities from which it is willing to accept certificates. Ideally, this list will include only one name; the name 
of the internal institutional CA that issues internal intranet-only 
certificates that internally contain a field with the CAS Principal name.

A user may install any number of certificates into the browser from any number of CA's. If only one of these certificates 
comes from a CA named in the list of acceptable CA's sent by the server, then most browsers will automatically send that 
one certificate without asking, and some can be configured in to not ask when there is only one possible choice. This 
presents a user experience where CAS becomes transparent to the user after some initial setup and the login happens 
automatically. However, if the server hosting CAS sends more than one CA name in the list and that matches more than 
one certificate on the browser, then the user will get prompted to choose a Certificate from the list. A user interaction 
defeats much of the purpose of certificates in CAS.

Note that CAS does not control this exchange. It is handled by the underlying server. You may not have the control to 
require the server to vend only one CA name when a browser visits CAS. So if you want to use X.509 certificates in CAS, 
you should consider this requirement when choosing the hosting environment. The ideal situation is to select a server 
that can identify itself with a public certificate issued by something like VeriSign or InCommon but then require the 
client certificate only be issued by the internal corporate/campus authority.

When CAS gets control, a user certificate may have been presented by the browser and be 
stored in the request. The CAS X.509 authentication machinery examines that certificate 
and verifies that it was issued by the trusted institutional authority. Then CAS searches 
through the fields of the certificate to identify one or more fields that can 
be turned into the principal identifier that the applications expect.

While an institution can have one certificate authority that issues certificates to employees, clients, 
machines, services, and devices, it is more common for the institution to have a single "root" certificate 
authority that in its entire existence only issues a handful of certificates. Each of 
these certificates identifies a secondary Certificate Authority 
that issues a particular category of certificates (to students, staff, servers, etc.). It is possible to 
configure CAS to trust the root Authority and, implicitly, all the secondary authorities 
that it creates. This, however, makes CAS only as secure as the 
least reliable secondary Certificate Authority created by the institution. At some point in the future, 
some manager will buy a product that requires a new class of certificates. He will ask to create a 
Certificate Authority that vends these certificates to the machines running this new product. He 
will then turn administration of this mess over to a junior 
programmer or consultant. If CAS trusts any certificate issued by any Authority created by the root, 
it will trust a fraudulent certificate forged by someone who has acquired control of what was intended 
to be a special purpose, isolated CA. Therefore, it is better to configure CAS to only accept 
certificates from the one secondary CA specifically expected to issue credentials to 
individuals, instead of trusting the institutional root CA.

## Configuration

X.509 support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-x509-webflow" %}

The X.509 handler technically performs additional checks _after_ the real SSL client authentication process performed
by the Web server terminating the SSL connection. Since an SSL peer may be configured to accept a wide range of
certificates, the CAS X.509 handler provides a number of properties that place additional restrictions on
acceptable client certificates.

{% include_cached casproperties.html properties="cas.authn.x509." excludes=".ldap,.webflow" %}

