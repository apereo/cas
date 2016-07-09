---
layout: default
title: CAS Properties
---

## CAS PropertiesJ

Various properties can be specified in CAS [either inside configuration files or as command 
line switches](Configuration-Management.md). This section provides a list common CAS properties and 
references to the underlying modules that consume them.

<div class="alert alert-info"><strong>Be Selective</strong><p>
This section is meant as a guide only. Do <strong>NOT</strong> copy/paste the entire collection of settings 
into your CAS configuration; rather pick only the properties that you need.</p></div>

 Note that property names can be specified
in very relaxed terms. For instance `cas.someProperty`, `cas.some-property`, `cas.some_property`
and `CAS_SOME_PROPERTY` are all valid names.

The following list of properties are controlled by and provided to CAS.

### Embedded Tomcat

The following properties are related to the embedded Tomcat container that ships with CAS. 

```properties
server.contextPath=/cas
server.port=8443
server.ssl.keyStore=file:/etc/cas/thekeystore
server.ssl.keyStorePassword=changeit
server.ssl.keyPassword=changeit
# server.ssl.ciphers=
# server.ssl.clientAuth=
# server.ssl.enabled=
# server.ssl.keyAlias=
# server.ssl.keyStoreProvider=
# server.ssl.keyStoreType=
# server.ssl.protocol=
# server.ssl.trustStore=
# server.ssl.trustStorePassword=
# server.ssl.trustStoreProvider=
# server.ssl.trustStoreType=

server.tomcat.basedir=build/tomcat
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=%t %a "%r" %s (%D ms)
server.tomcat.accesslog.suffix=.log
server.tomcat.maxHttpHeaderSize=20971520
server.tomcat.maxThreads=5
server.tomcat.portHeader=X-Forwarded-Port
server.tomcat.protocolHeader=X-Forwarded-Proto
server.tomcat.protocolHeaderHttpsValue=https
server.tomcat.remoteIpHeader=X-FORWARDED-FOR
server.tomcat.uriEncoding=UTF-8

server.useForwardHeaders=true
```

### Embedded Tomcat HTTP/AJP

Enable HTTP/AJP connections for the embedded Tomcat container.

```properties
# cas.server.http.port=8080
# cas.server.http.protocol=org.apache.coyote.http11.Http11NioProtocol
# cas.server.http.enabled=true
# cas.server.connectionTimeout=20000

# cas.server.ajp.secure=false
# cas.server.ajp.enabled=false
# cas.server.ajp.proxyPort=-1
# cas.server.ajp.protocol=AJP/1.3
# cas.server.ajp.asyncTimeout=5000
# cas.server.ajp.scheme=http
# cas.server.ajp.maxPostSize=20971520
# cas.server.ajp.port=8009
# cas.server.ajp.enableLookups=false
# cas.server.ajp.redirectPort=-1
# cas.server.ajp.allowTrace=false
```

### CAS Server

Identify the CAS server. `name` and `prefix` are always required settings. 

```properties
# cas.server.name=https://cas.example.org:8443
# cas.server.prefix=https://cas.example.org:8443/cas
```


### Cloud Config AMQP Bus

CAS uses the Spring Cloud Bus to manage configuration in a distributed deployment. Spring Cloud Bus links nodes of a 
distributed system with a lightweight message broker

```properties
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
```

### Admin Status Endpoints

The following properties describe access controls and settings to the `/status`
endpoint of CAS which provide administrative functionality and oversight into the CAS software.

```properties
# cas.securityContext.adminpages.ip=.+
# endpoints.enabled=true
# endpoints.sensitive=true
# management.contextPath=/status
# endpoints.restart.enabled=false
# endpoints.shutdown.enabled=false

# cas.adminPagesSecurity.adminRoles=ROLE_ADMIN
# cas.adminPagesSecurity.ip=127\.0\.0\.1
# cas.adminPagesSecurity.loginUrl=
# cas.adminPagesSecurity.service=
# cas.adminPagesSecurity.users=
```

### Web Application Session

Control the web application session behavior.

```properties
server.session.timeout=300
server.session.cookie.httpOnly=true
server.session.trackingModes=COOKIE
```

### Thymeleaf View Management

```properties
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false
```

### Logging

Control the location and other settings of the CAS logging configuration. 

```properties
# logging.config=file:/etc/cas/log4j2.xml
server.contextParameters.isLog4jAutoInitializationDisabled=true
```

### AspectJ Configuration

```properties
# spring.aop.auto=true
# spring.aop.proxyTargetClass=true
```

### Authentication Attributes

Set of authentication attributes that are retrieved by the principal resolution process,
unless noted otherwise by the specific authentication scheme. 

```properties
# cas.authn.attributes.uid=uid
# cas.authn.attributes.displayName=displayName
# cas.authn.attributes.cn=commonName
# cas.authn.attributes.affiliation=groupMembership
```

### Authentication Policy

Global authentication policy that is applied when
CAS attempts to vend and validate tickets. 

```properties
# cas.authn.policy.requiredHandlerAuthenticationPolicyEnabled=false
```

### CAS Groovy Shell Console

```properties
# shell.commandRefreshInterval=15
# shell.commandPathPatterns=classpath*:/commands/**
# shell.auth.simple.user.name=
# shell.auth.simple.user.password=
# shell.ssh.enabled=true
# shell.ssh.port=2000
# shell.telnet.enabled=false
# shell.telnet.port=5000
# shell.ssh.authTimeout=3000
# shell.ssh.idleTimeout=30000
```


### Saml Metadata UI

```properties
# cas.samlMetadataUi.requireValidMetadata=true
# cas.samlMetadataUi.repeatInterval=120000
# cas.samlMetadataUi.startDelay=30000
# cas.samlMetadataUi.resources=classpath:/sp-metadata::classpath:/pub.key,http://md.incommon.org/InCommon/InCommon-metadata.xml::classpath:/inc-md-pub.key
# cas.samlMetadataUi.maxValidity=0
# cas.samlMetadataUi.requireSignedRoot=false
# cas.samlMetadataUi.parameter=entityId
```

### Principal Transformation

Control how principal identifiers are transformed during the resolution phase. 

```properties
# cas.principalTransformation.suffix=
# cas.principalTransformation.uppercase=false
# cas.principalTransformation.prefix=
```

### Hibernate & JDBC 

```properties
# cas.jdbc.showSql=true
# cas.jdbc.genDdl=true
```

### Authentication Password Encoding

```properties
# cas.authn.passwordEncoder.characterEncoding=
# cas.authn.passwordEncoder.encodingAlgorithm=
```
                    
### X509 Principal Resolution

```properties
# cas.authn.x509.principal.principalAttribute=
# cas.authn.x509.principal.returnNull=false
# cas.authn.x509.principalType=SERIAL_NO|SERIAL_NO_DN|SUBJECT|SUBJECT_ALT_NAME|SUBJECT_DN
```

### X509 Authentication

