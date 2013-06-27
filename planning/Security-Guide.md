---
layout: default
title: CAS - Security Guide
---
# Security Guide
CAS is security software that provides secure Web-based single sign-on to Web-based applications. Single sign-on
provides a win/win in terms of security and convenience: it reduces password exposure to a single, trusted credential
broker while transparently providing access to multiple services without repetitious logins. The use of CAS generally
improves the security environment, but there are several CAS configuration and deployment concerns that should be
considered to achieve suitable security.

## Secure Transport (https)
All communication with the CAS server MUST occur over a secure channel (i.e. SSLv3, TLSv1). There are two primary
justifications for this requirement:

1. The authentication process requires transmission of security credentials.
2. The CAS ticket-granting ticket is a bearer token.

Since the disclosure of either data would allow impersonation attacks, it's vitally important to secure the
communication channel between CAS clients and the CAS server.

## Proxy Tickets
Proxy pickets are a powerful, important, and potentially security-improving feature of CAS. Proxy tickets provide
delegated authentication by allowing a service to request tickets on behalf of a user. They are commonly used in
cases where a service cannot interact directly with the user and as an alternative to replaying end-user credentials
to a service.

However, proxy tickets carry risk in that services accepting proxy tickets are responsible for validating the
proxy chain (the list of services through which the end-user's authentication have been delegated to arrive at
the ticket validating service). Services can opt out of accepting proxy tickets entirely (and avoid
responsibility for validating proxy chains) by simply validating tickets against the /serviceValidate
validation endpoint, but experience has shown it's easy to be confused about this and configure to
unintentionally use the /proxyValidate endpoint yet not scrutinize any proxy chains that appear in the
ticket validation response.

Historically any service could obtain a proxy-granting ticket and from it a proxy ticket to access any other service.
In other words, the security model is decentralized rather than centralized. You can centralize some of this through
the [Service Management](#service_management) facility by restricting which services
are allowed to obtain proxy tickets. Without explicitly controlling proxy ticket capability, it is not possible to
know which services are requesting proxy tickets.

## Service Management
The service management facility provides a number of service-specific configuration controls that affect security
policy and provide some support for centralized security policy. (Note that CAS has historically supported the
decentralized security policy model.) Some highlights of service management controls:

* Authorized services
* Attribute release
* Proxy control
* Theme control

The service management facility is comprised of a service registry containing one or more registered services, each
of which specifies the management controls above. The service registry can be controlled via static configuration files,
a Web user interface, or both. See the [Service Management](installation/Service-Management.html) section for more
information.

## Ticket Expiration Policies
Ticket expiration policies are a primary mechanism for implementing security policy. Ticket expiration policy allows
control of some important aspects of CAS SSO session behavior:

* SSO session duration (sliding expiration, absolute)
* Ticket reuse

See the [Configuring Ticketing Components](../installation/Configuring-Ticketing-Components.html) section for a
detailed discussion of the various expiration policies and configuration instructions.

## ClearPass

TBD: @wgthom
