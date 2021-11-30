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
- Ultimately, submit the account registration request to an identity manager system for provisioning and follow-up processes.

<div class="alert alert-info"><strong>Usage Note</strong><p>CAS is <strong>NOT</strong>, as 
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

The account registration request expects a number of user inputs initially to kick off the registration process.
For starters, a default set of fields and inputs are expected by CAS out of the box, and as always, such details 
and fields can be described in *metadata* form using a JSON document that matches the following map:

```json
{
  "@class" : "java.util.HashMap",
  "field-name" : {
    "@class" : "org.apereo.cas.acct.AccountRegistrationProperty",
    "name" : "field-name",
    "required" : true,
    "label" : "cas.screen.acct.label.field",
    "title" : "cas.screen.acct.title.field",
    "pattern": ".+",
    "type": "email",
    "values" : [ "java.util.ArrayList", [ "sample@gmail.com", "sample2@hotmail.com" ] ],
    "order": 0
  }
}
```
    
The following fields are supported:

| Field      | Description                                                                                  |
|------------|----------------------------------------------------------------------------------------------|
| `name`     | The name of the input field to display on the registration screen.                           |
| `required` | Whether or not this input is required. Defaults to `false`.                                  |
| `label`    | Key to a message key in the CAS language bundles to describe the label text for this input.  |
| `title`    | Key to a message key in the CAS language bundles to describe the title text for this input.  |
| `pattern`  | Regular expression pattern to force and validate the acceptable pattern for the input value. |
| `type`     | The type of this input field (i.e. `select`, `email`, `phone`, `text`, etc.).                |
| `order`    | The display order of this input on the screen.                                               |
| `values`   | List of values to display in order, when type is set to `select`.                            |

<div class="alert alert-info"><strong>Is it possible to...?</strong><p>You must be wondering 
by now whether it's possible to customize the screen and include other types of fields, forms and values. 
In general, you should be able to use JSON metadata to describe additional fields so long as the input field's
type is simple enough and supported. If you have a type that isn't supported by the existing 
metadata, you will need to build the input field and workflows and rules linked to it yourself as custom code.</p></div>

The loading and processing of the user registration metadata and fields can be customized using the following component:

```java
@Bean
public AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader() {
    return new MyAccountRegistrationPropertyLoader(resource);
}
```

## Communication Strategy
             
Account creation requests are expected to be verified using a dedicated activation link that 
can be shared with the user using mail or text messages. The activation link is expected 
to remain valid for a configurable period of time.

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html), or [this guide](../notifications/Notifications-Configuration.html).

{% include_cached casproperties.html properties="cas.account-registration.mail,cas.account-registration.sms" %}
   
## Username Validations
      
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