```properties
# cas.authn.x509.checkKeyUsage=false
# cas.authn.x509.revocationPolicyThreshold=172800
# cas.authn.x509.regExSubjectDnPattern=.*
# cas.authn.x509.principalDescriptor=
# cas.authn.x509.maxPathLength=1
# cas.authn.x509.throwOnFetchFailure=false
# cas.authn.x509.regExTrustedIssuerDnPattern=
# cas.authn.x509.valueDelimiter=, 
# cas.authn.x509.checkAll=false
# cas.authn.x509.requireKeyUsage=false
# cas.authn.x509.serialNumberPrefix=SERIALNUMBER=
# cas.authn.x509.refreshIntervalSeconds=3600
# cas.authn.x509.maxPathLengthAllowUnspecified=false
# cas.authn.x509.trustedIssuerDnPattern=
```

## Shiro Authentication

```properties
# cas.authn.shiro.requiredPermissions=value1,value2,...
# cas.authn.shiro.requiredRoles=value1,value2,...
# cas.authn.shiro.config.location=classpath:shiro.ini
```

## NTLM Authentication

```properties
# cas.authn.ntlm.includePattern=
# cas.authn.ntlm.loadBalance=true
# cas.authn.ntlm.domainController=
```

## Trusted Authentication

```properties
# cas.authn.trusted.principalAttribute=
# cas.authn.trusted.returnNull=false
```

## WS-Fed Authentication

```properties
# cas.authn.wsfed.identityProviderUrl=https://adfs.example.org/adfs/ls/
# cas.authn.wsfed.identityProviderIdentifier=https://adfs.example.org/adfs/services/trust
# cas.authn.wsfed.relyingPartyIdentifier=urn:cas:localhost
# cas.authn.wsfed.attributesType=WSFED
# cas.authn.wsfed.signingCertificateResources=classpath:adfs-signing.crt
# cas.authn.wsfed.tolerance=10000
# cas.authn.wsfed.identityAttribute=upn
# cas.authn.wsfed.attributeResolverEnabled=true
# cas.authn.wsfed.autoRedirect=true
```

## WS-Fed Principal Resolution

```properties
# cas.authn.wsfed.principal.principalAttribute=
# cas.authn.wsfed.principal.returnNull=false
```

## Multifactor Authentication

```properties
# cas.authn.mfa.globalPrincipalAttributeNameTriggers=memberOf,eduPersonPrimaryAffiliation
# cas.authn.mfa.requestParameter=authn_method
# cas.authn.mfa.globalFailureMode=CLOSED
# cas.authn.mfa.authenticationContextAttribute=authnContextClass
```

## Multifactor Authentication -> Google Authenticator

```properties
# cas.authn.mfa.gauth.windowSize=3
# cas.authn.mfa.gauth.issuer=
# cas.authn.mfa.gauth.codeDigits=6
# cas.authn.mfa.gauth.label=
# cas.authn.mfa.gauth.timeStepSize=30
# cas.authn.mfa.gauth.rank=0
```


## Multifactor Authentication -> YubiKey

```properties
# cas.authn.mfa.yubikey.clientId=
# cas.authn.mfa.yubikey.secretKey=
# cas.authn.mfa.yubikey.rank=0
```

## Multifactor Authentication -> Radius

```properties
# cas.authn.mfa.radius.failoverOnAuthenticationFailure=false
# cas.authn.mfa.radius.failoverOnException=false
# cas.authn.mfa.radius.rank=0

# cas.authn.mfa.radius.client.socketTimeout=0
# cas.authn.mfa.radius.client.sharedSecret=N0Sh@ar3d$ecReT
# cas.authn.mfa.radius.client.authenticationPort=1812
# cas.authn.mfa.radius.client.accountingPort=1813
# cas.authn.mfa.radius.client.inetAddress=localhost

# cas.authn.mfa.radius.server.retries=3
# cas.authn.mfa.radius.server.nasPortType=-1
# cas.authn.mfa.radius.server.protocol=EAP_MSCHAPv2
# cas.authn.mfa.radius.server.nasRealPort=-1
# cas.authn.mfa.radius.server.nasPortId=-1
# cas.authn.mfa.radius.server.nasIdentifier=-1
# cas.authn.mfa.radius.server.nasPort=-1
# cas.authn.mfa.radius.server.nasIpAddress=
# cas.authn.mfa.radius.server.nasIpv6Address=
```


## Multifactor Authentication -> Duo

```properties
# cas.authn.mfa.duo.duoSecretKey=
# cas.authn.mfa.duo.rank=0
# cas.authn.mfa.duo.duoApplicationKey=
# cas.authn.mfa.duo.duoIntegrationKey=
# cas.authn.mfa.duo.duoApiHost=
```

## Authentication Exceptions

Map custom authentication exceptions in the CAS webflow
and link them to custom messages defined in message bundles. 

```properties
# cas.authn.exceptions.exceptions=value1,value2,...
```

## Accept Users Authentication

```properties
# cas.authn.accept.users=
```

## Saml IdP -> Metadata

```properties
# cas.authn.samlIdp.metadata.cacheExpirationMinutes=30
# cas.authn.samlIdp.metadata.failFast=true
# cas.authn.samlIdp.metadata.location=file:/etc/cas/saml
# cas.authn.samlIdp.metadata.privateKeyAlgName=RSA
# cas.authn.samlIdp.metadata.requireValidMetadata=true
```


## Saml IdP

```properties
# cas.authn.samlIdp.entityId=https://cas.example.org/idp
# cas.authn.samlIdp.hostName=cas.example.org
# cas.authn.samlIdp.scope=example.org
```

## Saml IdP -> Logout

```properties
# cas.authn.samlIdp.logout.forceSignedLogoutRequests=true
# cas.authn.samlIdp.logout.singleLogoutCallbacksDisabled=false
```

## Saml IdP -> Response

```properties
# cas.authn.samlIdp.response.skewAllowance=0
# cas.authn.samlIdp.response.signError=false
# cas.authn.samlIdp.response.overrideSignatureCanonicalizationAlgorithm=
# cas.authn.samlIdp.response.useAttributeFriendlyName=true
```

## OIDC

```properties
# cas.authn.oidc.issuer=http://localhost:8080/cas/oidc
# cas.authn.oidc.skew=5
# cas.authn.oidc.jwksFile=file:/keystore.jwks
```

## Password Policy

```properties
# cas.authn.passwordPolicy.warnAll=false
# cas.authn.passwordPolicy.url=https://password.example.edu/change
# cas.authn.passwordPolicy.displayWarningOnMatch=true
# cas.authn.passwordPolicy.warningDays=30
# cas.authn.passwordPolicy.warningAttributeName=
# cas.authn.passwordPolicy.warningAttributeValue=

# cas.authn.passwordPolicy.policyAttributes.accountLocked=org.apereo.cas.authentication.AccountDisabledException
# cas.authn.passwordPolicy.policyAttributes.mustUpdatePassword=org.apereo.cas.authentication.AccountPasswordMustChangeException
```

## OpenID Principal Resolution

```properties
# cas.authn.openid.principal.principalAttribute=
# cas.authn.openid.principal.returnNull=false
```

## OpenID Authentication

```properties
# cas.authn.openid.enforceRpId=false
```

## SPNEGO Principal Resolution

```properties
# cas.authn.spnego.principal.principalAttribute=
# cas.authn.spnego.principal.returnNull=false
```

