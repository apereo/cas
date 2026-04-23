---
layout: default
title: CAS - Account Registration
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration

CAS provides a modest workflow to handle self-service account registration. Once enabled, the
account registration workflow allows users to register accounts with CAS to:
                                                              
- Provide an initial, customizable set of details such as first name, last name, email to kickstart the account creation request.
- Receive an activation link with instructions via email or text message to verify the account creation request.
- Finalize the account creation request, choose a password, security questions, etc.
- Submit the account registration request to an identity manager system for provisioning and follow-up processes.
- Establish a single sign-on session for the user and redirect them to the original application, if any.

<div class="alert alert-info">:information_source: <strong>Usage Note</strong><p>CAS is <strong>NOT</strong>, as 
of this writing, an identity management solution and does not intend to provide 
features or support capabilities that are typically found in such
systems, such as complex provisioning workflows, account lifecycle management, 
inbound/outbound attribute mappings, etc. While all open-source software can be customized to no end, 
the capabilities described here ultimately expect one or more systems 
of record to hold and manage user accounts.</p></div>

Account registration and sign-up functionality is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-account-mgmt" %}

## Configuration

{% include_cached casproperties.html properties="cas.account-registration.core" %}
  
## Account Registration Requests

[See this guide](Account-Registration-Requests.html) to learn more about the account registration request process
and how to customize it.

### Account Registration Validation

The validation of registration requests can be customized using the following component:

```java
@Bean
public AccountRegistrationRequestValidator accountMgmtRegistrationRequestValidator() {
    return new MyAccountRegistrationRequestValidator();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.

## Communication Strategy

[See this guide](Account-Registration-Communication.html) to learn more.
   
## Username Extraction
      
By default, registration requests allow the user to choose a username, Construction and 
extraction of a `usename` field from the registration request can be customized using the following component:

```java
@Bean
public AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder() {
    return new MyAccountRegistrationUsernameBuilder();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
