---
layout: default
title: CAS - Web Flow Customization
---


#Webflow Customization
CAS uses [Spring Web Flow](projects.spring.io/spring-webflow) to do "script" processing of login and logout protocols. Spring Web Flow builds on Spring MVC and allows implementing the "flows" of a web application. A flow encapsulates a sequence of steps that guide a user through the execution of some business task. It spans multiple HTTP requests, has state, deals with transactional data, is reusable, and may be dynamic and long-running in nature. Each flow may contain among many other settings the following major elements:

- Actions: components that describe an executable task and return back a result
- Transitions: Routing the flow from one state to another; Transitions may be global to the entire flow.
- Views: Components that describe the presentation layer displayed back to the client
- Decisions: Components that conditionally route to other areas of flow and can make logical decisions

Spring Web Flow presents CAS with a pluggable architecture where custom actions, views and decisions may be injected into the flow to account for additional use cases and processes. Note that to customize the weblow, one must possess a reasonable level of understanding of the webflow's internals and injection policies. The intention of this document is not to describe Spring Web Flow, but merely to demonstrate how the framework is used by CAS to carry out various aspects of the protocol and business logic execution.


##Login Flow
The flow in CAS is given a unique id that is registered inside a `flowRegistry` component. Support is enabled via the following configuration snippets in `cas-servlet.xml`: 


###Components
{% highlight xml %}

<webflow:flow-executor id="loginFlowExecutor" flow-registry="loginFlowRegistry">
	<webflow:flow-execution-attributes>
	  <webflow:always-redirect-on-pause value="false" />
	  <webflow:redirect-in-same-state value="false" />
	</webflow:flow-execution-attributes>
	<webflow:flow-execution-listeners>
	  <webflow:listener ref="terminateWebSessionListener" />
	</webflow:flow-execution-listeners>
</webflow:flow-executor>

...

<webflow:flow-registry id="loginFlowRegistry" flow-builder-services="builder">
    <webflow:flow-location path="/WEB-INF/login-webflow.xml" id="login" />
</webflow:flow-registry>

{% endhighlight %}

###login-flow.xml Overview
The login flow is at a high level composed of the following phases:

- Initialization of the flow
- Validation of Ticket Granting Ticket (TGT) 
- Validation of requesting service and ensuring that it is authorized to use CAS
- Generation of the Login Ticket (LT)
- Presentation of the Login Form
- Generation of TGT upon successful authentication
- Generation of Service Ticket (ST) for the requesting service
- Issuing a redirect back to the authenticating web service

A high-level diagram detailing major states in the flow is presented here:

