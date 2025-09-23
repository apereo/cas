---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email

CAS presents the ability to notify users on select actions via email messages. Example actions include notification 
of risky authentication attempts or password reset links/tokens, etc. Configuring 
an email provider (i.e. Amazon Simple Email Service ) is a matter of defining SMTP settings. Each particular feature 
in need of email functionality should be able to gracefully continue in case settings are not defined. 

Default support for email notifications is automatically 
enabled/included by the relevant modules using the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-core-notifications" %}

You need not explicitly include this module in WAR Overlay configurations, except 
when there is a need to access components and APIs at compile-time. 

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="health" healthIndicators="mailHealthIndicator" %}
            
## Email Message Body

The body of the email message that is defined in the CAS configuration can be 
constructed using the following strategies.
     
{% tabs emailmessagebody %}

{% tab emailmessagebody <i class="fa fa-envelope px-1"></i>Default %}

By default, the body of the email message that is defined in the CAS configuration is
formatted using special placeholders for variables that are marked as `${...}`. Such variables
are substituted at runtime with the appropriate values available to the feature that is being used.

{% endtab %}

{% tab emailmessagebody <i class="fa fa-file px-1"></i>Template File %}

The configuration setting for the email message body can also accept a path to an external file (i.e. `HTML`).
The contents of the file are processed for placeholder variables and values using the same default strategy.

The email template file can also be processed via `GStringTemplateEngine`, if the path ends
with the file extension `.gtemplate`. Input parameters are passed to the template which will
substitute variables and expressions into placeholders in a template source text to produce the desired output.

An example template, with variables and expressions such as `firstname` file would be:

```
Dear <%= firstname %> $lastname,
We <% if (accepted) print 'are pleased' else print 'regret' %> \
to inform you that your paper entitled
'$title' was ${ accepted ? 'accepted' : 'rejected' }.
```

Note that the template file can be automatically localized per the available `locale` parameter.
For example, if the template file is specified as `EmailTemplate.html`, and the available locale is `de` ,
CAS will automatically check for `EmailTemplate_de.html` first and will then fall back onto the default if the
localized template file is not found.

{% endtab %}

{% tab emailmessagebody <i class="fa fa-file-code px-1"></i>Groovy %}

The configuration setting for the email message body can also point to an external Groovy script
to build the contents of the message body dynamically. The script may be designed as:

```groovy
def run(Object[] args) {
    def values = (args[0] as Map).values()
    def logger = args[1]
    def locale = args.length == 3 ? args[2] : null
    
    logger.info("Parameters are {} with locale {}", args[0], locale)
    return String.format("%s, %s", values[0], values[1])
}
```

The following parameters are passed to the script:

| Parameter    | Description                                                                        |
|--------------|------------------------------------------------------------------------------------|
| `parameters` | `Map<String, ?>` of parameters passed by CAS, depending on feature and/or context. |
| `logger`     | The object responsible for issuing log messages such as `logger.info(...)`.        |
| `locale`     | The object representing the available `Locale`, if any and available.              |

The outcome of the script should be message body text.

Similarly, the email message body may be defined using an inline groovy script. In this case the configuration setting should be defined as:

```groovy
groovy { return 'This is your email message!' }
```

The script bindings prior to execution are prepared with a collection of parameters appropriate for that feature.   

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% endtabs %}
   
## Email Strategies

The following approaches may be used to send emails.

| Option          | Reference                                                    |
|-----------------|--------------------------------------------------------------|
| Default         | [See this page](Sending-Email-Configuration-Default.html).   |
| SendGrid        | [See this page](Sending-Email-Configuration-SendGrid.html).  |
| Microsoft Entra | [See this page](Sending-Email-Configuration-Azure-AD.html).  |
| Amazon SES      | [See this page](Sending-Email-Configuration-AmazonSES.html). |
| Mailjet         | [See this page](Sending-Email-Configuration-Mailjet.html).   |
| Custom          | [See this page](Sending-Email-Configuration-Custom.html).    |
