---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# REST Protocol

The REST protocol allows one to model applications as users, programmatically acquiring
service tickets to authenticate to other applications. This means that other applications would be able
to use a CAS client  to accept Service Tickets rather than to rely upon another technology such as
client SSL certificates for application-to-application authentication of requests. This is achieved
by exposing a way to REST-fully obtain a Ticket Granting Ticket and then use that to obtain a Service Ticket.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>The REST endpoint may
 become a tremendously convenient target for brute force dictionary attacks on CAS server. Consider
 enabling throttling support to ensure brute force attacks are prevented upon authentication failures.</p></div>

## Configuration

Support is enabled by including the following to the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-rest" %}

## Multiple Credentials

The CAS REST API machinery has the ability to use multiple *credential extractors* that are
tasked with analyzing the request body in order to fetch credentials and pass them
along. While by default expected credentials that may be extracted are based on
username/password, additional modules automatically lend themselves into this design and inject
their opinionated credential extractor into the REST engine automatically so that the final
collection of credentials may be used for issuing tickets, etc.

This indicates that you may pass along multiple credentials to the REST protocol in
the request body and so long as CAS is configured to understand and extract those
credentials and the authentication machinery is configured to also execute and
validate those credentials. For instance, you may deliver a use case where two sets
of credentials in form of username/password and OTP are provided to the REST protocol
and CAS would then attempt to authenticate both credentials and produce a response
on a successful validation, assuming that authentication strategies for
username/password and OTP are properly configured in CAS.

## Ticket Granting Ticket
   
Please see [this guide](REST-Protocol-Request-TicketGrantingTicket.html).

## Authenticate Credentials

Please see [this guide](REST-Protocol-CredentialAuthentication.html).

## Request a Service Ticket

Please see [this guide](REST-Protocol-Request-ServiceTicket.html).

## Validate Service Ticket

Please see [this guide](REST-Protocol-ServiceTicket-Validation.html).

## Logout

Please see [this guide](REST-Protocol-Logout.html).

## Ticket Status

Please see [this guide](REST-Protocol-TicketStatus.html).

## Add Service

Please see [this guide](REST-Protocol-Create-Service.html).

## CAS REST Clients

In order to interact with the CAS REST API, a REST client must be used to submit credentials,
receive tickets and validate them. The following Java REST client is available
by [pac4j](https://github.com/pac4j/pac4j):

```java
var casUrlPrefix = "http://localhost:8080/cas";
var username = args[0];
var password = args[1];
var serviceUrl = args[2];
var casConfiguration = new CasConfiguration(casUrlPrefix);
var authenticator = new CasRestAuthenticator(casConfiguration);
var client = new CasRestFormClient(casConfiguration,"username","password");
var request = new MockHttpServletRequest();
var response = new MockHttpServletResponse();

var webContext = new JEEContext(request, response);
casConfiguration.init(webContext);

var credentials = new UsernamePasswordCredentials(username,password,"testclient");
var restAuthenticator = new CasRestAuthenticator(casConfiguration);

restAuthenticator.validate(credentials, webContext);
var profile = (CasRestProfile) credentials.getUserProfile();
var casCredentials = client.requestServiceTicket(serviceUrl, profile, webContext);
var casProfile = client.validateServiceTicket(serviceUrl, casCredentials, webContext);

var attributes = casProfile.getAttributes();
var mapEntries = attributes.entrySet();
for (var entry : mapEntries) {
    System.out.println(entry.getKey() + ":" + entry.getValue());
}
client.destroyTicketGrantingTicket(profile, webContext);
```

## Throttling

To understand how to throttling works in CAS, 
please review [the available options](../authentication/Configuring-Authentication-Throttling.html). 
By default, throttling REST requests is turned off. 
To activate this functionality, you will need to choose an appropriate throttler and activate it by declaring the relevant module. 
The same throttling mechanism that handles the usual CAS server endpoints for authentication
and ticket validation, etc is then activated for the REST endpoints that are supported for throttling.

## Swagger API

CAS REST API may be automatically integrated with Swagger. [See this guide](../integration/Swagger-Integration.html) for more info.
