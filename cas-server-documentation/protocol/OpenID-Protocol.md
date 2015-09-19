---
layout: default
title: CAS - OpenID Protocol
---

#OpenID Protocol
OpenID is an open, decentralized, free framework for user-centric digital identity. Users represent themselves using URIs. For more information see the [http://www.openid.net](http://www.openid.net).

CAS supports both the "dumb" and "smart" modes of the OpenID protocol. Dumb mode acts in a similar fashion to the existing CAS protocol. The smart mode differs in that it establishes an association between the client and the openId provider (OP) at the begining. Thanks to that association and the key exchange done during association, information exchanged between the client and the provider are signed and verified using this key. There is no need for the final request (which is equivalent in CAS protocol to the ticket validation).

OpenID identifiers are URIs. The default mechanism in CAS support is an uri ending with the actual user login (ie. `http://my.cas.server/openid/myusername` where the actual user login id is `myusername`). This is not recommended and you should think of a more elaborated way of providing URIs to your users.

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-openid</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

##Configuration

###Update the webflow

CAS uses a spring webflow to describe the authentication process. We need to change it to switch to OpenID authentication if it recognizes one. This is done in the `login-webflow.xml` file. After the on-start element just add these two blocks:

{% highlight xml %}
<!-- If the request contains a parameter called openid.mode and is not an association request, switch to openId. Otherwise, continue normal webflow. -->
<decision-state id="selectFirstAction">
    <if
       test="externalContext.requestParameterMap['openid.mode'] ne ''
        &amp;&amp; externalContext.requestParameterMap['openid.mode'] ne null
        &amp;&amp; externalContext.requestParameterMap['openid.mode'] ne 'associate'"
       then="openIdSingleSignOnAction" else="ticketGrantingTicketCheck" />
</decision-state>

<!-- The OpenID authentication action. If authentication is successful, send the ticket granting ticker. Otherwise, redirect to the login form. -->
<action-state id="openIdSingleSignOnAction">
    <evaluate expression="openIdSingleSignOnAction" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition on="error" to="viewLoginForm" />
    <transition on="warn" to="warn" />
</action-state>
{% endhighlight %}

###Add an argument extractor

We must tell cas how to extract the OpenID information from the authentication request (`openid.mode`, `openid.sig`, `openid.assoc_handle`, etc). This is done in the *`argumentExtractorsConfiguration.xml`* file, located in the *`spring-configuration`* directory. Add this bean into the file:

{% highlight xml %}
<bean id="openIdArgumentExtractor" class="org.jasig.cas.support.openid.web.support.OpenIdArgumentExtractor">
  <property name="openIdPrefixUrl" value="${server.prefix}/openid" />
</bean>

<util:list id="argumentExtractors">
   <ref bean="casArgumentExtractor" />
   <!-- The OpenId arguments extractor -->
   <ref bean="openIdArgumentExtractor" />
   <ref bean="samlArgumentExtractor" />
</util:list>
{% endhighlight %}


##OpenID v2.0 support

By default, the CAS server is defined as an OpenID provider v1.0. This definition is held in the `user.jsp` file (in the `WEB-INF/view/jsp/protocol/openid` directory):

{% highlight xml %}
<html>
<head>
    <link rel="openid.server" href="${openid_server}"/>
</head>
</html>
{% endhighlight %}

To define the CAS server as an OpenID provider v2.0, the exposed endpoint must be changed accordingly. To do that, the first thing is to replace the content of the `user.jsp` file by a new file pointing to the appropriate Yadis definition:

{% highlight xml %}
<html>
<head>
    <meta http-equiv="X-XRDS-Location" content="http://mycasserver/yadispath/yadis.xml" />
</head>
</html>
{% endhighlight %}

And to add this Yadis definition on some publicly accessible url (in the above example, it is `htp://mycasserver/yadispath/yadis.xml`) as follows:

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<xrds:XRDS xmlns:xrds="xri://$xrds" xmlns="xri://$xrd*($v*2.0)"
           xmlns:openid="http://openid.net/xmlns/1.0">
<XRD>
    <Service priority="1">
        <Type>http://specs.openid.net/auth/2.0/signon</Type>
        <URI>http://mycasserver/login</URI>
    </Service>
</XRD>
</xrds:XRDS>
{% endhighlight %}

This XML content defines the CAS server available on `http://mycasserver/login` (to be changed for your server) as an OpenID provider v2.0 because of the type of service (`http://specs.openid.net/auth/2.0/signon`).

***

# Delegate To an OpenID Provider

Using the OpenID protocol, the CAS server can also be configured to [delegate the authentication](../integration/Delegate-Authentication.html) to an OpenID provider.
