---
layout: default
title: CAS - Shibboleth Integration
---

#Overview
CAS can be integrated with the [Shibboleth federated SSO platform](http://shibboleth.net/) by a couple different strategies. It is possible to designate CAS to serve as the authentication provider for the Shibboleth IdP. With such a setup, when user is routed to the IdP, the following may take place:

- If the user has already authenticated to CAS and has a valid CAS SSO session, the IdP will transparently perform the requested action, e.g. attribute release.
- If the user does not have a valid CAS SSO session, the user will be redirected to CAS and must authenticate before the IDP proceeds with the requested action.


##SSO Provider for Shibboleth IdP (RemoteUser)


###Configuration



####Include CAS Client Libraries in IdP Deployable

Download the latest Java CAS Client Release and modify the IdP war deployable such that the following jars are included in the `./lib` installer folder, then redeploy the Idp with these files:

    cas-client-$VERSION/modules/cas-client-core-$VERSION.jar


####Modify `$SHIB_HOME/conf/handler.xml`

Define the `RemoteUser` authentication method to be used with CAS authentication.
{% highlight xml %}
<!-- Remote User handler for CAS support -->
<LoginHandler xsi:type="RemoteUser">
  <AuthenticationMethod>
    urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified
  </AuthenticationMethod>
  <AuthenticationMethod>
    urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
  </AuthenticationMethod>
</LoginHandler>
{% endhighlight %}


####Modify IdP Deployable `web.xml`
Add the following XML blocks to the `web.xml` file for the IdP war deployable. 
{% highlight xml %}
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
      org.jasig.cas.client.authentication.AuthenticationFilter
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
    org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter
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
    org.jasig.cas.client.util.HttpServletRequestWrapperFilter
  </filter-class>
</filter>
  
<filter-mapping>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <url-pattern>/Authn/RemoteUser</url-pattern>
</filter-mapping>
{% endhighlight %}


####Enable `RemoteUserHandler` in Idp Deployable `web.xml`
Ensure the following is defined:

{% highlight xml %}
<!-- Servlet protected by container user for RemoteUser authentication -->
<servlet>
  <servlet-name>RemoteUserAuthHandler</servlet-name>
  <servlet-class>edu.internet2.middleware.shibboleth.idp.authn.provider.RemoteUserAuthServlet</servlet-class>
</servlet>
  
<servlet-mapping>
  <servlet-name>RemoteUserAuthHandler</servlet-name>
  <url-pattern>/Authn/RemoteUser</url-pattern>
</servlet-mapping>
{% endhighlight %}


##SSO Provider for Shibboleth IdP (External)
This is a Shibboleth IdP external authentication plugin that delegates the authentication to CAS. The advantage of using this component over the plain `RemoteUser` solution is the ability to utilize a full range of native CAS protocol features such as `renew` and `gateway`. 

The plugin is available for both Shibboleth Identity Provider [v2](https://github.com/Unicon/shib-cas-authn2) and [v3](https://github.com/Unicon/shib-cas-authn3).

##Shibboleth Service Provider Proxy
The [CASShib project](https://code.google.com/p/casshib/) "Shibbolizes" the CAS server and enables end applications to get authentication information from CAS rather than the Shibboleth Service Provider. CASShib is designed as an alternative to deploying the Shibboleth service provider for each application in order to:

- Leverage Shibboleth's sophisticated attribute release policy functionality to enable attribute releasing to services in the local environment.
- Offer the chance for local applications to easily become federated services.
