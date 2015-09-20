---
layout: default
title: CAS - ADFS Integration
---

#Overview
The integration between the CAS Server and ADFS delegates user authentication from CAS Server to ADFS, making CAS Server a WS-Federation client. Claims released from ADFS are made available as attributes to CAS Server, and by extension CAS Clients.

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-wsfederation</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

##Components

### WsFed Configuration

Adjust and provide settings for the ADFS instance, and make sure you have obtained the ADFS signing certificate
and made it available to CAS at a location that can be resolved at runtime. 

{% highlight properties %}
# The claim from ADFS that should be used as the user's identifier.
# cas.wsfed.idp.idattribute=upn
#
# Federation Service identifier
cas.wsfed.idp.id=
#
# The ADFS login url.
cas.wsfed.idp.url=
#
# Identifies resource(s) that point to ADFS's signing certificates.
# These are used verify the WS Federation token that is returned by ADFS.
# Multiple certificates may be separated by comma.
cas.wsfed.idp.signingcerts=classpath:adfs-signing.crt
#
# Unique identifier that will be set in the ADFS configuration.
# cas.wsfed.rp.id=urn:cas:localhost
#
# Slack dealing with time-drift between the ADFS Server and the CAS Server.
# cas.wsfed.idp.tolerance=10000
{% endhighlight %}


### Modifying ADFS Claims
The WsFed configuration optionally may allow you to manipulate claims coming from ADFS but before they are inserted into the CAS user principal. For this to happen, you need
to put together an implementation of `WsFederationAttributeMutator` that changes and manipulates ADFS claims:

{% highlight java %}
package org.jasig.cas.support.wsfederation;

public class WsFederationAttributeMutatorImpl implements WsFederationAttributeMutator {
    public void modifyAttributes(final Map<String, Object> attributes) {
        ...
    }
}
{% endhighlight %}

The mutator then needs to be declared in your configuration:

{% highlight xml %}
<bean id="wsfedAttributeMutator" 
	class="org.jasig.cas.support.wsfederation.WsFederationAttributeMutatorImpl" />
{% endhighlight %} 


### Webflow Changes

The login Spring Webflow needs to adjusted to route to ADFS.

This snippet should be added right after the `on-start` element. This checks to see if we have a returning WS-Federation 
request and handles it as necessary, otherwise we route through the normal flows.

{% highlight xml %}
<action-state id="wsFederationAction">
  <evaluate expression="wsFederationAction" />
  <transition on="success" to="sendTicketGrantingTicket" />
  <transition on="error" to="ticketGrantingTicketCheck" />
</action-state>
<view-state id="WsFederationRedirect" view="externalRedirect:#{flowScope.WsFederationIdentityProviderUrl}"/>
{% endhighlight %} 


Next, we need to redirect to ADFS instead of displaying the CAS Login view. 
This accomplished by adjusting the `generateLoginTicket` state. This is a modified snippet:

{% highlight xml %}
<action-state id="generateLoginTicket">
    <evaluate expression="generateLoginTicketAction.generate(flowRequestContext)" />
 	<!--Redirect to ADFS instead of showing the login form -->
    <transition on="generated" to="WsFederationRedirect" />
</action-state>
{% endhighlight %} 

Finally, ensure that the attributes send from ADFS are available and mapped in
your `attributeRepository` configuration.


### Handling CAS Logout

An optional step, it is recommended that the `casLogoutView.jsp` be replace to redirect to ADFS's logout page.

{% highlight jsp %}
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:redirect url="https://adfs.example.org/adfs/ls/?wa=wsignout1.0"/>
{% endhighlight %} 