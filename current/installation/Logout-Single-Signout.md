---
layout: default
title: CAS - Logout & Single Logout
---

<a name="LogoutAndSingleLogout(SLO)"> </a>
<a name="LogoutandSingleLogout(SLO)">  </a>
#Logout and Single Logout (SLO)

There are potentially many active application sessions during a CAS single sign-on session, and the distinction between
logout and single logout is based on the number of sessions that are ended upon a _logout_ operation. The scope of logout
is determined by where the action takes place:

1. Application logout - ends a single application session
2. CAS logout - ends the CAS SSO session

Note that the logout action in each case has no effect on the other in the simple case. Ending an application session
does not end the CAS session and ending the CAS session does not affect application sessions. This is a common cause of
confusion for new users and deployers of an SSO system.

The single logout support in CAS attempts to reconcile the dispartity between CAS logout and application logout. When
CAS is configured for SLO, it attempts to send logout messages to every application that requested authentication to
CAS during the SSO session. While this is a best-effort process, in many cases it works well and provides a consistent
user experience by creating symmetry between login and logout.

<a name="CASLogout">  </a>
##CAS Logout

Per the [CAS Protocol](../protocol/CAS-Protocol.html), the `/logout` endpoint is responsible for destroying the current SSO session. Upon logout, it may also be desirable to redirect back to a service. This is controlled via specifying the redirect link via the `service` parameter. 

The redirect behavior is turned off by default, and is activated via the following setting in `cas.properties`:

{% highlight bash %}
# Specify whether CAS should redirect to the specified service parameter on /logout requests
# cas.logout.followServiceRedirects=false
{% endhighlight %}

The specified url must be registered in the service registry of CAS and enabled.

<a name="SingleLogout(SLO)">  </a>
##Single Logout (SLO)
CAS is designed to support single sign out. Whenever a ticket-granting ticket is explicitly expired, the logout protocol will be initiated. Clients that do not support the logout protocol may notice extra requests in their access logs that appear not to do anything.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Single Logout is turned on by default.</p></div>

When a CAS session ends, it sends an HTTP POST message to each of the services that requested authentiation to CAS
during the SSO session. The message is delivered to the URL presented in the _service_ parameter of the original CAS
protocol ticket request. A sample SLO message:

{% highlight xml %}
<samlp:LogoutRequest
    xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
    xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
    ID="[RANDOM ID]"
    Version="2.0"
    IssueInstant="[CURRENT DATE/TIME]">
    <saml:NameID>@NOT_USED@</saml:NameID>
    <samlp:SessionIndex>[SESSION IDENTIFIER]</samlp:SessionIndex>
</samlp:LogoutRequest>
{% endhighlight %}

The session identifier is the CAS service ticket ID that was provided to the serivce when it originally authenticated
to CAS. The session identifier is used to correlate a CAS session with an application session; for example, the SLO
session identifier maps to a servlet session that can subsequently be destroyed to terminate the application session.

Logout protocol is effectively managed by the `LogoutManagerImpl` component:

{% highlight xml %}
<bean id="logoutManager" class="org.jasig.cas.logout.LogoutManagerImpl">
    <constructor-arg index="0" ref="servicesManager"/>
    <constructor-arg index="1" ref="noRedirectHttpClient"/>
    <property name="disableSingleSignOut" value="${slo.callbacks.disabled:false}" />         
</bean>
{% endhighlight %}

<a name="TurningOffSingleLogout">  </a>
###Turning Off Single Logout
To disable single logout, adjust the following setting in `cas.properties` file:

{% highlight bash %}
# To turn off all back channel SLO requests set slo.disabled to true
# slo.callbacks.disabled=false
{% endhighlight %}


<a name="TicketRegistryBehavior">  </a>
<a name="TicketRegistryCleanerBehavior">  </a>
###Ticket Registry Cleaner Behavior
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
3. For the collection of expired tickets, the cleaner will again ask them to "expire" which triggers the SLO callback to be issued.
4. The cleaner subsequently removes the TGT from the registry. Note that simply removing a ticket by itself from the registry does not issue the SLO callback. A ticket needs to be explicitly told one way or another, to "expire" itself:
    - If the ticket is already expired, the mechanism will issue the SLO callback.
    - If the ticket is not already expired, it will be marked as expired and the SLO callback will be issued.

<a name="WithoutTicketRegistryCleaner">  </a>
####Without `TicketRegistryCleaner`
1. Single Logout is turned on
2. Thereâ€™s no cleaner, so nothing runs in the background or otherwise to "expire" and delete tickets from the registry and thus, no SLO callbacks will be issued automatically. 
2. A logout request is received by CAS
3. CAS will locate the TGT and will attempt to destroy the SSO session.
4. In destroying the ticket, CAS will:
    - Ask the ticket to expire itself, which will issue SLO callbacks.
    - Delete the ticket from the registry

 
