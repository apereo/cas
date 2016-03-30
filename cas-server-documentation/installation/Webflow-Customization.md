---
layout: default
title: CAS - Web Flow Customization
---


# Webflow Customization
CAS uses [Spring Web Flow](projects.spring.io/spring-webflow) to do "script" processing of login and logout protocols. 
Spring Web Flow builds on Spring MVC and allows implementing the "flows" of a web application. A flow encapsulates a sequence 
of steps that guide a user through the execution of some business task. It spans multiple HTTP requests, has state, deals with
 transactional data, is reusable, and may be dynamic and long-running in nature. Each flow may contain among many other settings the following major elements:

- Actions: components that describe an executable task and return back a result
- Transitions: Routing the flow from one state to another; Transitions may be global to the entire flow.
- Views: Components that describe the presentation layer displayed back to the client
- Decisions: Components that conditionally route to other areas of flow and can make logical decisions

Spring Web Flow presents CAS with a pluggable architecture where custom actions, views and decisions may be injected into the 
flow to account for additional use cases and processes. Note that to customize the weblow, one must possess a reasonable level
of understanding of the webflow's internals and injection policies. The intention of this document is not to describe Spring Web Flow, 
but merely to demonstrate how the framework is used by CAS to carry out various aspects of the protocol and business logic execution.

## Termination of Web Flow Sessions
CAS provides a facility for storing flow execution state on the client in Spring Webflow. Flow state is stored as an encoded byte 
stream in the flow execution identifier provided to the client when rendering a view. The following features are presented via this strategy:

- Support for conversation management (e.g. flow scope)
- Encryption of encoded flow state to prevent tampering by malicious clients

By default, the conversational state of Spring Webflow is managed inside the application session, which can time out due to inactivity 
and must be cleared upon the termination of flow. Rather than storing this state inside the session, CAS automatically attempts to store 
and keep track of this state on the client in an encrypted form to remove the need for session cleanup, termination and replication.

Default encryption strategy controlled via the `loginFlowStateTranscoder` component.
These settings can be controlled via the following defined in the `cas.properties` file:

{% highlight properties %}
# The encryption secret key. By default, must be a octet string of size 256.
# webflow.encryption.key=

# The signing secret key. By default, must be a octet string of size 512.
# webflow.signing.key=
{% endhighlight %}

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>
While the above settings are all optional, it is recommended that you provide your own configuration and settings for encrypting and 
transcoding of the web session state.</p></div>

## Required Service for Authentication
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

## Acceptable Usage Policy
CAS presents the ability to allow the user to accept the usage policy before moving on to the application. 
See [this guide](Webflow-Customization-AUP.html) for more info.