![](http://i.imgur.com/SBDUGbH.png)

Acceptance of user credentials and invoking the authentication handler components is carried out by:

{% highlight xml %}
<bean id="authenticationViaFormAction" class="org.jasig.cas.web.flow.AuthenticationViaFormAction"
        p:centralAuthenticationService-ref="centralAuthenticationService"
        p:warnCookieGenerator-ref="warnCookieGenerator"/>

{% endhighlight %}

Handling authentication failures, mapping the result of which event to a new state is carried out by:

{% highlight xml %}
<bean id="authenticationExceptionHandler" class="org.jasig.cas.web.flow.AuthenticationExceptionHandler" />

...

<action-state id="realSubmit">
	<evaluate expression="authenticationViaFormAction.submit(flowRequestContext, flowScope.credential, messageContext)" />

  	<transition on="success" to="sendTicketGrantingTicket" />
    <transition on="authenticationFailure" to="handleAuthenticationFailure" />
    <transition on="error" to="generateLoginTicket" />
</action-state>

....
<action-state id="handleAuthenticationFailure">
    <evaluate expression="authenticationExceptionHandler.handle(currentEvent.attributes.error, messageContext)" />
    <transition on="AccountDisabledException" to="casAccountDisabledView"/>
    <transition on="AccountLockedException" to="casAccountLockedView"/>
    <transition on="CredentialExpiredException" to="casExpiredPassView"/>
    <transition on="InvalidLoginLocationException" to="casBadWorkstationView"/>
    <transition on="InvalidLoginTimeException" to="casBadHoursView"/>
    <transition on="FailedLoginException" to="generateLoginTicket"/>
    <transition on="AccountNotFoundException" to="generateLoginTicket"/>
    <transition on="UNKNOWN" to="generateLoginTicket"/>
</action-state>
{% endhighlight %}

Certain error conditions are also classified as global transitions, particularly in cases of unauthorized services attempting to use CAS:

{% highlight xml %}
<global-transitions>
    <transition to="viewLoginForm" on-exception="org.jasig.cas.services.UnauthorizedSsoServiceException"/>
    <transition to="viewServiceErrorView" on-exception="org.springframework.webflow.execution.repository.NoSuchFlowExecutionException" />
    <transition to="viewServiceErrorView" on-exception="org.jasig.cas.services.UnauthorizedServiceException" />
</global-transitions>
{% endhighlight %}


##Logout Flow
The flow in CAS is given a unique id that is registered inside a `flowRegistry` component. Support is enabled via the following configuration snippets in `cas-servlet.xml`: 

###Components
{% highlight xml %}
<webflow:flow-executor id="logoutFlowExecutor" flow-registry="logoutFlowRegistry">
    <webflow:flow-execution-attributes>
      <webflow:always-redirect-on-pause value="false" />
      <webflow:redirect-in-same-state value="false" />
    </webflow:flow-execution-attributes>
    <webflow:flow-execution-listeners>
      <webflow:listener ref="terminateWebSessionListener" />
    </webflow:flow-execution-listeners>
</webflow:flow-executor>

...

<webflow:flow-registry id="logoutFlowRegistry" flow-builder-services="builder">
	<webflow:flow-location path="/WEB-INF/logout-webflow.xml" id="logout" />
</webflow:flow-registry>
{% endhighlight %}

###logout-flow.xml Overview
The logout flow is at a high level composed of the following phases:

- Termination of the SSO session and destruction of the TGT
- Initiating the Logout protocol 
- Handling various methods of logout (front-channel, back-channel, etc)

The Logout protocol is initiated by the following component:

{% highlight xml %}
<bean id="logoutAction" class="org.jasig.cas.web.flow.LogoutAction"
        p:servicesManager-ref="servicesManager"
        p:followServiceRedirects="${cas.logout.followServiceRedirects:false}"/>

{% endhighlight %}

Front-channel method of logout is specifically handled by the following component:
{% highlight xml %}
<bean id="frontChannelLogoutAction" class="org.jasig.cas.web.flow.FrontChannelLogoutAction"
        c:logoutManager-ref="logoutManager"/>

{% endhighlight %}



##Termination of Web Flow Sessions
Each flow is registered with a `TerminateWebSessionListener` whose job is expire the web session as soon as the webflow is ended. The goal is to decrease memory consumption by deleting as soon as possible the web sessions created mainly for login and logout processes. By default, the webflow session is configured to be destroyed in 2 seconds, once the session has ended.


##Extending the Webflow
The CAS webflow provides discrete points to inject new functionality. Thus, the only thing to modify is the flow definition where new beans and views can be added easily with the Maven overlay build method.


###Adding Actions
Adding Spring Web Flow actions typically involves the following steps:

- Adding a SWF-specific Spring bean type that extends `org.springframework.webflow.action.AbstractAction`
- Access to the flow scope object also accessible in the flow definition file
- `doExecute()` method contains business logic and returns `success()` or `error()` Event types
- Returned Event types are evaluated in the flow definition file

Once the action bean is configured, you may define it inside the `login-webflow.xml`:

{% highlight xml %}
<action-state id="actionStateId">
	<action bean="customActionBeanId" />
	<transition on="success" to="doThis" />
	<transition on="error" to="doThat" />
</action-state>
{% endhighlight %}


###Adding Views
Adding Spring Web Flow views involves the following steps:

- The name of the view directly referenced from the flow definition file
- The name of the view must match the view file name

A sample is presented here:

{% highlight bash %}
### View for password update
passwordUpdateView.(class)=org.springframework.web.servlet.view.JstlView
passwordUpdateView.url=/WEB-INF/view/jsp/default/ui/passwordUpdateView.jsp

{% endhighlight %}

