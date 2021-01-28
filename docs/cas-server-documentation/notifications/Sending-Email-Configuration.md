---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email

CAS presents the ability to notify users on select actions via email messages. Example actions include notification 
of risky authentication attempts or password reset links/tokens, etc. Configuring 
an email provider (i.e. Amazon Simple Email Service )
is a matter of defining SMTP settings. Each particular feature 
in need of email functionality should be able to 
gracefully continue in case settings are not defined. 

Default support for email notifications is automatically 
enabled/included by the relevant modules using the following module:

{% include casmodule.html group="org.apereo.cas" module="cas-server-core-notifications" %}

You need not explicitly include this module in WAR Overlay configurations, except 
when there is a need to access components and APIs at compile-time. 

{% include {{ version }}/email-notifications-configuration.md %}
            
## Email Message Body
                
By default, the body of the email message that is defined in the CAS configuration is 
formatted using special placeholders for variables that are marked as `%s`. The
formatting of the message closely follows the semantics of JDK's `String.format()`.
Arguments referenced by the format specifiers in the format string are passed by CAS depending on the context or feature. 
If there are more arguments than format specifiers, the extra arguments are ignored.

The configuration setting for the email message body can also accept a path to an external file (i.e. `HTML`).
The contents of the file are processed for placeholder variables and values using the same default strategy.


The configuration setting for the email message body can also point to an external Groovy script 
to build the contents of the message bod dynamically. The script may be designed as:

```groovy
def run(Object[] args) {
    def values = (args[0] as Map).values()
    def logger = args[1]
    
    logger.info("Parameters are {}", args[0])
    return String.format("%s, %s", values[0], values[1]);
}
```

The following parameters are passed to the script:

| Parameter        | Description
|------------------|--------------------------------------------------------------------------------------------
| `parameters`       | `Map<String, ?>` of parameters passed by CAS, depending on feature and/or context.
| `logger`           | The object responsible for issuing log messages such as `logger.info(...)`.

The outcome of the script should be message body text.