## SPNEGO Authentication

```properties
# cas.authn.spnego.kerberosConf=
# cas.authn.spnego.mixedModeAuthentication=false
# cas.authn.spnego.cachePolicy=600
# cas.authn.spnego.timeout=300000
# cas.authn.spnego.jcifsServicePrincipal=HTTP/cas.example.com@EXAMPLE.COM
# cas.authn.spnego.jcifsNetbiosWins=
# cas.authn.spnego.loginConf=
# cas.authn.spnego.ntlmAllowed=true
# cas.authn.spnego.hostNamePatternString=.+
# cas.authn.spnego.jcifsUsername=
# cas.authn.spnego.useSubjectCredsOnly=false
# cas.authn.spnego.supportedBrowsers=MSIE,Trident,Firefox,AppleWebKit
# cas.authn.spnego.jcifsDomainController=
# cas.authn.spnego.dnsTimeout=2000
# cas.authn.spnego.hostNameClientActionStrategy=hostnameSpnegoClientAction
# cas.authn.spnego.kerberosKdc=172.10.1.10
# cas.authn.spnego.alternativeRemoteHostAttribute=alternateRemoteHeader
# cas.authn.spnego.jcifsDomain=
# cas.authn.spnego.ipsToCheckPattern=127.+
# cas.authn.spnego.kerberosDebug=
# cas.authn.spnego.send401OnAuthenticationFailure=true
# cas.authn.spnego.kerberosRealm=EXAMPLE.COM
# cas.authn.spnego.ntlm=false
# cas.authn.spnego.principalWithDomainName=false
# cas.authn.spnego.jcifsServicePassword=
# cas.authn.spnego.jcifsPassword=
# cas.authn.spnego.spnegoAttributeName=distinguishedName
```

## Jaas Authentication

```properties
# cas.authn.jaas.realm=CAS
# cas.authn.jaas.kerberosKdcSystemProperty=
# cas.authn.jaas.kerberosRealmSystemProperty=
```

## Stormpath Authentication

```properties
# cas.authn.stormpath.apiKey=
# cas.authn.stormpath.secretkey=
# cas.authn.stormpath.applicationId=
```

## Remote Address Authentication

```properties
# cas.authn.remoteAddress.ipAddressRange=
```

## Authentication Policy -> Any

Satisfied if any handler succeeds. Supports a tryAll flag to avoid short circuiting 
and try every handler even if one prior succeeded. 

```properties
# cas.authn.policy.any.tryAll=false
```

## Authentication Policy -> All

Satisfied if and only if all given credentials are successfully authenticated. 
Support for multiple credentials is new in CAS and this handler 
would only be acceptable in a multi-factor authentication situation.

```properties
# cas.authn.policy.all.enabled=true
```

## Authentication Policy -> NotPrevented

```properties
# cas.authn.policy.notPrevented.enabled=true
```

## Authentication Policy -> Required

Satisfied if an only if a specified handler successfully authenticates its credential. 

```properties
# cas.authn.policy.req.tryAll=false
# cas.authn.policy.req.handlerName=handlerName
# cas.authn.policy.req.enabled=true
```

## Authentication Policy

```properties
# cas.authn.policy.requiredHandlerAuthenticationPolicyEnabled=false
```

## Pac4j

Act as a proxy, and delegate authentication to external identity providers.

```properties
# cas.authn.pac4j.typedIdUsed=false
```

## Pac4j -> CAS

Delegate authentication to an external CAS server.

```properties
# cas.authn.pac4j.cas.loginUrl=
# cas.authn.pac4j.cas.protocol=
```

## Pac4j -> Facebook

Delegate authentication to Facebook.

```properties
# cas.authn.pac4j.facebook.fields=
# cas.authn.pac4j.facebook.id=
# cas.authn.pac4j.facebook.secret=
# cas.authn.pac4j.facebook.scope=
```

## Pac4j -> Twitter

Delegate authentication to Twitter.

```properties
# cas.authn.pac4j.twitter.id=
# cas.authn.pac4j.twitter.secret=
```

## Pac4j -> OIDC

Delegate authentication to an external OpenID Connect server.

```properties
# cas.authn.pac4j.oidc.discoveryUri=
# cas.authn.pac4j.oidc.maxClockSkew=
# cas.authn.pac4j.oidc.customParamKey2=
# cas.authn.pac4j.oidc.customParamValue2=
# cas.authn.pac4j.oidc.id=
# cas.authn.pac4j.oidc.secret=
# cas.authn.pac4j.oidc.customParamKey1=
# cas.authn.pac4j.oidc.customParamValue1=
# cas.authn.pac4j.oidc.useNonce=
# cas.authn.pac4j.oidc.preferredJwsAlgorithm=
```

## Pac4j -> SAML

Delegate authentication to an external SAML2 IdP.

```properties
# cas.authn.pac4j.saml.keystorePassword=
# cas.authn.pac4j.saml.privateKeyPassword=
# cas.authn.pac4j.saml.serviceProviderEntityId=
# cas.authn.pac4j.saml.keystorePath=
# cas.authn.pac4j.saml.maximumAuthenticationLifetime=
# cas.authn.pac4j.saml.identityProviderMetadataPath=
```

## Radius Authentication

```properties
# cas.authn.radius.server.nasPortId=-1
# cas.authn.radius.server.nasRealPort=-1
# cas.authn.radius.server.protocol=EAP_MSCHAPv2
# cas.authn.radius.server.retries=3
# cas.authn.radius.server.nasPortType=-1
# cas.authn.radius.server.nasPort=-1
# cas.authn.radius.server.nasIpAddress=
# cas.authn.radius.server.nasIpv6Address=
# cas.authn.radius.server.nasIdentifier=-1

# cas.authn.radius.client.authenticationPort=1812
# cas.authn.radius.client.sharedSecret=N0Sh@ar3d$ecReT
# cas.authn.radius.client.socketTimeout=0
# cas.authn.radius.client.inetAddress=localhost
# cas.authn.radius.client.accountingPort=1813

# cas.authn.radius.failoverOnException=false
# cas.authn.radius.failoverOnAuthenticationFailure=false
```

## OAuth -> RefreshToken Expiration Policy

```properties
# cas.authn.oauth.refreshToken.timeToKillInSeconds=2592000
```

## OAuth -> AccessToken Expiration Policy

```properties
# cas.authn.oauth.accessToken.timeToKillInSeconds=7200
# cas.authn.oauth.accessToken.maxTimeToLiveInSeconds=28800
```

## OAuth -> Code Expiration Policy

```properties
# cas.authn.oauth.code.timeToKillInSeconds=30
# cas.authn.oauth.code.numberOfUses=1
```

## File Authentication

```properties
# cas.authn.file.separator=::
# cas.authn.file.filename=
```

## Reject Users Authentication

```properties
# cas.authn.reject.users=user1,user2
```

## JDBC Authentication -> Query

Authenticates a user by comparing the (hashed) user password against the password on record determined by a configurable database query.

