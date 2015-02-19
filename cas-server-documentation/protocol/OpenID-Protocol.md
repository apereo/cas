---
layout: default
title: CAS - OpenID Protocol
---

#OpenID Protocol
OpenID is an open, decentralized, free framework for user-centric digital identity. Users represent themselves using URIs. For more information see the http://www.openid.net.

CAS supports both the "dumb" and "smart" modes of the OpenID protocol. Dumb mode acts in a similar fashion to the existing CAS protocol. The smart mode differs in that it establishes an association between the client and the openId provider (OP) at the begining. Thanks to that association and the key exchange done during association, information exchanged between the client and the provider are signed and verified using this key. There is no need for the final request (which is equivalent in CAS protocol to the ticket validation).

OpenID identifiers are URIs. The default mechanism in CAS support is an uri ending with the actual user login (ie. http://my.cas.server/openid/*myusername* where the actual user login is *myusername*). This is not recommended and you should think of a more elaborated way of providing URIs to your users.

Support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-openid</artifactId>
      <version>${cas.version}</version>
    </dependency>


##Configuration


###Declare the OpenID endpoint

The OpenID discovery endpoint should be enabled during the configuration process. In the *web.xml* file, the following mapping must be added:

{% highlight xml %}
<servlet-mapping>
  <servlet-name>cas</servlet-name>
  <url-pattern>/openid/*</url-pattern>
</servlet-mapping>
{% endhighlight %}

In the *cas-servlet.xml* file, the following mapping and bean must be also added:

{% highlight xml %}
<bean id="handlerMappingC" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
  <property name="mappings">
    <props>
      <prop key="/logout">logoutController</prop>
      ...
      <prop key="/openid/*">openIdProviderController</prop>
      ... 


<bean
    id="openIdProviderController"
    class="org.jasig.cas.support.openid.web.OpenIdProviderController"
    p:loginUrl="${server.prefix}/login"/>
{% endhighlight %}


###Add the OpenID entry in the unique id generator map

The OpenID entry should be added to the *uniqueIdGenerators.xml* file:

{% highlight xml %}
<util:map id="uniqueIdGeneratorsMap">
  ...
  <entry
    key="org.jasig.cas.support.openid.authentication.principal.OpenIdService"
    value-ref="serviceTicketUniqueIdGenerator" />
</util:map>
{% endhighlight %}


###Update the webflow

CAS uses a spring webflow to describe the the authentication process. We need to change it a little bit to allow CAS to switch to OpenID authentication if it recognizes one. This is done in the *login-webflow.xml* file. After the on-start element just add these two blocks:

{% highlight xml %}
<!-- If the request contains a parameter called openid.mode and is not an association request, switch to openId. Otherwise, continue normal webflow. -->
<decision-state id="selectFirstAction">
    <if
       test="externalContext.requestParameterMap['openid.mode'] neq ''
        &amp;&amp; externalContext.requestParameterMap['openid.mode'] neq null
        &amp;&amp; externalContext.requestParameterMap['openid.mode'] neq 'associate'"
       then="openIdSingleSignOnAction" else="ticketGrantingTicketExistsCheck" />
</decision-state>
       
<!-- The OpenID authentication action. If authentication is successful, send the ticket granting ticker. Otherwise, redirect to the login form. -->
<action-state id="openIdSingleSignOnAction">
    <evaluate expression="openIdSingleSignOnAction" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition on="error" to="viewLoginForm" />
    <transition on="warn" to="warn" />
</action-state>
{% endhighlight %}

The openIdSingleSignOnAction is itself defined in the *cas-servlet.xml* file:

{% highlight xml %}
<bean id="openIdSingleSignOnAction" class="org.jasig.cas.support.openid.web.flow.OpenIdSingleSignOnAction"
      p:centralAuthenticationService-ref="centralAuthenticationService"/>
{% endhighlight %}


###Enable OpenID in the AuthenticationManager

The authentication manager is the place where authentication takes place. We must provide it two elements needed for a successful OpenId authentication. The first thing to do is to detect the user name from the OpenID identifier. When your CAS server will work as an OP, users will authenticate with an OpenID identifier, looking like this : http://localhost:8080/cas/openid/*myusername*. Actually, in your users database, this users login is probably myusername. We must provide the CAS server with a way to extract the user principal from the credentials he provides us. This is the first thing we'll do in this section: add an OpenIdCredentialsToPrincipalResolver to the authentication manager. The next thing to give CAS is a specialized authentication handler.  
Open the *deployerConfigContext.xml* file, and locate the authenticationManager bean definition. It has two properties containing beans. In the credentials to principal property, add this bean definition:

{% highlight xml %}
<!-- The openid credentials to principal resolver -->
<bean class="org.jasig.cas.support.openid.authentication.principal.OpenIdCredentialsToPrincipalResolver" />
{% endhighlight %}

Then, in the authentication handler property, add this bean definition:

{% highlight xml %}
<!-- The open id authentication handler -->
<bean class="org.jasig.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler" p:ticketRegistry-ref="ticketRegistry" />
{% endhighlight %}


###Adapt the Spring CAS servlet configuration

We now have to make CAS handle nicely the OpenID request he will be presented with. First, we'll add a handler for the /login url, when called to validate a ticket (CAS is implementing the dumb OpenID mode, which means it does not create an association at the beginning of the authentication process. It must then check the received authentication success notification, which is done by one extra HTTP request at the end of the process). Anywhere in the *cas-servlet.xml* file, add this bean definition:

{% highlight xml %}
<bean id="handlerMappingOpendId"
      class="org.jasig.cas.support.openid.web.support.OpenIdPostUrlHandlerMapping">
    <!-- Notice we set the order value to 2, which is the order of the flow handler mapping. We'll fix that just next.
    The OpenIDPostUrlHandlerMapping MUST be called before the login webflow action is called, otherwise we will never be able to validate the authentication success. -->
    <property name="order" value="2"/>
    <property name="mappings">
        <props>
            <prop key="/login">delegatingController</prop>
        </props>
    </property>
</bean>
{% endhighlight %}

As we gave the order of 2 to the OpenIdPostUrlHandlerMapping, we must modify the FlowHandlerMapping objects orders to give it an incremented value. The loginFlowHandlerMapping order is incremented from 2 to 3:

{% highlight xml %}
<bean id="loginFlowHandlerMapping" class="org.springframework.webflow.mvc.servlet.FlowHandlerMapping"
   p:flowRegistry-ref="loginFlowRegistry" p:order="3">
   <property name="interceptors">
      <ref local="localeChangeInterceptor" />
   </property>
</bean>
{% endhighlight %}

And the logoutFlowHandlerMapping order is moved from 3 to 4:

{% highlight xml %}
<bean id="logoutFlowHandlerMapping" class="org.springframework.webflow.mvc.servlet.FlowHandlerMapping"
   p:flowRegistry-ref="logoutFlowRegistry" p:order="4">
   <property name="interceptors">
      <ref local="localeChangeInterceptor" />
   </property>
</bean>
{% endhighlight %}

In the handlerMappingOpenId, we referenced a bean called delegatingController. this bean is a special controller, using the Delegate pattern, which delegates the processing of a request to the first controller of its delegates which says it can handle it. So now we'll provide two delegate controllers. The first one is handling the Smart OpenId association, and the second process the authentication and ticket validation. Add this two beans in the file.
The Smart OpenId controller:

{% highlight xml %}
<bean id="smartOpenIdAssociationController" class="org.jasig.cas.support.openid.web.mvc.SmartOpenIdController"
     p:serverManager-ref="serverManager"
     p:successView="casOpenIdAssociationSuccessView" p:failureView="casOpenIdAssociationFailureView" />
{% endhighlight %}

The OpenID validation controller:

{% highlight xml %}
<bean id="openIdValidateController" class="org.jasig.cas.web.ServiceValidateController"
       p:validationSpecificationClass="org.jasig.cas.validation.Cas20WithoutProxyingValidationSpecification"
       p:centralAuthenticationService-ref="centralAuthenticationService"
       p:proxyHandler-ref="proxy20Handler" p:argumentExtractor-ref="openIdArgumentExtractor"
       p:successView="casOpenIdServiceSuccessView" p:failureView="casOpenIdServiceFailureView" />
{% endhighlight %}

We are done with the delegates. Now we must create the Delegating controller itself, and give it a list of delegates referencing the two delegates we just defined. So add this definition:

{% highlight xml %}
<bean id="delegatingController" class="org.jasig.cas.web.DelegatingController"
  p:delegates-ref="delegateControllers"/>
 
<util:list id="delegateControllers">
  <ref bean="smartOpenIdAssociationController"/>
  <ref bean="openIdValidateController"/>
</util:list>
{% endhighlight %}

Don't forget to include the *util* namespace if you don't have it already!


###Add an argument extractor

We must tell cas how to extract the OpenID information from the authentication request (openid.mode, openid.sig, openid.assoc_handle...). This is done in the *argumentExtractorsConfiguration.xml* file, located in the *spring-configuration* directory. Add this bean into the file:

{% highlight xml %}
<bean id="openIdArgumentExtractor" class="org.jasig.cas.support.openid.web.support.OpenIdArgumentExtractor" />
 
<util:list id="argumentExtractors">
   <ref bean="casArgumentExtractor" />
   <!-- The OpenId arguments extractor -->
   <ref bean="openIdArgumentExtractor" />
   <ref bean="samlArgumentExtractor" />
</util:list>
{% endhighlight %}


###Add the server manager

Next we must provide a ServerManager, which is a class from the openid4java library, which allows us to handle the Diffie-Hellman algorithm used by the association process. In the *spring-configuration/applicationContext.xml* file, add this bean definition:

{% highlight xml %}
<bean id="serverManager" class="org.openid4java.server.ServerManager"
   p:oPEndpointUrl="{cas.securityContext.casProcessingFilterEntryPoint.loginUrl}"
   p:enforceRpId="false" />
{% endhighlight %}

And finally, we need an applicationContext provider in the *spring-configuration/applicationContext.xml* file again:

{% highlight xml %}
<bean id="applicationContextProvider" class="org.jasig.cas.util.ApplicationContextProvider" />
{% endhighlight %}

***


#Or delegate the authentication to an OpenID provider

Using the OpenID protocol, the CAS server can also be configured to [delegate the authentication](../integration/Delegate-Authentication.html) to an OpenID provider.

