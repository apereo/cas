---
layout: default
title: CAS - CAS Clients
---

# Overview

A CAS client is also a software package that can be integrated with various software platforms and applications in order to 
communicate with the CAS server using or or more supported protocols. CAS clients supporting a number of software platforms and products have been developed.


## Official Clients

* [.NET CAS Client](https://github.com/apereo/dotnet-cas-client)
* [Java CAS Client](https://github.com/apereo/java-cas-client)
* [PHP CAS Client](https://github.com/Jasig/phpCAS)
* [Apache CAS Client](https://github.com/Jasig/mod_auth_cas)


## Other Clients

Other unofficial or incubating CAS clients may be [found here](https://wiki.jasig.org/display/CASC).
Given the above projects are unofficial and not under direct maintenance of CAS,
their availability and accuracy may vary.

## Samples

- [CASified Python web application using Flask](https://github.com/cas-projects/cas-sample-python-webapp)
- [CASified Java web application using Java CAS Client](https://github.com/cas-projects/cas-sample-java-webapp)
- [Go CLI for CAS admin endpoints](https://github.com/cas-projects/casctl)
- [Smoke Testing CAS HA Deployments](https://github.com/cas-projects/duct)
- [CASified Bootiful Java web application](https://github.com/cas-projects/bootiful-cas-client)

## Framework Support

The following programming frameworks have built-in support for CAS:

* [Spring Security](http://static.springsource.org/spring-security/site/)
* [Apache Shiro](http://shiro.apache.org/cas.html)
* [Pac4j](https://github.com/pac4j/pac4j)


## Build your own CAS client

As a lot of CAS clients already exist, there is little opportunity to develop a CAS client and it should be avoided as much as possible. Indeed, creating your own client is not an easy job and you're most likely to generate security breaches.

Though, if you really need to create your own CAS client, please be aware of these incomplete guidelines:

* Rely on a static internal configuration instead of leveraging the behaviour on received inputs which can be forged
* Ensure that all outside inputs are properly decoded and encoded when used calls to CAS or other services
* Ensure that input is validated and that overly large inputs are discarded.