```properties
# cas.authn.jdbc.query[0].healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.authn.jdbc.query[0].isolateInternalQueries=false
# cas.authn.jdbc.query[0].url=jdbc:hsqldb:mem:cas-hsql-database
# cas.authn.jdbc.query[0].failFast=true
# cas.authn.jdbc.query[0].isolationLevelName=ISOLATION_READ_COMMITTED
# cas.authn.jdbc.query[0].dialect=org.hibernate.dialect.HSQLDialect
# cas.authn.jdbc.query[0].leakThreshold=10
# cas.authn.jdbc.query[0].propagationBehaviorName=PROPAGATION_REQUIRED
# cas.authn.jdbc.query[0].batchSize=1
# cas.authn.jdbc.query[0].user=sa
# cas.authn.jdbc.query[0].ddlAuto=create-drop
# cas.authn.jdbc.query[0].maxAgeDays=180
# cas.authn.jdbc.query[0].password=
# cas.authn.jdbc.query[0].autocommit=false
# cas.authn.jdbc.query[0].driverClass=org.hsqldb.jdbcDriver
# cas.authn.jdbc.query[0].idleTimeout=5000
# cas.authn.jdbc.query[0].sql=
```

## JDBC Authentication -> Search

Searches for a user record by querying against a username and password; the user is authenticated if at least one result is found.

```properties
# cas.authn.jdbc.search[0].fieldUser=
# cas.authn.jdbc.search[0].tableUsers=
# cas.authn.jdbc.search[0].fieldPassword=
# cas.authn.jdbc.search[0].healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.authn.jdbc.search[0].isolateInternalQueries=false
# cas.authn.jdbc.search[0].url=jdbc:hsqldb:mem:cas-hsql-database
# cas.authn.jdbc.search[0].failFast=true
# cas.authn.jdbc.search[0].isolationLevelName=ISOLATION_READ_COMMITTED
# cas.authn.jdbc.search[0].dialect=org.hibernate.dialect.HSQLDialect
# cas.authn.jdbc.search[0].leakThreshold=10
# cas.authn.jdbc.search[0].propagationBehaviorName=PROPAGATION_REQUIRED
# cas.authn.jdbc.search[0].batchSize=1
# cas.authn.jdbc.search[0].user=sa
# cas.authn.jdbc.search[0].ddlAuto=create-drop
# cas.authn.jdbc.search[0].maxAgeDays=180
# cas.authn.jdbc.search[0].password=
# cas.authn.jdbc.search[0].autocommit=false
# cas.authn.jdbc.search[0].driverClass=org.hsqldb.jdbcDriver
# cas.authn.jdbc.search[0].idleTimeout=5000
```

## JDBC Authentication -> Bind

Authenticates a user by attempting to create a database connection using the username and (hashed) password.

```properties
# cas.authn.jdbc.bind[0].healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.authn.jdbc.bind[0].isolateInternalQueries=false
# cas.authn.jdbc.bind[0].url=jdbc:hsqldb:mem:cas-hsql-database
# cas.authn.jdbc.bind[0].failFast=true
# cas.authn.jdbc.bind[0].isolationLevelName=ISOLATION_READ_COMMITTED
# cas.authn.jdbc.bind[0].dialect=org.hibernate.dialect.HSQLDialect
# cas.authn.jdbc.bind[0].leakThreshold=10
# cas.authn.jdbc.bind[0].propagationBehaviorName=PROPAGATION_REQUIRED
# cas.authn.jdbc.bind[0].batchSize=1
# cas.authn.jdbc.bind[0].user=sa
# cas.authn.jdbc.bind[0].ddlAuto=create-drop
# cas.authn.jdbc.bind[0].maxAgeDays=180
# cas.authn.jdbc.bind[0].password=
# cas.authn.jdbc.bind[0].autocommit=false
# cas.authn.jdbc.bind[0].driverClass=org.hsqldb.jdbcDriver
# cas.authn.jdbc.bind[0].idleTimeout=5000
```

## JDBC Authentication -> Encode

A JDBC querying handler that will pull back the password and the private salt value for a user and validate the encoded 
password using the public salt value. Assumes everything is inside the same database table. Supports settings for 
number of iterations as well as private salt.

This password encoding method, combines the private Salt and the public salt which it prepends to the password before hashing. 
If multiple iterations are used, the bytecode Hash of the first iteration is rehashed without the salt values. The final hash 
is converted to Hex before comparing it to the database value.

```properties
# cas.authn.jdbc.encode[0].numberOfIterations=0
# cas.authn.jdbc.encode[0].numberOfIterationsFieldName=numIterations
# cas.authn.jdbc.encode[0].saltFieldName=salt
# cas.authn.jdbc.encode[0].staticSalt=
# cas.authn.jdbc.encode[0].sql=
# cas.authn.jdbc.encode[0].algorithmName=
# cas.authn.jdbc.encode[0].passwordFieldName=password
# cas.authn.jdbc.encode[0].healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.authn.jdbc.encode[0].isolateInternalQueries=false
# cas.authn.jdbc.encode[0].url=jdbc:hsqldb:mem:cas-hsql-database
# cas.authn.jdbc.encode[0].failFast=true
# cas.authn.jdbc.encode[0].isolationLevelName=ISOLATION_READ_COMMITTED
# cas.authn.jdbc.encode[0].dialect=org.hibernate.dialect.HSQLDialect
# cas.authn.jdbc.encode[0].leakThreshold=10
# cas.authn.jdbc.encode[0].propagationBehaviorName=PROPAGATION_REQUIRED
# cas.authn.jdbc.encode[0].batchSize=1
# cas.authn.jdbc.encode[0].user=sa
# cas.authn.jdbc.encode[0].ddlAuto=create-drop
# cas.authn.jdbc.encode[0].maxAgeDays=180
# cas.authn.jdbc.encode[0].password=
# cas.authn.jdbc.encode[0].autocommit=false
# cas.authn.jdbc.encode[0].driverClass=org.hsqldb.jdbcDriver
# cas.authn.jdbc.encode[0].idleTimeout=5000
```

## Mongo Authentication

```properties
# cas.authn.mongo.mongoHostUri=mongodb://uri
# cas.authn.mongo.usernameAttribute=username
# cas.authn.mongo.attributes=
# cas.authn.mongo.passwordAttribute=password
# cas.authn.mongo.collectionName=users
```

## Authentication Throttling

```properties
# cas.authn.throttle.usernameParameter=username
# cas.authn.throttle.startDelay=10000
# cas.authn.throttle.repeatInterval=20000
# cas.authn.throttle.appcode=CAS

# cas.authn.throttle.failure.threshold=100
# cas.authn.throttle.failure.code=AUTHENTICATION_FAILED
# cas.authn.throttle.failure.rangeSeconds=60
```

## Authentication Throttle via JPA

