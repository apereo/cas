---
layout: default
title: CAS - Authentication Interrupt
---

# Authentication Interrupt

CAS has the ability to pause and interrupt the authentication flow to reach out to external services and resources, querying for status and setings that would then dicatate how CAS should manage and control the SSO session. Interrupt services are able to present notification messages to the user, provide options for redirects to external services, etc. A common use case of this functionality deals with presenting a bulletin board during the authentication flow to present messages and announcements to select users and then optionally require that audience to complete a certain task before CAS is able to honor the authentication request and establish a session.

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

| Field                  | Description
|------------------------|---------------------------------------------------------------------------------
| `message`              | Announcement message to display on the screen.
| `link`                 | A map of links to display on the screen where key is the link text and value is the destination.
| `interrupt`            | `true/false` to indicate whether CAS should interrupt the authentication flow.
| `block`                | `true/false` to indicate whether CAS should block the authentication flow altogether.
| `ssoEnabled`           | `true/false` to indicate whether CAS should permit the authentication but not establish SSO.
| `autoRedirect`         | `true/false` to indicate whether CAS should auto-redirect to the first provided link.
| `autoRedirectAfterSeconds` | Indicate whether CAS should auto-redirect after the configured number of seconds. The default is `-1`, meaning delayed redirect functionality should be execute.

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
    return new InterruptResponse("Message", [link1:"google.com", link2:"yahoo.com"], false, true)
}
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt-groovy).


### REST

This strategy reaches out to a REST endpoint resource whose job is to dynamically calculate whether the authentication flow should be interrupted given the provided `username` and `service` parameters. To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#authentication-interrupt-rest).

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