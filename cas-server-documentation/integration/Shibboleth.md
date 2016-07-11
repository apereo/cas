---
layout: default
title: CAS - Shibboleth Integration
---

# Overview

CAS can be integrated with the [Shibboleth federated SSO platform](http://shibboleth.net/) by a couple 
different strategies. 
It is possible to designate CAS to serve as the authentication provider for the Shibboleth IdP. 
With such a setup, when user 
is routed to the IdP, the following may take place:

- If the user has already authenticated to CAS and has a valid CAS SSO session, the IdP will transparently 
perform the requested action, e.g. attribute release.

- If the user does not have a valid CAS SSO session, the user will be redirected to CAS and must 
authenticate before the 
IDP proceeds with the requested action.


## SSO for Shibboleth IdP (RemoteUser)

Download the latest Java CAS Client Release and modify the IdP war deployable such 
that the following jars are 
included in the `./lib` installer folder, then redeploy the Idp with these files:

```bash
cas-client-$VERSION/modules/cas-client-core-$VERSION.jar
```

Define the `RemoteUser` authentication method in `$IDP_HOME/conf/handler.xml` 
to be used with CAS authentication.


```xml
<!-- Remote User handler for CAS support -->
<LoginHandler xsi:type="RemoteUser">
  <AuthenticationMethod>
    urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified
  </AuthenticationMethod>
  <AuthenticationMethod>
    urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
  </AuthenticationMethod>
</LoginHandler>
```


Add the following XML blocks to the `web.xml` file for the IdP war deployable.

```xml

<!-- For CAS client support -->
<context-param>
  <param-name>serverName</param-name>
  <param-value>${idp.hostname}</param-value>
</context-param>
CAS Filters
<!-- CAS client filters -->
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>
      org.apereo.cas.client.authentication.AuthenticationFilter
  </filter-class>
  <init-param>
    <param-name>casServerLoginUrl</param-name>
    <param-value>${cas.server.url}login</param-value>
  </init-param>
</filter>

<filter-mapping>
  <filter-name>CAS Authentication Filter</filter-name>
  <url-pattern>/Authn/RemoteUser</url-pattern>
</filter-mapping>

<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>
    org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter
  </filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>${cas.server.url}</param-value>
  </init-param>
  <init-param>
    <param-name>redirectAfterValidation</param-name>
    <param-value>true</param-value>
  </init-param>
</filter>

<filter-mapping>
  <filter-name>CAS Validation Filter</filter-name>
  <url-pattern>/Authn/RemoteUser</url-pattern>
</filter-mapping>

<filter>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <filter-class>
    org.apereo.cas.client.util.HttpServletRequestWrapperFilter
  </filter-class>
</filter>

<filter-mapping>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <url-pattern>/Authn/RemoteUser</url-pattern>
</filter-mapping>
```

Enable `RemoteUserHandler` in Idp Deployable `web.xml`. Ensure the following is defined:

```xml
<!-- Servlet protected by container user for RemoteUser authentication -->
<servlet>
  <servlet-name>RemoteUserAuthHandler</servlet-name>
  <servlet-class>edu.internet2.middleware.shibboleth.idp.authn.provider.RemoteUserAuthServlet</servlet-class>
</servlet>

<servlet-mapping>
  <servlet-name>RemoteUserAuthHandler</servlet-name>
  <url-pattern>/Authn/RemoteUser</url-pattern>
</servlet-mapping>
```


## SSO for Shibboleth IdP (External)
This is a Shibboleth IdP external authentication plugin that delegates 
the authentication to CAS. The advantage of using 
this component over the plain `RemoteUser` solution is the ability to 
utilize a full range of native CAS protocol features such as `renew` and `gateway`.

The plugin is available for both 
Shibboleth Identity Provider [v2](https://github.com/Unicon/shib-cas-authn2) 
and [v3](https://github.com/Unicon/shib-cas-authn3).

### Relying Party EntityId
The authentication plugin is able to pass the relying party's entity ID over 
to the CAS server upon authentication requests. 
The entity ID is passed in form of a url parameter to the CAS server as such:

```
https://sso.example.org/cas/login?service=<authentication-plugin-url>&entityId=<relying-party-entity-id>
```

## Displaying SAML MDUI
The CAS server is able to recognize the `entityId` parameter and display SAML MDUI on the login page,
that is provided by the metadata associated with the relying party. 
This means that CAS will also need to know
about metadata sources that the identity provider uses.

### Configuration

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-saml-mdui</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).


A sample screenshot of the above configuration in action:

![capture](https://cloud.githubusercontent.com/assets/1205228/8120071/095c7628-1050-11e5-810e-7bce128391df.PNG)

## Shibboleth Service Provider Proxy

The [CASShib project](https://code.google.com/p/casshib/) "Shibbolizes" the CAS 
server and enables end applications to get authentication 
information from CAS rather than the Shibboleth Service Provider. 
CASShib is designed as an alternative to deploying the 
Shibboleth service provider for each application in order to:

- Leverage Shibboleth's sophisticated attribute release policy functionality to 
enable attribute releasing to services in the local environment.
- Offer the chance for local applications to easily become federated services.