```properties
# cas.authn.throttle.jdbc.auditQuery=SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC
# cas.authn.throttle.jdbc.healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.authn.throttle.jdbc.isolateInternalQueries=false
# cas.authn.throttle.jdbc.url=jdbc:hsqldb:mem:cas-hsql-database
# cas.authn.throttle.jdbc.failFast=true
# cas.authn.throttle.jdbc.isolationLevelName=ISOLATION_READ_COMMITTED
# cas.authn.throttle.jdbc.dialect=org.hibernate.dialect.HSQLDialect
# cas.authn.throttle.jdbc.leakThreshold=10
# cas.authn.throttle.jdbc.propagationBehaviorName=PROPAGATION_REQUIRED
# cas.authn.throttle.jdbc.batchSize=1
# cas.authn.throttle.jdbc.user=sa
# cas.authn.throttle.jdbc.ddlAuto=create-drop
# cas.authn.throttle.jdbc.maxAgeDays=180
# cas.authn.throttle.jdbc.password=
# cas.authn.throttle.jdbc.autocommit=false
# cas.authn.throttle.jdbc.driverClass=org.hsqldb.jdbcDriver
# cas.authn.throttle.jdbc.idleTimeout=5000

# cas.authn.throttle.jdbc.pool.suspension=false
# cas.authn.throttle.jdbc.pool.minSize=6
# cas.authn.throttle.jdbc.pool.maxSize=18
# cas.authn.throttle.jdbc.pool.maxIdleTime=1000
# cas.authn.throttle.jdbc.pool.maxWait=2000
```

## Locale

```properties
# cas.locale.paramName=locale
# cas.locale.defaultValue=en
```

## Global SSO Behavior

```properties
# cas.sso.missingService=true
# cas.sso.renewedAuthn=true
```

## Ticket Granting Cookie

```properties
# cas.tgc.path=
# cas.tgc.maxAge=-1
# cas.tgc.domain=
# cas.tgc.signingKey=
# cas.tgc.name=TGC
# cas.tgc.encryptionKey=
# cas.tgc.secure=true
# cas.tgc.rememberMeMaxAge=1209600
# cas.tgc.cipherEnabled=true
```

## Ldap Authentication

CAS authenticates a username/password against an LDAP directory such as Active Directory or OpenLDAP. 
There are numerous directory architectures and we provide configuration for four common cases.

- Active Directory - Users authenticate with sAMAccountName.
- Authenticated Search - Manager bind/search followed by user simple bind.
- Anonymous Search - Anonymous search followed by user simple bind.
- Direct Bind - Compute user DN from format string and perform simple bind. This is relevant when
no search is required to compute the DN needed for a bind operation. There are two requirements for this use case:
1. All users are under a single branch in the directory, e.g. `ou=Users,dc=example,dc=org`.
2. The username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=exmaple,dc=org`.

Note that CAS will automatically create the appropriate components internally
based on the settings specified below. If you wish to authenticate against more than one LDAP
server, simply increment the index and specify the settings for the latter LDAP. 

```properties
# cas.authn.ldap[0].ldapUrl=ldaps://ldap1.example.edu,ldaps://ldap2.example.edu,...
# cas.authn.ldap[0].useSsl=true
# cas.authn.ldap[0].useStartTls=false
# cas.authn.ldap[0].connectTimeout=5000
# cas.authn.ldap[0].baseDn=dc=example,dc=org
# cas.authn.ldap[0].userFilter=cn={user}
# cas.authn.ldap[0].subtreeSearch=true
# cas.authn.ldap[0].usePasswordPolicy=true
# cas.authn.ldap[0].bindDn=cn=Directory Manager,dc=example,dc=org
# cas.authn.ldap[0].bindCredential=Password

# cas.authn.ldap[0].dnFormat=uid=%s,ou=people,dc=example,dc=org
# cas.authn.ldap[0].principalAttributeId=uid
# cas.authn.ldap[0].principalAttributeList=sn,cn,givenName
# cas.authn.ldap[0].allowMultiplePrincipalAttributeValues=true
# cas.authn.ldap[0].additionalAttributes=
# cas.authn.ldap[0].type=AD|AUTHENTICATED|DIRECT|ANONYMOUS

# cas.authn.ldap[0].trustCertificates=
# cas.authn.ldap[0].keystore=
# cas.authn.ldap[0].keystorePassword=
# cas.authn.ldap[0].keystoreType=JKS|JCEKS|PKCS12

# cas.authn.ldap[0].minPoolSize=3
# cas.authn.ldap[0].maxPoolSize=10
# cas.authn.ldap[0].validateOnCheckout=true
# cas.authn.ldap[0].validatePeriodically=true
# cas.authn.ldap[0].validatePeriod=600

# cas.authn.ldap[0].failFast=true
# cas.authn.ldap[0].idleTime=500
# cas.authn.ldap[0].prunePeriod=600
# cas.authn.ldap[0].blockWaitTime=5000

# cas.authn.ldap[0].providerClass=org.ldaptive.provider.unboundid.UnboundIDProvider
# cas.authn.ldap[0].allowMultipleDns=false
```

## Ldap Authorization

```properties
# cas.ldapAuthz.rolePrefix=ROLE_
# cas.ldapAuthz.allowMultipleResults=false
# cas.ldapAuthz.searchFilter=
# cas.ldapAuthz.baseDn=
# cas.ldapAuthz.roleAttribute=uugid
```

## Logout

```properties
# cas.logout.followServiceRedirects=false
```

## Views

```properties
# cas.view.cas2.success=protocol/2.0/casServiceValidationSuccess
# cas.view.cas2.failure=protocol/2.0/casServiceValidationFailure
# cas.view.cas2.proxy.success=protocol/2.0/casProxySuccessView
# cas.view.cas2.proxy.failure=protocol/2.0/casProxyFailureView

# cas.view.cas3.success=protocol/3.0/casServiceValidationSuccess
# cas.view.cas3.failure=protocol/3.0/casServiceValidationFailure
# cas.view.cas3.releaseProtocolAttributes=true
```

## Clearpass

```properties
# cas.clearpass.cacheCredential=false
```

## Message Bundles

```properties
# cas.messageBundle.encoding=UTF-8
# cas.messageBundle.fallbackSystemLocale=false
# cas.messageBundle.cacheSeconds=180
# cas.messageBundle.useCodeMessage=true
# cas.messageBundle.baseNames=classpath:custom_messagesclasspath:messages
```

## Shibboleth Attribute Resolver

```properties
# cas.shibAttributeResolver.resources=classpath:/attribute-resolver.xml
```

## Audit -> JPA

```properties
# cas.audit.jdbc.healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.audit.jdbc.isolateInternalQueries=false
# cas.audit.jdbc.url=jdbc:hsqldb:mem:cas-hsql-database
# cas.audit.jdbc.failFast=true
# cas.audit.jdbc.isolationLevelName=ISOLATION_READ_COMMITTED
# cas.audit.jdbc.dialect=org.hibernate.dialect.HSQLDialect
# cas.audit.jdbc.leakThreshold=10
# cas.audit.jdbc.propagationBehaviorName=PROPAGATION_REQUIRED
# cas.audit.jdbc.batchSize=1
# cas.audit.jdbc.user=sa
# cas.audit.jdbc.ddlAuto=create-drop
# cas.audit.jdbc.maxAgeDays=180
# cas.audit.jdbc.password=
# cas.audit.jdbc.autocommit=false
# cas.audit.jdbc.driverClass=org.hsqldb.jdbcDriver
# cas.audit.jdbc.idleTimeout=5000

