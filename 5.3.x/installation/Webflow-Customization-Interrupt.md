---
layout: default
title: CAS - Authentication Interrupt
---

# Authentication Interrupt

CAS has the ability to pause and interrupt the authentication flow to reach out to external services and resources, querying for status and settings that 
would then dictate how CAS should manage and control the SSO session. Interrupt services are able to present notification messages to the user, provide options for redirects to external services, etc. A common use case deals with presenting a *bulletin board* during the authentication flow to present messages and announcements to select users and then optionally require the audience to complete a certain task before CAS is able to honor the authentication request and establish a session.

In the interrupt flow, CAS is not at the moment reaching back to an external resource acting as an interrupt service to store, track or remember a user's decision. In other words, we are only dealing with the `R` (ie. Read) in `CRUD`. Today's functionality only deals with inquiring status and reading results solely in read-only mode. Interrupt services are themselves required and encouraged to redirect the audience to external resources where execution of an action resets the interrupt status thereby freeing CAS to proceed forward later on without having to interrupt the authentication flow again.  

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-interrupt-webflow</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt).

## Interrupt Payload

Each interrupt strategy is ultimately tasked to produce a response that contains the following settings:

| Field                      | Description
|----------------------------|---------------------------------------------------------------------------------
| `message`                  | Announcement message to display on the screen.
| `links`                     | A map of links to display on the screen where key is the link text and value is the destination.
| `interrupt`                | `true/false` to indicate whether CAS should interrupt the authentication flow.
| `block`                    | `true/false` to indicate whether CAS should block the authentication flow altogether.
| `ssoEnabled`               | `true/false` to indicate whether CAS should permit the authentication but not establish SSO.
| `autoRedirect`             | `true/false` to indicate whether CAS should auto-redirect to the first provided link.
| `autoRedirectAfterSeconds` | Indicate whether CAS should auto-redirect after the configured number of seconds. The default is `-1`, meaning delayed redirect functionality should not be executed.

## Interrupt Strategies

Interrupt queries can be executed via the following ways:

### JSON

This strategy reaches out to a static JSON resource that contains a map of usernames linked to various interrupt policies.
This option is most useful during development, testing and demos.

```json
{
  "casuser" : {
    "message" : "Announcement message <strong>goes here</strong>.",
    "links" : {
      "Go to Location1" : "https://www.location1.com",
      "Go to Location2" : "https://www.location2.com"
    },
    "block" : false,
    "ssoEnabled" : false,
    "interrupt" : true,
    "autoRedirect" : false,
    "autoRedirectAfterSeconds" : -1
  }
}
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt-json).

### Regex Attribute

This strategy allows one to define regular expression patterns in CAS settings that would be matched against attributes names and values.
If a successful match is produced while CAS examines the collection of both authentication and principal attributes, the authentication flow
would be interrupted.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt-regex-attributes).

### Groovy

This strategy reaches out to a Groovy resource whose job is to dynamically calculate whether the authentication flow should be interrupted given the provided username and certain number of other parameters.

The script may be defined as:

```groovy
import org.apereo.cas.interrupt.InterruptResponse

def run(final Object... args) {
    def uid = args[0]
    def attributes = args[1]
    def service = args[2]
    def logger = args[3]

    ...
    def block = false
    def ssoEnabled = true

    return new InterruptResponse("Message", [link1:"google.com", link2:"yahoo.com"], block, ssoEnabled)
}
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt-groovy).

The following parameters are passed to the script:

| Parameter             | Description
|------------------------------------------------------------------------------------------------------------------------
| `uid`                 | Authenticated principal id.
| `attributes`          | A map of type `Map<String, Objec>` that contains both principal and authentication attributes. 
| `service`             | The identifier (i.e. URL) of the requesting application.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.

### REST

This strategy reaches out to a REST endpoint resource whose job is to dynamically calculate whether the authentication flow should be interrupted given the following parameters:

| Parameter             | Description
|-------------------------------------------------------------------------------------------------------
| `username`            | Authenticated principal id.
| `service`             | The identifier (URL) for the requesting application.
| `registeredService`   | The identifier of the registered service matched and found in the registry. 

On a successful operation with a status code of `200`, the response body is expected to contain the JSON payload whose syntax and structure is identical to what is described above.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt-rest).

### Custom

If you wish to design your own interrupt strategy to make inquiries, you can design your component to make determinations:

```java
package org.apereo.cas.support.interrupt;

@Configuration("myInterruptConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyInterruptConfiguration {

    @Bean
    public InterruptInquirer interruptInquirer() {
      ...
    }

}
```

[See this guide](Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Skipping Interrupts

Interrupt notifications may be disabled on a per-service basis. A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "skipInterrupt" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "true" ] ]
    }
  }
}
```
