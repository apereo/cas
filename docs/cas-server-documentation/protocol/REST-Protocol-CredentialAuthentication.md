---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Credential Authentication - REST Protocol

Similar to asking for ticket-granting tickets, this endpoint allows 
one to only verify the validity of provided credentials as they are extracted from the request body:

```bash
POST /cas/v1/users HTTP/1.0

username=battags&password=password
```

You may also specify a `service` parameter to verify whether the authenticated 
user may be allowed to access the given service. While the above example 
shows `username` and `password` as the provided credentials, you are 
practically allowed to provide multiple sets and different types of 
credentials provided CAS is equipped to extract and recognize those 
from the request body.

A successful response will produce a `200 OK` status code along with 
a JSON representation of the authentication result, which may include 
the authentication object, authenticated principal along with 
any captured attributes and/or metadata fetched for the authenticated user.
   
## Customizations

If you wish to control the REST authentication policy to determine which attempts are allowed to proceed,
you may define the following bean definition in your environment:

```java
@AutoConfiguration
public class MyConfiguration {
    @Bean
    public AuthenticationPolicy restAuthenticationPolicy() {
        return new MyAuthenticationPolicy();
    }
}
```
                                         
A more comprehensive option would be to take full control of the REST 
authentication attempt via the following bean definition:

```java
@AutoConfiguration
public class MyConfiguration {
    @Bean
    public RestAuthenticationService restAuthenticationService() {
        return new MyRestAuthenticationService();
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
