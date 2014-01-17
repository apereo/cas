---
layout: default
title: CAS - Logout & Single Logout
---

<a name="Logout">  </a>
#Logout

<a name="Overview">  </a>
##Overview
Per the [CAS Protocol](../protocol/CAS-Protocol.html), the `/logout` endpoint is responsible for destroying the current SSO session. Upon logout, it may also be desirable to redirect back to a service. This is controlled via specifying the redirect link via the `service` parameter. 

<a name="Configuration">  </a>
###Configuration
The redirect behavior is turned off by default, and is activated via the following setting in `cas.properties`:

{% highlight bash %}
# Specify whether CAS should redirect to the specified service parameter on /logout requests
# cas.logout.followServiceRedirects=false
{% endhighlight %}

The specified url must be registered in the service registry of CAS and enabled.

<a name="SingleLogout(SLO)">  </a>
#Single Logout (SLO)
CAS is designed to support single sign out. Whenever a Ticket Granting Ticket is explicitly expired, the logout protocol will be initiated. Clients that do not support the logout protocol may notice extra requests in their access logs that appear not to do anything.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Single Logout is turned on by default.</p></div>

<a name="Overview">  </a>
##Overview
When a CAS session ends, it will callback to each of the services (using their _original_ url) that are registered with the system and send a POST request with the following:

{% highlight xml %}
<samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="[RANDOM ID]" Version="2.0" IssueInstant="[CURRENT DATE/TIME]">
    <saml:NameID>@NOT_USED@</saml:NameID>
    <samlp:SessionIndex>[SESSION IDENTIFIER]</samlp:SessionIndex>
</samlp:LogoutRequest>
{% endhighlight %}

The session identifier is the same as the CAS Service Ticket which may be sufficiently long to be secure. The session identifier should map back to a session which can be terminated (i.e. deleted from a database, expired, etc.)

<a name="Components">  </a>
##Components
Logout protocol is effectively managed by the `LogoutManagerImpl` component:

{% highlight xml %}
<bean id="logoutManager" class="org.jasig.cas.logout.LogoutManagerImpl">
    <constructor-arg index="0" ref="servicesManager"/>
    <constructor-arg index="1" ref="noRedirectHttpClient"/>
    <property name="disableSingleSignOut" value="${slo.callbacks.disabled:false}" />         
</bean>
{% endhighlight %}

<a name="TurningOffSingleLogout">  </a>
##Turning Off Single Logout
To disable single logout, adjust the following setting in `cas.properties` file:

{% highlight bash %}
# To turn off all back channel SLO requests set slo.disabled to true
# slo.callbacks.disabled=false
{% endhighlight %}


<a name="TicketRegistryBehavior">  </a>
###Ticket Registry Behavior
Furthermore, the default behavior is to issue single sign out callbacks in response to a logout request or when a TGT is expired via expiration policy when a `TicketRegistryCleaner` runs.  If you are using ticket registry cleaner and you want to enable the single sign out callback only when CAS receives a logout request, you can configure your `TicketRegistryCleaner` as such:

{% highlight xml %}
<bean id="ticketRegistryCleaner" class="org.jasig.cas.ticket.registry.support.DefaultTicketRegistryCleaner"
      p:ticketRegistry-ref="ticketRegistry"
      p:lock-ref="cleanerLock"
      p:logUserOutOfServices="${slo.callbacks.disabled:false}" />
{% endhighlight %}

Note that certain ticket registries don't use or need a registry cleaner. For such registries, the option of having a ticker registry cleaner is entirely done away with and is currently not implemented. With that being absent, you will no longer receive automatic SLO callbacks upon TGT expiration. As such, the only thing that would reach back to the should then be explicit logout requests per the CAS protocol.

<a name="WithTicketRegistryCleaner">  </a>
####With `TicketRegistryCleaner`
1. Single Logout is turned on
2. The cleaner runs to detect the ticket that are automatically expired. It will query the tickets in the ticket registry, and will accumulate those that are expired. 
3. For the collection of expired tickets, the cleaner will again ask them to “expire” which triggers the SLO callback to be issued.
4. The cleaner subsequently removes the TGT from the registry. Note that simply removing a ticket by itself from the registry does not issue the SLO callback. A ticket needs to be explicitly told one way or another, to “expire” itself:
    - If the ticket is already expired, the mechanism will issue the SLO callback.
    - If the ticket is not already expired, it will be marked as expired and the SLO callback will be issued.

<a name="WithoutTicketRegistryCleaner">  </a>
####Without `TicketRegistryCleaner`
1. Single Logout is turned on
2. There’s no cleaner, so nothing runs in the background or otherwise to “expire” and delete tickets from the registry and thus, no SLO callbacks will be issued automatically. 
2. A logout request is received by CAS
3. CAS will locate the TGT and will attempt to destroy the SSO session.
4. In destroying the ticket, CAS will:
    - Ask the ticket to expire itself, which will issue SLO callbacks.
    - Delete the ticket from the registry

 
