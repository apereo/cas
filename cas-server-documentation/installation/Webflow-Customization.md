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

##Termination of Web Flow Sessions
CAS provides a facility for storing flow execution state on the client in Spring Webflow. Flow state is stored as an encoded byte stream in the flow execution identifier provided to the client when rendering a view. The following features are presented via this strategy:

- Support for conversation management (e.g. flow scope)
- Encryption of encoded flow state to prevent tampering by malicious clients

By default, the conversational state of Spring Webflow is managed inside the application session, which can time out due to inactivity and must be cleared upon the termination of flow. Rather than storing this state inside the session, CAS automatically attempts to store and keep track of this state on the client in an encrypted form to remove the need for session cleanup, termination and replication.

Default encryption strategy controlled via the `loginFlowStateTranscoder` component is using the 128-bit AES in CBC ciphering mode with compression turned on. These settings can be controlled via the following settings defined in the `cas.properties` file:

{% highlight properties %}
# cas.webflow.cipher.alg=AES
# cas.webflow.cipher.mode=CBC
# cas.webflow.cipher.padding=PKCS7
# cas.webflow.keystore=classpath:/etc/keystore.jceks
# cas.webflow.keystore.type=JCEKS
# cas.webflow.keystore.password=changeit
# cas.webflow.keyalias=aes128
# cas.webflow.keypassword=changeit
{% endhighlight %}

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>
While the above settings are all optional, it is recommended that you provide your own configuration and settings for encrypting and transcoding of the web session state.</p></div>

##Required Service for Authentication Flow
By default, CAS will present a generic success page if the initial authentication request does not identify
the target application. In some cases, the ability to login to CAS without logging
in to a particular service may be considered a misfeature because in practice, too few users and institutions
are prepared to understand, brand, and support what is at best a fringe use case of logging in to CAS for the
sake of establishing an SSO session without logging in to any CAS-reliant service.

As such, CAS optionally allows adopters to not bother to prompt for credentials when no target application is presented
and instead presents a message when users visit CAS directly without specifying a service.

This behavior is controlled via `cas.properties`:

{% highlight properties %}
# Indicates whether an SSO session can be created if no service is present.
# create.sso.missing.service=false
{% endhighlight %}

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

##Acceptable Usage Policy Flow
CAS presents the ability to allow the user to accept the usage policy before moving on to the application. The task of remembering the user's choice is kept in memory by default and will be lost upon container restarts and/or in clustered deployments. Production-level deployments of this feature would require modifications to the flow such that the retrieval and/or acceptance of the policy would be handled via an external storage mechanism such as LDAP or JDBC.  

###Configuration

####Enable Webflow

- In the `login-webflow.xml` file, enable the transition to `acceptableUsagePolicyCheck` by uncommenting the following entry:

{% highlight xml %}
<transition on="success" to="acceptableUsagePolicyCheck" />
{% endhighlight %}

- Enable the actual flow components by uncommenting the following entries:

{% highlight xml %}
<!-- Enable AUP flow
<action-state id="acceptableUsagePolicyCheck">
    <evaluate expression="acceptableUsagePolicyFormAction.verify(flowRequestContext, flowScope.credential, messageContext)" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition to="acceptableUsagePolicyView" />
</action-state>
...

-->
{% endhighlight %}

- Customize the policy by modifying `casAcceptableUsagePolicyView.jsp` located at `src/main/webapp/WEB-INF/view/jsp/default/ui`.

####Configure Storage

The task of remembering and accepting the policy is handled by `AcceptableUsagePolicyFormAction`. Adopters may extend this class to retrieve and persistent the user's choice via an external backend mechanism such as LDAP or JDBC.

{% highlight xml %}
<bean id="acceptableUsagePolicyFormAction"
      class="org.jasig.cas.web.flow.AcceptableUsagePolicyFormAction"/>
{% endhighlight %}
