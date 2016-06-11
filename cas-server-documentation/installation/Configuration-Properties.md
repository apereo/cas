---
layout: default
title: CAS - Properties
---

# CAS Properties

Various properties can be specified inside CAS either inside configuration files or as command 
line switches. This section provides a list common CAS properties and 
references to the underlying modules that consume them.

This section is meant as a guide only. Do not copy/paste the entire content into your CAS configuration; 
rather pick only the properties that you need.


## Spring Boot & Cloud

The following list of properties are controlled and provided to 
CAS by [Spring Boot](https://github.com/spring-projects/spring-boot):

```properties
##
# CAS Server Context Configuration
#
server.context-path=/cas
server.port=8443
server.ssl.key-store=file:/etc/cas/thekeystore
server.ssl.key-store-password=changeit
server.ssl.key-password=changeit
server.tomcat.basedir=build/tomcat
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=%t %a "%r" %s (%D ms)
server.tomcat.max-http-header-size=20971520
server.use-forward-headers=true

spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

##
# CAS Cloud Amqp Bus Configuration
#
spring.cloud.bus.enabled=false
spring.cloud.bus.refresh.enabled=true
spring.cloud.bus.env.enabled=true
spring.cloud.bus.destination=CasCloudBus
spring.cloud.bus.ack.enabled=true
# spring.activemq.broker-url=
# spring.activemq.in-memory=
# spring.activemq.pooled=
# spring.activemq.user=
# spring.activemq.password=

##
# CAS Admin Status Endpoints
#
endpoints.enabled=true
endpoints.sensitive=true
management.context-path=/status
endpoints.restart.enabled=false
endpoints.shutdown.enabled=false

##
# CAS Web Application Session Configuration
#
server.session.timeout=300
server.session.cookie.http-only=true
server.session.tracking-modes=COOKIE

##
# CAS Thymeleaf View Configuration
#
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false

##
# CAS Log4j Configuration
#
# logging.config=file:/etc/cas/log4j2.xml
server.context-parameters.isLog4jAutoInitializationDisabled=true

##
# CAS AspectJ Configuration
#
spring.aop.auto=true
spring.aop.proxy-target-class=true


##
# CAS Groovy Shell Console
#
# shell.command-refresh-interval=15
# shell.command-path-patterns=classpath*:/commands/**
# shell.auth.simple.user.name=
# shell.auth.simple.user.password=
# shell.ssh.enabled=true
# shell.ssh.port=2000
# shell.telnet.enabled=false
# shell.telnet.port=5000
# shell.ssh.auth-timeout=3000
# shell.ssh.idle-timeout=30000
```

## CAS Server

The following list of properties are defined and controlled by the CAS project:

```properties
##
# CAS Components Mapping
#
attributeRepository=stubAttributeRepository
primaryAuthenticationHandler=acceptUsersAuthenticationHandler
primaryPrincipalResolver=personDirectoryPrincipalResolver
themeResolver=serviceThemeResolver
serviceRegistryDao=jsonServiceRegistryDao
ticketRegistry=defaultTicketRegistry
ticketCipherExecutor=noOpCipherExecutor
grantingTicketExpirationPolicy=ticketGrantingTicketExpirationPolicy
authenticationPolicy=anyAuthenticationPolicy
authenticationPolicyFactory=acceptAnyAuthenticationPolicyFactory
authenticationThrottle=neverThrottle
restAuthenticationThrottle=neverThrottle
cookieCipherExecutor=tgcCipherExecutor
cookieValueManager=defaultCookieValueManager
principalFactory=defaultPrincipalFactory
authenticationTransactionManager=defaultAuthenticationTransactionManager
principalElectionStrategy=defaultPrincipalElectionStrategy
auditTrailManager=slf4jAuditTrailManager

##
# CAS Authentication Attributes
#
cas.attrs.resolve.uid=uid
cas.attrs.resolve.displayName=displayNameN
cas.attrs.resolve.cn=commonName
cas.attrs.resolve.affiliation=groupMembership
```