# cas.audit.jdbc.pool.suspension=false
# cas.audit.jdbc.pool.minSize=6
# cas.audit.jdbc.pool.maxSize=18
# cas.audit.jdbc.pool.maxIdleTime=1000
# cas.audit.jdbc.pool.maxWait=2000
```

## Audit Global

```properties
# cas.audit.auditFormat=DEFAULT
# cas.audit.ignoreAuditFailures=false
# cas.audit.singlelineSeparator=|
# cas.audit.useSingleLine=false
# cas.audit.appCode=CAS
```

## Warning Cookie

```properties
# cas.warningCookie.path=
# cas.warningCookie.maxAge=-1
# cas.warningCookie.domain=
# cas.warningCookie.name=CASPRIVACY
# cas.warningCookie.secure=true
```

## CAS Host

A CAS host is automatically appended to the ticket ids generated by CAS.
If none is specified, one is automatically detected and used by CAS. 

```properties
# cas.host.name=
```

## Monitor -> Ticket Granting Tickets

Decide how CAS should monitor the generation of TGTs. 

```properties
# cas.monitor.tgt.warn.threshold=10
# cas.monitor.tgt.warn.evictionThreshold=0
```

## Monitor -> Service Tickets

Decide how CAS should monitor the generation of STs. 

```properties
# cas.monitor.st.warn.threshold=10
# cas.monitor.st.warn.evictionThreshold=0
```

## Monitor -> Cache (Ehcache, Hazelcast, etc) Monitors

Decide how CAS should monitor the internal state of various cache storage services.

```properties
# cas.monitor.warn.threshold=10
# cas.monitor.warn.evictionThreshold=0
```

## Monitor -> JDBC DataSource

Decide how CAS should monitor the internal state of JDBC connections used
for authentication or attribute retrieval.

```properties
# cas.monitor.jdbc.validationQuery=SELECT 1
# cas.monitor.jdbc.maxWait=5000
# cas.monitor.jdbc.healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.monitor.jdbc.isolateInternalQueries=false
# cas.monitor.jdbc.url=jdbc:hsqldb:mem:cas-hsql-database
# cas.monitor.jdbc.failFast=true
# cas.monitor.jdbc.isolationLevelName=ISOLATION_READ_COMMITTED
# cas.monitor.jdbc.dialect=org.hibernate.dialect.HSQLDialect
# cas.monitor.jdbc.leakThreshold=10
# cas.monitor.jdbc.propagationBehaviorName=PROPAGATION_REQUIRED
# cas.monitor.jdbc.batchSize=1
# cas.monitor.jdbc.user=sa
# cas.monitor.jdbc.ddlAuto=create-drop
# cas.monitor.jdbc.maxAgeDays=180
# cas.monitor.jdbc.password=
# cas.monitor.jdbc.autocommit=false
# cas.monitor.jdbc.driverClass=org.hsqldb.jdbcDriver
# cas.monitor.jdbc.idleTimeout=5000
```

## Monitor -> LDAP Connection Pool

Decide how CAS should monitor the internal state of LDAP connections
used for authentication, etc.

```properties
# cas.monitor.ldap.pool.suspension=false
# cas.monitor.ldap.pool.minSize=6
# cas.monitor.ldap.pool.maxSize=18
# cas.monitor.ldap.pool.maxIdleTime=1000
# cas.monitor.ldap.pool.maxWait=2000
# cas.monitor.ldap.maxWait=5000
```

## Monitor Memory

Decide how CAS should monitor the internal state of JVM memory available at runtime.

```properties
# cas.monitor.freeMemThreshold=10
```

## Themes

```properties
# cas.theme.paramName=theme
# cas.theme.defaultThemeName=cas-theme-default
```

## Acceptable Usage Policy

Decide how CAS should attempt to determine whether AUP is accepted. 

```properties
# cas.acceptableUsagePolicy.aupAttributeName=aupAccepted
```

## Acceptable Usage Policy -> Ldap

If AUP is controlled via LDAP, decide how choices should be remembered back inside the LDAP instance.

```properties
# cas.acceptableUsagePolicy.ldap.searchFilter=cn={0}
# cas.acceptableUsagePolicy.ldap.baseDn=dc=example,dc=org
```

## Events JPA Database

Decide how CAS should store authentication events.

```properties
# cas.events.jpa.database.healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.events.jpa.database.isolateInternalQueries=false
# cas.events.jpa.database.url=jdbc:hsqldb:mem:cas-events
# cas.events.jpa.database.failFast=true
# cas.events.jpa.database.dialect=org.hibernate.dialect.HSQLDialect
# cas.events.jpa.database.leakThreshold=10
# cas.events.jpa.database.batchSize=1
# cas.events.jpa.database.user=sa
# cas.events.jpa.database.ddlAuto=create-drop
# cas.events.jpa.database.password=
# cas.events.jpa.database.autocommit=false
# cas.events.jpa.database.driverClass=org.hsqldb.jdbcDriver
# cas.events.jpa.database.idleTimeout=5000

# cas.events.jpa.database.pool.suspension=false
# cas.events.jpa.database.pool.minSize=6
# cas.events.jpa.database.pool.maxSize=18
# cas.events.jpa.database.pool.maxIdleTime=1000
# cas.events.jpa.database.pool.maxWait=2000
```

## Events -> Mongodb


Decide how CAS should store authentication events.

```properties
# cas.events.mongodb.clientUri=
# cas.events.mongodb.dropCollection=false
# cas.events.mongodb.collection=MongoDbCasEventRepository
```

## Events

Decide how CAS should track authentication events. 

```properties
# cas.events.trackGeolocation=false
```


## Http Client

```properties
# cas.httpClient.connectionTimeout=5000
# cas.httpClient.asyncTimeout=5000
# cas.httpClient.readTimeout=5000
```

## Http Client -> Truststore

```properties
# cas.httpClient.truststore.psw=changeit
# cas.httpClient.truststore.file=classpath:/truststore.jks
```

## Mongo Service Registry

```properties
# cas.serviceRegistry.mongo.idleTimeout=30000
# cas.serviceRegistry.mongo.port=27017
# cas.serviceRegistry.mongo.dropCollection=false
# cas.serviceRegistry.mongo.socketKeepAlive=false
# cas.serviceRegistry.mongo.userPassword=
# cas.serviceRegistry.mongo.serviceRegistryCollection=cas-service-registry
# cas.serviceRegistry.mongo.timeout=5000
# cas.serviceRegistry.mongo.userId=
# cas.serviceRegistry.mongo.writeConcern=NORMAL
# cas.serviceRegistry.mongo.host=localhost

