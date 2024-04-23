---
layout: default
title: CAS - Webflow Customization
category: Webflow Management
---

{% include variables.html %}

# Webflow Customization

CAS uses [Spring Webflow](https://github.com/apereo/spring-webflow) to do *script* processing of login and logout protocols.
Spring Web Flow builds on Spring MVC and allows implementing the "flows" of a web application. A flow encapsulates a sequence
of steps that guide a user through the execution of some business task. It spans multiple HTTP requests, has state, deals with
 transactional data, is reusable, and may be dynamic and long-running in nature. Each 
flow may contain among many other settings the following major elements:

- Actions: components that describe an executable task and return back a result
- Transitions: Routing the flow from one state to another; Transitions may be global to the entire flow.
- Views: Components that describe the presentation layer displayed back to the client
- Decisions: Components that conditionally route to other areas of flow and can make logical decisions

Spring Web Flow presents CAS with a pluggable architecture where custom actions, views and decisions may be injected into the
flow to account for additional use cases and processes. Note that <strong>to customize the 
webflow, one must possess a reasonable level of understanding of the webflow internals 
and injection policies</strong>. The intention of this document is NOT to describe 
Spring Web Flow, but merely to demonstrate how the framework is used by CAS to 
carry out various aspects of the protocol and business logic execution.

## Webflow Session

See [this guide](Webflow-Customization-Sessions.html) for more info.

## Webflow Auto Configuration

See [this guide](Webflow-Customization-AutoConfiguration.html) for more info.

## Extending Webflow

If you want to learn how to modify and extend the CAS authentication flows, [please see this guide](Webflow-Customization-Extensions.html).

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="springWebflow" casModule="cas-server-support-reports" %}

## Webflow Decorations

Learn how to fetch and display data dynamically from external data sources and 
endpoints and pass those along to the webflow by [reviewing this guide](Webflow-Customization-Extensions.html).

## Single Sign-on & Services

See [this guide](Webflow-Customization-Services.html) for more info.

## Acceptable Usage Policy

CAS presents the ability to allow the user to accept the usage policy before moving on to the application.
See [this guide](Webflow-Customization-AUP.html) for more info.

## Customizing Errors

See [this guide](Webflow-Customization-Exceptions.html) for more info.

## Custom Settings

See [this guide](Webflow-Customization-CustomProperties.html) for more info.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.webflow" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```

