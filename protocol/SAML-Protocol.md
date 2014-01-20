---
layout: default
title: CAS - CAS SAML Protocol
---
#Overview
CAS has support for versions 1.1 and 2 of the SAML protocol to a specific extent. This document deals with CAS-specific concerns.

Support is enabled by including the following dependency in the Maven WAR overlay:

    <dependency>
      <groupId>org.jasig.cas</groupId>
      <artifactId>cas-server-support-saml</artifactId>
      <version>${cas.version}</version>
    </dependency>

#SAML 1.1
CAS supports the [standardized SAML 1.1 protocol](http://en.wikipedia.org/wiki/SAML_1.1) primarily to:

- Support a method of [attribute release](../integration/Attribute-Release.html)
- [Single Logout](../installation/Logout-Single-Signout.html)

A SAML 1.1 ticket validation response is obtained by validating a ticket via POST at the `/samlValidate URI`.

##Sample Request
{% highlight xml %}
POST /cas/samlValidate?ticket=
Host: cas.example.com
Content-Length: 491
Content-Type: text/xml
 
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Header/>
  <SOAP-ENV:Body>
    <samlp:Request xmlns:samlp="urn:oasis:names:tc:SAML:1.0:protocol" MajorVersion="1"
      MinorVersion="1" RequestID="_192.168.16.51.1024506224022"
      IssueInstant="2002-06-19T17:03:44.022Z">
      <samlp:AssertionArtifact>
        ST-1-u4hrm3td92cLxpCvrjylcas.example.com
      </samlp:AssertionArtifact>
    </samlp:Request>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
{% endhighlight %}

##Sample Response
{% highlight xml %}
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Header />
  <SOAP-ENV:Body>
    <Response xmlns="urn:oasis:names:tc:SAML:1.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:1.0:assertion"
    xmlns:samlp="urn:oasis:names:tc:SAML:1.0:protocol" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" IssueInstant="2008-12-10T14:12:14.817Z"
    MajorVersion="1" MinorVersion="1" Recipient="https://eiger.iad.vt.edu/dat/home.do"
    ResponseID="_5c94b5431c540365e5a70b2874b75996">
      <Status>
        <StatusCode Value="samlp:Success">
        </StatusCode>
      </Status>
      <Assertion xmlns="urn:oasis:names:tc:SAML:1.0:assertion" AssertionID="_e5c23ff7a3889e12fa01802a47331653"
      IssueInstant="2008-12-10T14:12:14.817Z" Issuer="localhost" MajorVersion="1"
      MinorVersion="1">
        <Conditions NotBefore="2008-12-10T14:12:14.817Z" NotOnOrAfter="2008-12-10T14:12:44.817Z">
          <AudienceRestrictionCondition>
            <Audience>
              https://some-service.example.com/app/
            </Audience>
          </AudienceRestrictionCondition>
        </Conditions>
        <AttributeStatement>
          <Subject>
            <NameIdentifier>johnq</NameIdentifier>
            <SubjectConfirmation>
              <ConfirmationMethod>
                urn:oasis:names:tc:SAML:1.0:cm:artifact
              </ConfirmationMethod>
            </SubjectConfirmation>
          </Subject>
          <Attribute AttributeName="uid" AttributeNamespace="http://www.ja-sig.org/products/cas/">
            <AttributeValue>12345</AttributeValue>
          </Attribute>
          <Attribute AttributeName="groupMembership" AttributeNamespace="http://www.ja-sig.org/products/cas/">
            <AttributeValue>
              uugid=middleware.staff,ou=Groups,dc=vt,dc=edu
            </AttributeValue>
          </Attribute>
          <Attribute AttributeName="eduPersonAffiliation" AttributeNamespace="http://www.ja-sig.org/products/cas/">
            <AttributeValue>staff</AttributeValue>
          </Attribute>
          <Attribute AttributeName="accountState" AttributeNamespace="http://www.ja-sig.org/products/cas/">
            <AttributeValue>ACTIVE</AttributeValue>
          </Attribute>
        </AttributeStatement>
        <AuthenticationStatement AuthenticationInstant="2008-12-10T14:12:14.741Z"
        AuthenticationMethod="urn:oasis:names:tc:SAML:1.0:am:password">
          <Subject>
            <NameIdentifier>johnq</NameIdentifier>
            <SubjectConfirmation>
              <ConfirmationMethod>
                urn:oasis:names:tc:SAML:1.0:cm:artifact
              </ConfirmationMethod>
            </SubjectConfirmation>
          </Subject>
        </AuthenticationStatement>
      </Assertion>
    </Response>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
{% endhighlight %}

##Configuration

In addition to the `cas-server-support-saml` module dependency, the following 5 steps are required to enabled the SAML 1.1 support.

###`Define the samlValidateController bean and map it to /samlValidate URL via handlerMappingC bean in cas-servlet.xml:`
{% highlight xml %}
<bean id="samlValidateController" class="org.jasig.cas.web.ServiceValidateController"
  p:validationSpecificationClass="org.jasig.cas.validation.Cas20WithoutProxyingValidationSpecification"
  p:centralAuthenticationService-ref="centralAuthenticationService"
  p:proxyHandler-ref="proxy20Handler"
  p:argumentExtractor-ref="samlArgumentExtractor"
  p:successView="casSamlServiceSuccessView"
  p:failureView="casSamlServiceFailureView"/>
{% endhighlight %}

{% highlight xml %}
<bean id="handlerMappingC" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
  <property name="mappings">
    <props>
      ...
      <prop key="/samlValidate">samlValidateController</prop>
      ...
{% endhighlight %}

###`Add the servlet mapping for /samlValidate URL in the web.xml file:`
{% highlight xml %}
<servlet-mapping>
  <servlet-name>cas</servlet-name>
  <url-pattern>/samlValidate</url-pattern>
</servlet-mapping>
{% endhighlight %}

###`Add the appropriate SAML arguments extractor in the argumentExtractorsConfiguration.xml file:`
{% highlight xml %}
<bean id="samlArgumentExtractor" class="org.jasig.cas.support.saml.web.support.SamlArgumentExtractor" />

<util:list id="argumentExtractors">
  <ref bean="casArgumentExtractor" />
  <ref bean="samlArgumentExtractor" />
</util:list>
{% endhighlight %}

###`Add the SAML ID generator in the uniqueIdGenerators.xml file:`
{% highlight xml %}
<bean id="samlServiceTicketUniqueIdGenerator" class="org.jasig.cas.support.saml.util.SamlCompliantUniqueTicketIdGenerator">
  <constructor-arg index="0" value="https://localhost:8443" />
</bean>

<util:map id="uniqueIdGeneratorsMap">
  <entry
    key="org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl"
    value-ref="serviceTicketUniqueIdGenerator" />
  <entry
    key="org.jasig.cas.support.saml.authentication.principal.SamlService"
    value-ref="samlServiceTicketUniqueIdGenerator" />
</util:map>
{% endhighlight %}

###`Add the SAML views in the cas-servlet.xml file:`
{% highlight xml %}
<bean id="viewResolver" class="org.springframework.web.servlet.view.ResourceBundleViewResolver" p:order="0">
  <property name="basenames">
    <list>
      <value>${cas.viewResolver.basename}</value>
      <value>protocol_views</value>
      <value>saml_views</value>
    </list>
  </property>
</bean>
{% endhighlight %}


#SAML 2 (Google Apps Integration)
Google Apps utilizes SAML 2 to provide [an integration point for external authentication services](https://developers.google.com/google-apps/sso/saml_reference_implementation). CAS includes an `ArgumentExtractor` and accompanying `Service` to provide process and understand SAML 2 requests from Google.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Though Google Accounts integration is enabled through the use of SAML 2 AuthenticationRequests and Assertions, it may not work with any SAML 2 compliant application. </p></div>

##Configuration

###Generate DSA/RSA Keys
The first step is to generate DSA/RSA public and private keys. These are used to sign and read the Assertions. After you've generated your keys, you will need to register the public key with Google. The keys will also need to be available to the CAS application but not publicly available over the Internet. It is recommended that you place the keys within the classpath (i.e. `WEB-INF/classes`) though any location accessible by the user running the web server instance and not served publicly to the Internet is acceptable. 

{% highlight bash %}
openssl genrsa -out private.key 1024
openssl rsa -pubout -in private.key -out public.key -inform PEM -outform DER
openssl pkcs8 -topk8 -inform PER -outform DER -nocrypt -in private.key -out private.p8
openssl req -new -x509 -key private.key -out x509.pem -days 365
{% endhighlight %}

The `public.key` and `private.p8` must be in the classpath. The `x509.pem` file should be uploaded into Google Apps.

###Configure CAS Server
Google Accounts integration within CAS is enabled by simply adding an additional `ArgumentExtractor` to the list of `ArgumentExtractors`. You'll need to modify the `WEB-INF/spring-configuration/argumentExtractorsConfiguration.xml`, and add the following:

{% highlight xml %}
<bean id="googleAccountsArgumentExtractor" 
        class="org.jasig.cas.web.support.GoogleAccountsArgumentExtractor"
      p:privateKey-ref="privateKeyFactoryBean"
      p:publicKey-ref="publicKeyFactoryBean"
      p:httpClient-ref="httpClient" />

<util:list id="argumentExtractors">
	<ref bean="casArgumentExtractor" />
	<ref bean="samlArgumentExtractor" />
	<ref bean="googleAccountsArgumentExtractor" />
</util:list>

<bean id="privateKeyFactoryBean" class="org.jasig.cas.util.PrivateKeyFactoryBean"
      p:location="classpath:private.p8"
      p:algorithm="RSA" />

<bean id="publicKeyFactoryBean"	class="org.jasig.cas.util.PublicKeyFactoryBean"
      p:location="classpath:public.key"
      p:algorithm="RSA" />
{% endhighlight %}

Replace the `public.key` and `private.key` with the names of your key files. If you are using DSA instead of RSA, change the algorithm as appropriate.

You'll also need to add a new generator in the `WEB-INF/spring-configuration/uniqueIdGenerators.xml` file:
{% highlight xml %}
<util:map id="uniqueIdGeneratorsMap">
  <entry
    key="org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl"
    value-ref="serviceTicketUniqueIdGenerator" />
  <entry
    key="org.jasig.cas.support.saml.authentication.principal.GoogleAccountsService"
    value-ref="serviceTicketUniqueIdGenerator" />
</util:map>
{% endhighlight %}


###Configure Google
You'll need to provide Google with the URL for your SAML-based SSO service, as well as the URL your users will be redirected to when they log out of a hosted Google application.

Use the following URLs when you are configuring for Google Apps

    Sign-in page URL: https://yourCasServer/login
    Sign-out page URL: https://yourCasServer/logout
    Change password URL: http://whateverServerYouWouldLike


###Register Google with CAS

    Name : Google Apps
    Service URL : https://www.google.com/a/YourGoogleDomain/acs

#Customizing the SAML Artifact
When constructing an instance of the `SamlCompliantUniqueTicketIdGenerator` available at `cas-server-webapp/WEB-INF/spring-configuration/uniqueIdGenerators.xml`, you may set the `saml2compliant` property to "true" in order to generate SAML2 artifacts. Otherwise SAML1 compliant artifacts are generated.
{% highlight xml %}
<bean id="samlServiceTicketUniqueIdGenerator" class="org.jasig.cas.util.SamlCompliantUniqueTicketIdGenerator">
    <constructor-arg index="0" value="https://localhost:8443" />
    <property name="saml2compliant" value="true" />
</bean>
{% endhighlight %}