# cas.serviceRegistry.mongo.conns.lifetime=60000
# cas.serviceRegistry.mongo.conns.perHost=10
```

## Service Registry

If the underlying service registry is using local system resources
to locate service definitions, decide how those resources should be found.

```properties
# cas.serviceRegistry.config.location=classpath:/services
```

## Ldap Service Registry

Control how CAS services should be found inside an LDAP instance.

```properties
# cas.serviceRegistry.ldap.serviceDefinitionAttribute=description
# cas.serviceRegistry.ldap.idAttribute=uid
# cas.serviceRegistry.ldap.objectClass=casRegisteredService
# cas.serviceRegistry.ldap.baseDn=dc=example,dc=org
```

## Couchbase Service Registry


Control how CAS services should be found inside a Couchbase instance.

```properties
# cas.serviceRegistry.couchbase.nodeSet=localhost:8091
# cas.serviceRegistry.couchbase.password=
# cas.serviceRegistry.couchbase.queryEnabled=true
# cas.serviceRegistry.couchbase.bucket=default
# cas.serviceRegistry.couchbase.timeout=10
```

## JPA Service Registry

Control how CAS services should be found inside a database instance

```properties
# cas.serviceRegistry.jpa.healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.serviceRegistry.jpa.isolateInternalQueries=false
# cas.serviceRegistry.jpa.url=jdbc:hsqldb:mem:cas-service-registry
# cas.serviceRegistry.jpa.failFast=true
# cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.HSQLDialect
# cas.serviceRegistry.jpa.leakThreshold=10
# cas.serviceRegistry.jpa.batchSize=1
# cas.serviceRegistry.jpa.user=sa
# cas.serviceRegistry.jpa.ddlAuto=create-drop
# cas.serviceRegistry.jpa.password=
# cas.serviceRegistry.jpa.autocommit=false
# cas.serviceRegistry.jpa.driverClass=org.hsqldb.jdbcDriver
# cas.serviceRegistry.jpa.idleTimeout=5000

# cas.serviceRegistry.jpa.pool.suspension=false
# cas.serviceRegistry.jpa.pool.minSize=6
# cas.serviceRegistry.jpa.pool.maxSize=18
# cas.serviceRegistry.jpa.pool.maxIdleTime=1000
# cas.serviceRegistry.jpa.pool.maxWait=2000
```

## Service Registry Global

```properties
# cas.serviceRegistry.watcherEnabled=true
# cas.serviceRegistry.repeatInterval=120000
# cas.serviceRegistry.startDelay=15000
# cas.serviceRegistry.initFromJson=true
```

## Proxy Tickets

```properties
# cas.ticket.pt.timeToKillInSeconds=10
# cas.ticket.pt.numberOfUses=1
```


## JPA Ticket Registry

```properties
# cas.ticket.registry.jpa.jpaLockingTimeout=3600
# cas.ticket.registry.jpa.healthQuery=SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS
# cas.ticket.registry.jpa.isolateInternalQueries=false
# cas.ticket.registry.jpa.url=jdbc:hsqldb:mem:cas-ticket-registry
# cas.ticket.registry.jpa.failFast=true
# cas.ticket.registry.jpa.dialect=org.hibernate.dialect.HSQLDialect
# cas.ticket.registry.jpa.leakThreshold=10
# cas.ticket.registry.jpa.jpaLockingTgtEnabled=true
# cas.ticket.registry.jpa.batchSize=1
# cas.ticket.registry.jpa.user=sa
# cas.ticket.registry.jpa.ddlAuto=create-drop
# cas.ticket.registry.jpa.password=
# cas.ticket.registry.jpa.autocommit=false
# cas.ticket.registry.jpa.driverClass=org.hsqldb.jdbcDriver
# cas.ticket.registry.jpa.idleTimeout=5000

# cas.ticket.registry.jpa.pool.suspension=false
# cas.ticket.registry.jpa.pool.minSize=6
# cas.ticket.registry.jpa.pool.maxSize=18
# cas.ticket.registry.jpa.pool.maxIdleTime=1000
# cas.ticket.registry.jpa.pool.maxWait=2000
```

## Couchbase Ticket Registry

```properties
# cas.ticket.registry.couchbase.timeout=10
# cas.ticket.registry.couchbase.nodeSet=localhost:8091
# cas.ticket.registry.couchbase.password=
# cas.ticket.registry.couchbase.queryEnabled=true
# cas.ticket.registry.couchbase.bucket=default
```

## Hazelcast Ticket Registry

```properties
# cas.ticket.registry.hazelcast.pageSize=500
# cas.ticket.registry.hazelcast.mapName=tickets
# cas.ticket.registry.hazelcast.configLocation=

# cas.ticket.registry.hazelcast.cluster.evictionPolicy=LRU
# cas.ticket.registry.hazelcast.cluster.maxNoHeartbeatSeconds=300
# cas.ticket.registry.hazelcast.cluster.multicastEnabled=false
# cas.ticket.registry.hazelcast.cluster.evictionPercentage=10
# cas.ticket.registry.hazelcast.cluster.tcpipEnabled=true
# cas.ticket.registry.hazelcast.cluster.members=localhost
# cas.ticket.registry.hazelcast.cluster.loggingType=slf4j
# cas.ticket.registry.hazelcast.cluster.instanceName=localhost
# cas.ticket.registry.hazelcast.cluster.port=5701
# cas.ticket.registry.hazelcast.cluster.portAutoIncrement=true
# cas.ticket.registry.hazelcast.cluster.maxHeapSizePercentage=85
# cas.ticket.registry.hazelcast.cluster.maxSizePolicy=USED_HEAP_PERCENTAGE
```

## Infinispan Ticket Registry

```properties
# cas.ticket.registry.infinispan.cacheName=
# cas.ticket.registry.infinispan.configLocation=/infinispan.xml
```

## Ticket Registry Cryptography -> Signing & Encryption

Decide whether the underlying ticket registry should
encrypt and sign ticket objects in transition. This is mostly applicable
to registry choices that are designed to share tickets across multiple CAS nodes.

```properties
# cas.ticket.registry.signing.key=
# cas.ticket.registry.signing.keySize=512
# cas.ticket.registry.encryption.key=
# cas.ticket.registry.encryption.keySize=16
# cas.ticket.registry.alg=AES
```

## Ticket Registry -> InMemory

```properties
# cas.ticket.registry.inMemory.loadFactor=1
# cas.ticket.registry.inMemory.concurrency=20
# cas.ticket.registry.inMemory.initialCapacity=1000
```

## Ticket Registry -> Cleaner

```properties
# cas.ticket.registry.cleaner.appId=cas-ticket-registry-cleaner
# cas.ticket.registry.cleaner.startDelay=10000
# cas.ticket.registry.cleaner.repeatInterval=60000
# cas.ticket.registry.cleaner.enabled=true
```

## Ehcache Ticket Registry

```properties
# cas.ticket.registry.ehcache.replicateUpdatesViaCopy=true
# cas.ticket.registry.ehcache.maxElementsInMemory=10000
# cas.ticket.registry.ehcache.cacheManagerName=ticketRegistryCacheManager
# cas.ticket.registry.ehcache.replicatePuts=true
# cas.ticket.registry.ehcache.replicateUpdates=true
# cas.ticket.registry.ehcache.memoryStoreEvictionPolicy=LRU
# cas.ticket.registry.ehcache.configLocation=classpath:/ehcache-replicated.xml
# cas.ticket.registry.ehcache.maximumBatchSize=100
# cas.ticket.registry.ehcache.overflowToDisk=false
# cas.ticket.registry.ehcache.shared=false
# cas.ticket.registry.ehcache.replicationInterval=10000
# cas.ticket.registry.ehcache.cacheTimeToLive=2147483647
# cas.ticket.registry.ehcache.diskExpiryThreadIntervalSeconds=0
# cas.ticket.registry.ehcache.replicateRemovals=true
# cas.ticket.registry.ehcache.maxChunkSize=5000000
# cas.ticket.registry.ehcache.maxElementsOnDisk=0
# cas.ticket.registry.ehcache.cacheName=org.apereo.cas.ticket.TicketCache
# cas.ticket.registry.ehcache.eternal=false
# cas.ticket.registry.ehcache.loaderAsync=true
# cas.ticket.registry.ehcache.replicatePutsViaCopy=true
# cas.ticket.registry.ehcache.cacheTimeToIdle=0
# cas.ticket.registry.ehcache.diskPersistent=false
```

## Ignite Ticket Registry

```properties
# cas.ticket.registry.ignite.keyAlgorithm=
# cas.ticket.registry.ignite.protocol=
# cas.ticket.registry.ignite.trustStorePassword=
# cas.ticket.registry.ignite.keyStoreType=
# cas.ticket.registry.ignite.keyStoreFilePath=
# cas.ticket.registry.ignite.keyStorePassword=
# cas.ticket.registry.ignite.trustStoreType=
# cas.ticket.registry.ignite.igniteAddresses=localhost:47500
# cas.ticket.registry.ignite.trustStoreFilePath=

