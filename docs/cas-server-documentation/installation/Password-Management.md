---
layout: default
title: CAS - Password Management
---

# Password Management

If authentication fails due to a rejected password policy, CAS is able to intercept
that request and allow the user to update the account password in place. The password management features of CAS are rather modest, and alternatively should the functionality provide inadequate for your policy, you may always redirect CAS to use a separate and standalone application that is fully in charge of managing the account password and associated flows.

CAS may also allow users to reset their passwords voluntarily. Those who have forgotten their account password
may receive a secure link with a time-based expiration policy at their registered email address and/or phone. The link
will allow the user to provide answers to his/her pre-defined security questions, which if successfully done,
will allow the user to next reset their password and login again. To learn more about available notification options, please [see this guide](SMS-Messaging-Configuration.html)
or [this guide](Sending-Email-Configuration.html).

You may also specify a pattern for accepted passwords. 

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#password-management).

## JSON

Accounts and password may be stored inside a static modest JSON resource, whose location is taught to CAS via settings.
This option is most useful during development, testing and demos and is not suitable for production.

The outline of the JSON file may match the following:

```json
{
  "casuser" : {
    "email" : "casuser@example.org",
    "password" : "p@ssw0rd",
    "securityQuestions" : {
      "question1" : "answer1",
      "question2" : "answer2"
    }
  }
}
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#json-password-management).

## LDAP

The updated password may be stored inside an LDAP server.

LDAP support is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-pm-ldap</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#ldap-password-management).

## JDBC

The updated password may be stored inside a database.

JDBC support is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-pm-jdbc</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#jdbc-password-management).

## REST

Tasks such as locating user's email and security questions as well as management
and updating of the password are delegated to user-defined rest endpoints.

REST support is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-pm-rest</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#rest-password-management).

| Endpoint                  | Method    | Headers             | Expected Response
|---------------------------|-----------|------------------------------------------------------------------------
| Get Email Address         | `GET`     | `username`          | `200`. Email address in the body.
| Get Security Questions    | `GET`     | `username`          | `200`. Security questions map in the body.
| Update Password           | `POST`    | `username`, `password`, `oldPassword` | `200`. `true/false` in the body.

## Custom

You may also inject your own implementation for password management into CAS that would itself handle account updates and retrievals.
In order to do this, you will need to design a configuration class that roughly matches the following: 

```java
package org.apereo.cas.pm;

@Configuration("MyPasswordConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyPasswordConfiguration {

    @Bean
    public PasswordManagementService passwordChangeService() {
        ...
    }
}
```

[See this guide](Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.