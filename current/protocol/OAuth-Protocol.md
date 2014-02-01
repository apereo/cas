---
layout: default
title: CAS - OAuth Protocol
---
<a name="Overview">  </a>
#Overview
You can configure the CAS server with OAuth server support, which means that you will be able to communicate with your CAS server through the [OAuth 2.0 protocol](http://oauth.net/2/), using the *Authorization Code* grant type.

Three new urls will be available:

* **/oauth2.0/authorize**  
It's the url to call to authorize the user: the CAS login page will be displayed and the user will authenticate. After successful authentication, the user will be redirected to the OAuth *callback url* with a code. Input GET parameters required: *client_id* and *redirect_uri*.

* **/oauth2.0/accessToken**  
It's the url to call to exchange the code for an access token. Input GET parameters required: *client_id*, *redirect_uri*, *client_secret* and *code*.

* **/oauth2.0/profile**  
It's the url to call to get the profile of the authorized user. Input GET parameter required: *access_token*. The response is in JSON format with all attributes of the user.

Support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-oauth</artifactId>
      <version>${cas.version}</version>
    </dependency>


<a name="Configuration">  </a>
#Configuration

<a name="AddtheOAuth20WrapperController">  </a>
##Add the OAuth20WrapperController

To add the `OAuth20WrapperController`, you need to add the mapping between the /oauth2.0/* url and the CAS servlet in the *web.xml* file:

{% highlight xml %}
<servlet-mapping>
  <servlet-name>cas</servlet-name>
  <url-pattern>/oauth2.0/*</url-pattern>
</servlet-mapping>
{% endhighlight %}

You have to create the controller itself in the *cas-servlet.xml* file:

{% highlight xml %}
<bean
  id="oauth20WrapperController"
  class="org.jasig.cas.support.oauth.web.OAuth20WrapperController"
  p:loginUrl="http://mycasserverwithoauthwrapper/login"
  p:servicesManager-ref="servicesManager"
  p:ticketRegistry-ref="ticketRegistry"
  p:timeout="7200" />
{% endhighlight %}

The *loginUrl* is the login url of the CAS server. The timeout is the lifetime of a CAS ticket granting ticket (in seconds, not in milliseconds!) with its mapping in the `handlerMappingC` bean (*cas-servlet.xml* file):

{% highlight xml %}
<bean id="handlerMappingC" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
  <property name="mappings">
    <props>
      <prop key="/serviceValidate">serviceValidateController</prop>
 
   ...
 
      <prop key="/statistics">statisticsController</prop>
      <prop key="/oauth2.0/*">oauth20WrapperController</prop>
    </props>
  </property>
  <property name="alwaysUseFullPath" value="true" />
</bean>
{% endhighlight %}

<a name="AddtheneededCASservices">  </a>
##Add the needed CAS services

<a name="CallbackAuthorization">  </a>
###Callback Authorization

One service is needed to make the OAuth wrapper works in CAS. It defines the callback url after CAS authentication to return to the OAuth wrapper as a CAS service.  
**Note**: the callback url must end with "callbackAuthorize".

{% highlight xml %}
<bean id="serviceRegistryDao" class="org.jasig.cas.services.InMemoryServiceRegistryDaoImpl">
  <property name="registeredServices">
    <list>
      <!-- A dedicated component to recognize OAuth Callback Authorization requests -->
      <bean class="org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService">
        <property name="id" value="0" />
        <property name="name" value="HTTP" />
        <property name="description" value="oauth wrapper callback url" />
        <!-- By default, only support regex patterns if/when needed -->
        <property name="serviceId" value="${server.prefix}/oauth2.0/callbackAuthorize" />
      </bean>
...
{% endhighlight %}

<a name="OAuthClients">  </a>
###OAuth Clients

Every OAuth client must be defined as a CAS service (notice the new *clientId* and *clientSecret* properties, specific to OAuth):

{% highlight xml %}
<bean id="serviceRegistryDao" class="org.jasig.cas.services.InMemoryServiceRegistryDaoImpl">
  <property name="registeredServices">
    <list>
       
      <bean class="org.jasig.cas.support.oauth.services.OAuthRegisteredService">
        <property name="id" value="1" />
        <property name="name" value="serviceName" />
        <property name="description" value="Service Description" />
        <!-- Supports regex patterns by default for service ids -->
        <property name="serviceId" value="oauth client service url" />
        <property name="clientId" value="client id goes here" />
        <property name="clientSecret" value="client secret goes here" />
      </bean>
...
{% endhighlight %}

***

<a name="OrdelegatetheauthenticationtoanOAuthprovider">  </a>
#Or delegate the authentication to an OAuth provider

Using the OAuth protocol, the CAS server can also be configured to [delegate the authentication](../integration/Delegate-Authentication.html) to an OAuth provider (like Facebook, Twitter, Google, Yahoo...)