# cas.ticket.registry.ignite.ticketsCache.writeSynchronizationMode=FULL_SYNC
# cas.ticket.registry.ignite.ticketsCache.atomicityMode=TRANSACTIONAL
# cas.ticket.registry.ignite.ticketsCache.cacheName=TicketsCache
# cas.ticket.registry.ignite.ticketsCache.cacheMode=REPLICATED
```

## Memcached Ticket Registry

```properties
# cas.ticket.registry.memcached.servers=localhost:11211
# cas.ticket.registry.memcached.locatorType=ARRAY_MOD
# cas.ticket.registry.memcached.failureMode=Redistribute
# cas.ticket.registry.memcached.hashAlgorithm=FNV1_64_HASH
```

## Service Ticket Expiration Policy

Controls the expiration policy of service tickets, as well as other properties
applicable to STs. 

```properties
# cas.ticket.st.maxLength=20

# cas.ticket.st.numberOfUses=1
# cas.ticket.st.timeToKillInSeconds=10
```

## Ticket Granting Ticket

```properties
# cas.ticket.tgt.onlyTrackMostRecentSession=true
# cas.ticket.tgt.maxLength=50
```

## Proxy Granting Ticket

```properties
# cas.ticket.pgt.maxLength=50
```

## TGT Expiration Policy -> Remember Me

```properties
# cas.ticket.tgt.rememberMe.enabled=true
# cas.ticket.tgt.rememberMe.timeToKillInSeconds=true
```

## TGT Expiration Policy -> Timeout

The expiration policy applied to TGTs provides for most-recently-used expiration policy, similar to a Web server session timeout.

```properties
# cas.ticket.tgt.timeout.maxTimeToLiveInSeconds=28800
```

## TGT Expiration Policy -> Throttled Timeout

The throttled timeout policy extends the Timeout policy with the concept of throttling where a ticket may be used at most every N seconds.

```properties
# cas.ticket.tgt.throttledTimeout.timeToKillInSeconds=28800
# cas.ticket.tgt.throttledTimeout.timeInBetweenUsesInSeconds=5
```

## TGT Expiration Policy -> Hard Timeout

The hard timeout policy provides for finite ticket lifetime as measured from the time of creation.

```properties
# cas.ticket.tgt.hardTimeout.timeToKillInSeconds=28800
```

## TGT Expiration Policy

Provides a hard-time out as well as a sliding window.

```properties
# cas.ticket.tgt.maxTimeToLiveInSeconds=28800 (Set to a negative value to never expire tickets)
# cas.ticket.tgt.timeToKillInSeconds=7200 (Set to a negative value to never expire tickets)
```

## Saml Response

```properties
# cas.samlCore.ticketidSaml2=false
# cas.samlCore.skewAllowance=0
# cas.samlCore.attributeNamespace=http://www.ja-sig.org/products/cas/
# cas.samlCore.issuer=localhost
# cas.samlCore.securityManager=org.apache.xerces.util.SecurityManager
```

## Maxmind GeoTracking

```properties
# cas.maxmind.cityDatabase=
# cas.maxmind.countryDatabase=
```

## Management Webapp

```properties
# cas.mgmt.adminRoles=ROLE_ADMIN
# cas.mgmt.userPropertiesFile=classpath:/user-details.properties
# cas.mgmt.serverName=https://localhost:8443
```

## Google Analytics Integration

```properties
# cas.googleAnalytics.googleAnalyticsTrackingId=
```

## Http Web Requests

```properties
# cas.httpWebRequest.header.xframe=false
# cas.httpWebRequest.header.xss=false
# cas.httpWebRequest.header.hsts=false
# cas.httpWebRequest.header.xcontent=false
# cas.httpWebRequest.header.cache=false

# cas.httpWebRequest.web.forceEncoding=true
# cas.httpWebRequest.web.encoding=UTF-8

# cas.httpWebRequest.allowMultiValueParameters=false
# cas.httpWebRequest.onlyPostParams=username,password
# cas.httpWebRequest.paramsToCheck=ticket,service,renew,gateway,warn,method,target,SAMLart,pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token

spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true
```


## Webflow Cryptography -> Encryption & Signing

```properties
# cas.webflow.signing.key=
# cas.webflow.signing.keySize=512

# cas.webflow.encryption.keySize=16
# cas.webflow.encryption.key=

# cas.webflow.alg=AES
```

## Webflow -> Session Management

Control how Spring Webflow's conversational session state should be managed by CAS. 


```properties
# cas.webflow.session.lockTimeout=30
# cas.webflow.session.compress=false
# cas.webflow.session.maxConversations=5
# cas.webflow.session.storage=true
# cas.webflow.session.hzLocation=classpath:/hazelcast.xml
```

## Webflow Global

```properties
# cas.webflow.autoconfigure=true
# cas.webflow.alwaysPauseRedirect=false
# cas.webflow.refresh=true
# cas.webflow.redirectSameState=false
```

## Principal Resolution Global

```properties
# cas.personDirectory.principalAttribute=
# cas.personDirectory.returnNull=false
```

## REST Authentication

```properties
# cas.authn.rest.uri=https://...
```

## REST API

```properties
# cas.rest.attributeName=
# cas.rest.attributeValue=
# cas.rest.throttler=neverThrottle
```

## Single Logout

```properties
# cas.slo.disabled=false
# cas.slo.asynchronous=true
```

## Metrics

```properties
# cas.metrics.loggerName=perfStatsLogger
# cas.metrics.refreshInterval=30
```

## Google Apps Authentication

```properties
# cas.googleApps.publicKeyLocation=file:/etc/cas/public.key
# cas.googleApps.keyAlgorithm=RSA
# cas.googleApps.privateKeyLocation=file:/etc/cas/private.key

```
