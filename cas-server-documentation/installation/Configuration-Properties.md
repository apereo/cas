---
layout: default
title: CAS - Properties
---

# CAS Properties

Various properties can be specified in CAS [either inside configuration files or as command 
line switches](Configuration-Management.md). This section provides a list common CAS properties and 
references to the underlying modules that consume them.

<div class="alert alert-info"><strong> Be Selective</strong><p>
This section is meant as a guide only. Do <strong>NOT</strong> copy/paste the entire collection of settings 
into your CAS configuration; rather pick only the properties that you need.</p></div>

## Spring Boot & Cloud

The following list of properties are controlled and provided to CAS:

```properties
##
# CAS Server Context Configuration
#
server.context-path=/cas
server.port=8443

server.ssl.key-store=file:/etc/cas/thekeystore
server.ssl.key-store-password=changeit
server.ssl.key-password=changeit
# server.ssl.ciphers= 
# server.ssl.client-auth=
# server.ssl.enabled=
# server.ssl.key-alias=
# server.ssl.key-store-provider=
# server.ssl.key-store-type=
# server.ssl.protocol=
# server.ssl.trust-store=
# server.ssl.trust-store-password=
# server.ssl.trust-store-provider=
# server.ssl.trust-store-type=

server.tomcat.basedir=build/tomcat
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=%t %a "%r" %s (%D ms)
server.tomcat.accesslog.suffix=.log
server.tomcat.max-http-header-size=20971520
server.tomcat.max-threads=5
server.tomcat.port-header=X-Forwarded-Port
server.tomcat.protocol-header=X-Forwarded-Proto
server.tomcat.protocol-header-https-value=https
server.tomcat.remote-ip-header=X-FORWARDED-FOR
server.tomcat.uri-encoding=UTF-8

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
cas.securityContext.adminpages.ip=.+
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
# CAS Authentication Attributes
#
cas.attrs.resolve.uid=uid
cas.attrs.resolve.displayName=displayName
cas.attrs.resolve.cn=commonName
cas.attrs.resolve.affiliation=groupMembership

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

##
# CAS Component Aliasing and Selection
#
attributeRepository=stubAttributeRepository
primaryAuthenticationHandler=acceptUsersAuthenticationHandler
primaryPrincipalResolver=personDirectoryPrincipalResolver
themeResolver=serviceThemeResolver
ticketCipherExecutor=noOpCipherExecutor
grantingTicketExpirationPolicy=ticketGrantingTicketExpirationPolicy
authenticationPolicy=anyAuthenticationPolicy
authenticationPolicyFactory=acceptAnyAuthenticationPolicyFactory
authenticationThrottle=neverThrottle
restAuthenticationThrottle=neverThrottle
cookieCipherExecutor=tgcCipherExecutor
cookieValueManager=defaultCookieValueManager
authenticationTransactionManager=defaultAuthenticationTransactionManager
principalElectionStrategy=defaultPrincipalElectionStrategy
auditTrailManager=slf4jAuditTrailManager

##
# Server Properties -> Http
#
# cas.server.http.port=8080
# cas.server.http.protocol=org.apache.coyote.http11.Http11NioProtocol
# cas.server.http.enabled=true

##
# Server Properties -> Ajp
#
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

##
# Server Properties
#
# cas.server.connectionTimeout=20000
# cas.server.name=https://cas.example.org:8443
# cas.server.prefix=https://cas.example.org:8443/cas

##
# Saml Metadata UI Properties
#
# cas.samlMetadataUi.requireValidMetadata=true
# cas.samlMetadataUi.repeatInterval=120000
# cas.samlMetadataUi.startDelay=30000
# cas.samlMetadataUi.resources=classpath:/sp-metadata::classpath:/pub.key,http://md.incommon.org/InCommon/InCommon-metadata.xml::classpath:/inc-md-pub.key
# cas.samlMetadataUi.maxValidity=0
# cas.samlMetadataUi.requireSignedRoot=false
# cas.samlMetadataUi.parameter=entityId

##
# Principal Transformation Properties
#
# cas.principalTransformation.suffix=
# cas.principalTransformation.uppercase=false
# cas.principalTransformation.prefix=

##
# Database Properties
#
# cas.jdbc.showSql=true
# cas.jdbc.genDdl=true

##
# Password Encoder Properties
#
# cas.authn.passwordEncoder.characterEncoding=
# cas.authn.passwordEncoder.encodingAlgorithm=

##
# Person Dir Principal Resolver Properties
#
# cas.authn.x509.principal.principalAttribute=
# cas.authn.x509.principal.returnNull=false

##
# X 509 Properties
#
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

##
# Shiro Config Properties -> Config
#
# cas.authn.shiro.config.location=classpath:shiro.ini

##
# Shiro Authentication Properties
#
# cas.authn.shiro.requiredPermissions=value1,value2,...
# cas.authn.shiro.requiredRoles=value1,value2,...

##
# Ntlm Properties
#
# cas.authn.ntlm.includePattern=
# cas.authn.ntlm.loadBalance=true
# cas.authn.ntlm.domainController=

##
# Trusted Authentication Properties
#
# cas.authn.trusted.principalAttribute=
# cas.authn.trusted.returnNull=false

##
# Person Dir Principal Resolver Properties
#
# cas.authn.wsfed.principal.principalAttribute=
# cas.authn.wsfed.principal.returnNull=false

##
# Ws Federation Properties
#
# cas.authn.wsfed.identityProviderUrl=https://adfs.example.org/adfs/ls/
# cas.authn.wsfed.identityProviderIdentifier=https://adfs.example.org/adfs/services/trust
# cas.authn.wsfed.relyingPartyIdentifier=urn:cas:localhost
# cas.authn.wsfed.attributesType=WSFED
# cas.authn.wsfed.signingCertificateResources=classpath:adfs-signing.crt
# cas.authn.wsfed.tolerance=10000
# cas.authn.wsfed.identityAttribute=upn
# cas.authn.wsfed.attributeResolverEnabled=true

##
# Multifactor Authentication Properties -> GAuth
#
# cas.authn.mfa.gauth.windowSize=3
# cas.authn.mfa.gauth.issuer=
# cas.authn.mfa.gauth.codeDigits=6
# cas.authn.mfa.gauth.label=
# cas.authn.mfa.gauth.timeStepSize=30
# cas.authn.mfa.gauth.rank=0

##
# Multifactor Authentication Properties -> YubiKey
#
# cas.authn.mfa.yubikey.clientId=
# cas.authn.mfa.yubikey.secretKey=
# cas.authn.mfa.yubikey.rank=0

##
# Radius -> Server
#
# cas.authn.mfa.radius.server.retries=3
# cas.authn.mfa.radius.server.nasPortType=-1
# cas.authn.mfa.radius.server.protocol=EAP_MSCHAPv2
# cas.authn.mfa.radius.server.nasRealPort=-1
# cas.authn.mfa.radius.server.nasPortId=-1
# cas.authn.mfa.radius.server.nasIdentifier=-1
# cas.authn.mfa.radius.server.nasPort=-1
# cas.authn.mfa.radius.server.nasIpAddress=
# cas.authn.mfa.radius.server.nasIpv6Address=

##
# Radius -> Client
#
# cas.authn.mfa.radius.client.socketTimeout=0
# cas.authn.mfa.radius.client.sharedSecret=N0Sh@ar3d$ecReT
# cas.authn.mfa.radius.client.authenticationPort=1812
# cas.authn.mfa.radius.client.accountingPort=1813
# cas.authn.mfa.radius.client.inetAddress=localhost

##
# Multifactor Authentication Properties -> Radius
#
# cas.authn.mfa.radius.failoverOnAuthenticationFailure=false
# cas.authn.mfa.radius.failoverOnException=false
# cas.authn.mfa.radius.rank=0

##
# Multifactor Authentication Properties -> Duo
#
# cas.authn.mfa.duo.duoSecretKey=
# cas.authn.mfa.duo.rank=0
# cas.authn.mfa.duo.duoApplicationKey=
# cas.authn.mfa.duo.duoIntegrationKey=
# cas.authn.mfa.duo.duoApiHost=

##
# Multifactor Authentication Properties
#
# cas.authn.mfa.globalPrincipalAttributeNameTriggers=memberOf,eduPersonPrimaryAffiliation
# cas.authn.mfa.requestParameter=authn_method
# cas.authn.mfa.globalFailureMode=CLOSED
# cas.authn.mfa.authenticationContextAttribute=authnContextClass

##
# Authentication Exceptions Properties
#
# cas.authn.exceptions.exceptions=value1,value2,...

##
# Accept Authentication Properties
#
# cas.authn.accept.users=

##
# Saml IdP Properties -> Metadata
#
# cas.authn.samlIdp.metadata.cacheExpirationMinutes=30
# cas.authn.samlIdp.metadata.failFast=true
# cas.authn.samlIdp.metadata.location=file:\etc\cas\saml
# cas.authn.samlIdp.metadata.privateKeyAlgName=RSA
# cas.authn.samlIdp.metadata.requireValidMetadata=true

##
# Saml IdP Properties -> Logout
#
# cas.authn.samlIdp.logout.forceSignedLogoutRequests=true
# cas.authn.samlIdp.logout.singleLogoutCallbacksDisabled=false

##
# Saml IdP Properties -> Response
#
# cas.authn.samlIdp.response.skewAllowance=0
# cas.authn.samlIdp.response.signError=false
# cas.authn.samlIdp.response.overrideSignatureCanonicalizationAlgorithm=
# cas.authn.samlIdp.response.useAttributeFriendlyName=true

##
# Saml IdP Properties
#
# cas.authn.samlIdp.entityId=https://cas.example.org/idp
# cas.authn.samlIdp.hostName=cas.example.org
# cas.authn.samlIdp.scope=example.org

##
# Oidc Properties
#
# cas.authn.oidc.issuer=http://localhost:8080/cas/oidc
# cas.authn.oidc.skew=5
# cas.authn.oidc.jwksFile=file:/keystore.jwks

##
# Password Policy Properties
#
# cas.authn.passwordPolicy.warnAll=false
# cas.authn.passwordPolicy.url=https://password.example.edu/change
# cas.authn.passwordPolicy.displayWarningOnMatch=true
# cas.authn.passwordPolicy.warningDays=30
# cas.authn.passwordPolicy.warningAttributeName=
# cas.authn.passwordPolicy.warningAttributeValue=

##
# Person Dir Principal Resolver Properties
#
# cas.authn.openid.principal.principalAttribute=
# cas.authn.openid.principal.returnNull=false

##
# Open Id Properties
#
# cas.authn.openid.enforceRpId=false

##
# Person Dir Principal Resolver Properties
#
# cas.authn.spnego.principal.principalAttribute=
# cas.authn.spnego.principal.returnNull=false

##
# Spnego Properties
#
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

##
# Jaas Authentication Properties
#
# cas.authn.jaas.realm=CAS
# cas.authn.jaas.kerberosKdcSystemProperty=
# cas.authn.jaas.kerberosRealmSystemProperty=

##
# Stormpath Properties
#
# cas.authn.stormpath.apiKey=
# cas.authn.stormpath.secretkey=
# cas.authn.stormpath.applicationId=

##
# Remote Address Authentication Properties
#
# cas.authn.remoteAddress.ipAddressRange=

##
# Authentication Policy Properties -> Any
#
# cas.authn.policy.any.tryAll=false

##
# Authentication Policy Properties -> Req
#
# cas.authn.policy.req.tryAll=false
# cas.authn.policy.req.handlerName=handlerName

##
# Pac 4j Properties -> Cas
#
# cas.authn.pac4j.cas.loginUrl=
# cas.authn.pac4j.cas.protocol=

##
# Pac4j Properties -> Facebook
#
# cas.authn.pac4j.facebook.fields=
# cas.authn.pac4j.facebook.id=
# cas.authn.pac4j.facebook.secret=
# cas.authn.pac4j.facebook.scope=

##
# Pac4j Properties -> Twitter
#
# cas.authn.pac4j.twitter.id=
# cas.authn.pac4j.twitter.secret=

##
# Pac4j Properties -> Oidc
#
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

##
# Pac4j Properties -> Saml
#
# cas.authn.pac4j.saml.keystorePassword=
# cas.authn.pac4j.saml.privateKeyPassword=
# cas.authn.pac4j.saml.serviceProviderEntityId=
# cas.authn.pac4j.saml.keystorePath=
# cas.authn.pac4j.saml.maximumAuthenticationLifetime=
# cas.authn.pac4j.saml.identityProviderMetadataPath=

##
# Pac4j Properties
#
# cas.authn.pac4j.typedIdUsed=false

##
# Radius Properties -> Server
#
# cas.authn.radius.server.nasPortId=-1
# cas.authn.radius.server.nasRealPort=-1
# cas.authn.radius.server.protocol=EAP_MSCHAPv2
# cas.authn.radius.server.retries=3
# cas.authn.radius.server.nasPortType=-1
# cas.authn.radius.server.nasPort=-1
# cas.authn.radius.server.nasIpAddress=
# cas.authn.radius.server.nasIpv6Address=
# cas.authn.radius.server.nasIdentifier=-1

##
# Radius Properties -> Client
#
# cas.authn.radius.client.authenticationPort=1812
# cas.authn.radius.client.sharedSecret=N0Sh@ar3d$ecReT
# cas.authn.radius.client.socketTimeout=0
# cas.authn.radius.client.inetAddress=localhost
# cas.authn.radius.client.accountingPort=1813

##
# Radius Properties
#
# cas.authn.radius.failoverOnException=false
# cas.authn.radius.failoverOnAuthenticationFailure=false

##
# OAuth Properties -> RefreshToken
#
# cas.authn.oauth.refreshToken.timeToKillInSeconds=2592000

##
# OAuth Properties -> AccessToken
#
# cas.authn.oauth.accessToken.timeToKillInSeconds=7200
# cas.authn.oauth.accessToken.maxTimeToLiveInSeconds=28800

##
# OAuth Properties -> Code
#
# cas.authn.oauth.code.timeToKillInSeconds=30
# cas.authn.oauth.code.numberOfUses=1

##
# File Authentication Properties
#
# cas.authn.file.separator=::
# cas.authn.file.filename=

##
# Reject Authentication Properties
#
# cas.authn.reject.users=user1,user2

##
# Jdbc Authentication Properties -> Query
#
# cas.authn.jdbc.query.sql=

##
# Jdbc Authentication Properties -> Search
#
# cas.authn.jdbc.search.fieldUser=
# cas.authn.jdbc.search.tableUsers=
# cas.authn.jdbc.search.fieldPassword=

##
# Jdbc Authentication Properties -> Encode
#
# cas.authn.jdbc.encode.numberOfIterations=0
# cas.authn.jdbc.encode.numberOfIterationsFieldName=numIterations
# cas.authn.jdbc.encode.saltFieldName=salt
# cas.authn.jdbc.encode.staticSalt=
# cas.authn.jdbc.encode.sql=
# cas.authn.jdbc.encode.algorithmName=
# cas.authn.jdbc.encode.passwordFieldName=password

##
# Mongo Authentication Properties
#
# cas.authn.mongo.mongoHostUri=mongodb://uri
# cas.authn.mongo.usernameAttribute=username
# cas.authn.mongo.attributes=
# cas.authn.mongo.passwordAttribute=password
# cas.authn.mongo.collectionName=users

##
# Throttle Properties -> Failure
#
# cas.authn.throttle.failure.threshold=100
# cas.authn.throttle.failure.code=AUTHENTICATION_FAILED
# cas.authn.throttle.failure.rangeSeconds=60

##
# Throttle Properties
#
# cas.authn.throttle.auditQuery=SELECT AUD_DATE FROM COM_AUDIT_TRAIL WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? AND AUD_ACTION = ? AND APPLIC_CD = ? AND AUD_DATE >= ? ORDER BY AUD_DATE DESC
# cas.authn.throttle.usernameParameter=username
# cas.authn.throttle.startDelay=10000
# cas.authn.throttle.repeatInterval=20000
# cas.authn.throttle.appcode=CAS

##
# Locale Properties
#
# cas.locale.paramName=locale
# cas.locale.defaultValue=en

##
# Sso Properties
#
# cas.sso.missingService=true
# cas.sso.renewedAuthn=true

##
# Ticket Granting Cookie Properties
#
# cas.tgc.path=
# cas.tgc.maxAge=-1
# cas.tgc.domain=
# cas.tgc.signingKey=
# cas.tgc.name=TGC
# cas.tgc.encryptionKey=
# cas.tgc.secure=true
# cas.tgc.rememberMeMaxAge=1209600

##
# Ldap Authorization Properties
#
# cas.ldapAuthz.rolePrefix=ROLE_
# cas.ldapAuthz.allowMultipleResults=false
# cas.ldapAuthz.searchFilter=
# cas.ldapAuthz.baseDn=
# cas.ldapAuthz.roleAttribute=uugid

##
# Logout Properties
#
# cas.logout.followServiceRedirects=false

##
# View Properties -> Cas3
#
# cas.view.cas3.success=protocol/3.0/casServiceValidationSuccess
# cas.view.cas3.failure=protocol/3.0/casServiceValidationFailure
# cas.view.cas3.releaseProtocolAttributes=true

##
# Cas2 -> Proxy
#
# cas.view.cas2.proxy.success=protocol/2.0/casProxySuccessView
# cas.view.cas2.proxy.failure=protocol/2.0/casProxyFailureView

##
# View Properties -> Cas2
#
# cas.view.cas2.success=protocol/2.0/casServiceValidationSuccess
# cas.view.cas2.failure=protocol/2.0/casServiceValidationFailure

##
# Clearpass Properties
#
# cas.clearpass.cacheCredential=false

##
# Message Bundle Properties
#
# cas.messageBundle.encoding=UTF-8
# cas.messageBundle.fallbackSystemLocale=false
# cas.messageBundle.cacheSeconds=180
# cas.messageBundle.useCodeMessage=true
# cas.messageBundle.baseNames=classpath:custom_messagesclasspath:messages

##
# Shibboleth Attribute Resolver Properties
#
# cas.shibAttributeResolver.resources=classpath:/attribute-resolver.xml

##
# Audit Jpa Properties -> Pool
#
# cas.audit.jdbc.pool.suspension=false
# cas.audit.jdbc.pool.minSize=6
# cas.audit.jdbc.pool.maxSize=18
# cas.audit.jdbc.pool.maxIdleTime=1000
# cas.audit.jdbc.pool.maxWait=2000

##
# Audit Properties -> Jdbc
#
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

##
# Audit Properties
#
# cas.audit.auditFormat=DEFAULT
# cas.audit.ignoreAuditFailures=false
# cas.audit.singlelineSeparator=|
# cas.audit.useSingleLine=false
# cas.audit.appCode=CAS

##
# Warning Cookie Properties
#
# cas.warningCookie.path=
# cas.warningCookie.maxAge=-1
# cas.warningCookie.domain=
# cas.warningCookie.name=CASPRIVACY
# cas.warningCookie.secure=true

##
# Host Properties
#
# cas.host.name=

##
# Theme Properties
#
# cas.theme.paramName=theme
# cas.theme.defaultThemeName=cas-theme-default

##
# Acceptable Usage Policy Properties -> Ldap
#
# cas.acceptableUsagePolicy.ldap.searchFilter=cn={0}
# cas.acceptableUsagePolicy.ldap.baseDn=dc=example,dc=org

##
# Acceptable Usage Policy Properties
#
# cas.acceptableUsagePolicy.aupAttributeName=aupAccepted

##
# Jpa Properties -> Pool
#
# cas.events.jpa.database.pool.suspension=false
# cas.events.jpa.database.pool.minSize=6
# cas.events.jpa.database.pool.maxSize=18
# cas.events.jpa.database.pool.maxIdleTime=1000
# cas.events.jpa.database.pool.maxWait=2000

##
# Jpa -> Database
#
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

##
# Events Properties -> Mongodb
#
# cas.events.mongodb.clientUri=
# cas.events.mongodb.dropCollection=false
# cas.events.mongodb.collection=MongoDbCasEventRepository

##
# Events Properties
#
# cas.events.trackGeolocation=false

##
# Http Client Properties -> Truststore
#
# cas.httpClient.truststore.psw=changeit
# cas.httpClient.truststore.file=classpath:/truststore.jks

##
# Http Client Properties
#
# cas.httpClient.connectionTimeout=5000
# cas.httpClient.asyncTimeout=5000
# cas.httpClient.readTimeout=5000

##
# Mongo Service Registry Properties -> Conns
#
# cas.serviceRegistry.mongo.conns.lifetime=60000
# cas.serviceRegistry.mongo.conns.perHost=10

##
# Mongo Service Registry Properties
#
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

##
# Service Registry Config Properties -> Config
#
# cas.serviceRegistry.config.location=

##
# Ldap Service Registry Properties
#
# cas.serviceRegistry.ldap.serviceDefinitionAttribute=description
# cas.serviceRegistry.ldap.idAttribute=uid
# cas.serviceRegistry.ldap.objectClass=casRegisteredService
# cas.serviceRegistry.ldap.baseDn=dc=example,dc=org

##
# Couchbase Service Registry Properties
#
# cas.serviceRegistry.couchbase.nodeSet=localhost:8091
# cas.serviceRegistry.couchbase.password=
# cas.serviceRegistry.couchbase.queryEnabled=true
# cas.serviceRegistry.couchbase.bucket=default
# cas.serviceRegistry.couchbase.timeout=10

##
# Service Registry Jpa Properties -> Pool
#
# cas.serviceRegistry.jpa.pool.suspension=false
# cas.serviceRegistry.jpa.pool.minSize=6
# cas.serviceRegistry.jpa.pool.maxSize=18
# cas.serviceRegistry.jpa.pool.maxIdleTime=1000
# cas.serviceRegistry.jpa.pool.maxWait=2000

##
# Jpa Service Registry Properties
#
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

##
# Service Registry Properties
#
# cas.serviceRegistry.watcherEnabled=true
# cas.serviceRegistry.repeatInterval=120000
# cas.serviceRegistry.startDelay=15000
# cas.serviceRegistry.initFromJson=true

##
# Proxy Ticket Properties
#
# cas.ticket.pt.timeToKillInSeconds=10
# cas.ticket.pt.numberOfUses=1

##
# Ticket Registry Jpa Properties -> Pool
#
# cas.ticket.registry.jpa.pool.suspension=false
# cas.ticket.registry.jpa.pool.minSize=6
# cas.ticket.registry.jpa.pool.maxSize=18
# cas.ticket.registry.jpa.pool.maxIdleTime=1000
# cas.ticket.registry.jpa.pool.maxWait=2000

##
# Jpa Ticket Registry Properties
#
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

##
# Couchbase Ticket Registry Properties
#
# cas.ticket.registry.couchbase.timeout=10
# cas.ticket.registry.couchbase.nodeSet=localhost:8091
# cas.ticket.registry.couchbase.password=
# cas.ticket.registry.couchbase.queryEnabled=true
# cas.ticket.registry.couchbase.bucket=default

##
# Hazelcast Properties -> Cluster
#
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

##
# Hazelcast Properties
#
# cas.ticket.registry.hazelcast.pageSize=500
# cas.ticket.registry.hazelcast.mapName=tickets
# cas.ticket.registry.hazelcast.configLocation=

##
# Infinispan Properties
#
# cas.ticket.registry.infinispan.cacheName=
# cas.ticket.registry.infinispan.configLocation=/infinispan.xml

##
# Ticket Registry Properties -> InMemory
#
# cas.ticket.registry.inMemory.loadFactor=1
# cas.ticket.registry.inMemory.concurrency=20
# cas.ticket.registry.inMemory.initialCapacity=1000

##
# Ticket Registry Properties -> Cleaner
#
# cas.ticket.registry.cleaner.appId=cas-ticket-registry-cleaner
# cas.ticket.registry.cleaner.startDelay=10000
# cas.ticket.registry.cleaner.repeatInterval=60000
# cas.ticket.registry.cleaner.enabled=true

##
# Ehcache Properties
#
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

##
# Ignite Properties -> TicketsCache
#
# cas.ticket.registry.ignite.ticketsCache.writeSynchronizationMode=FULL_SYNC
# cas.ticket.registry.ignite.ticketsCache.atomicityMode=TRANSACTIONAL
# cas.ticket.registry.ignite.ticketsCache.cacheName=TicketsCache
# cas.ticket.registry.ignite.ticketsCache.cacheMode=REPLICATED

##
# Ignite Properties
#
# cas.ticket.registry.ignite.keyAlgorithm=
# cas.ticket.registry.ignite.protocol=
# cas.ticket.registry.ignite.trustStorePassword=
# cas.ticket.registry.ignite.keyStoreType=
# cas.ticket.registry.ignite.keyStoreFilePath=
# cas.ticket.registry.ignite.keyStorePassword=
# cas.ticket.registry.ignite.trustStoreType=
# cas.ticket.registry.ignite.igniteAddresses=localhost:47500
# cas.ticket.registry.ignite.trustStoreFilePath=

##
# Memcached Ticket Registry Properties
#
# cas.ticket.registry.memcached.servers=localhost:11211
# cas.ticket.registry.memcached.locatorType=ARRAY_MOD
# cas.ticket.registry.memcached.failureMode=Redistribute
# cas.ticket.registry.memcached.hashAlgorithm=FNV1_64_HASH

##
# Service Ticket Properties
#
# cas.ticket.st.numberOfUses=1
# cas.ticket.st.maxLength=20
# cas.ticket.st.timeToKillInSeconds=10

##
# Cryptography Properties -> Signing
#
# cas.ticket.signing.key=C@$W3bSecretKey!
# cas.ticket.signing.keySize=512

##
# Ticket Granting Ticket Properties -> Timeout
#
# cas.ticket.tgt.timeout.maxTimeToLiveInSeconds=28800

##
# Ticket Granting Ticket Properties -> ThrottledTimeout
#
# cas.ticket.tgt.throttledTimeout.timeToKillInSeconds=28800
# cas.ticket.tgt.throttledTimeout.timeInBetweenUsesInSeconds=5

##
# Ticket Granting Ticket Properties -> HardTimeout
#
# cas.ticket.tgt.hardTimeout.timeToKillInSeconds=28800

##
# Ticket Granting Ticket Properties
#
# cas.ticket.tgt.maxTimeToLiveInSeconds=28800
# cas.ticket.tgt.maxLength=50
# cas.ticket.tgt.timeToKillInSeconds=7200
# cas.ticket.tgt.onlyTrackMostRecentSession=true

##
# Proxy Granting Ticket Properties
#
# cas.ticket.pgt.maxLength=50

##
# Cryptography Properties -> Encryption
#
# cas.ticket.encryption.keySize=16
# cas.ticket.encryption.key=

##
# Ticket Properties
#
# cas.ticket.alg=AES

##
# Saml Response Properties
#
# cas.samlResponse.ticketidSaml2=false
# cas.samlResponse.skewAllowance=0
# cas.samlResponse.attributeNamespace=http://www.ja-sig.org/products/cas/
# cas.samlResponse.issuer=localhost

##
# Admin Pages Security Properties
#
# cas.adminPagesSecurity.adminRoles=ROLE_ADMIN
# cas.adminPagesSecurity.ip=127\.0\.0\.1
# cas.adminPagesSecurity.loginUrl=
# cas.adminPagesSecurity.service=
# cas.adminPagesSecurity.users=

##
# Monitor Properties -> Warn
#
# cas.monitor.tgt.warn.threshold=10
# cas.monitor.tgt.warn.evictionThreshold=0

##
# Monitor Properties -> Warn
#
# cas.monitor.st.warn.threshold=10
# cas.monitor.st.warn.evictionThreshold=0

##
# Monitor Properties -> Warn
#
# cas.monitor.warn.threshold=10
# cas.monitor.warn.evictionThreshold=0

##
# Monitor Properties -> DataSource
#
# cas.monitor.dataSource.validationQuery=SELECT 1

##
# Monitor Properties
#
# cas.monitor.maxWait=5000
# cas.monitor.freeMemThreshold=10

##
# Maxmind Properties
#
# cas.maxmind.cityDatabase=
# cas.maxmind.countryDatabase=

##
# Management Webapp Properties
#
# cas.mgmt.adminRoles=ROLE_ADMIN
# cas.mgmt.userPropertiesFile=classpath:/user-details.properties
# cas.mgmt.serverName=https://localhost:8443

##
# Google Analytics Properties
#
# cas.googleAnalytics.googleAnalyticsTrackingId=

##
# Http Web Request Properties -> Web
#
# cas.httpWebRequest.web.forceEncoding=true
# cas.httpWebRequest.web.encoding=UTF-8

##
# Http Web Request Properties -> Header
#
# cas.httpWebRequest.header.xframe=false
# cas.httpWebRequest.header.xss=false
# cas.httpWebRequest.header.hsts=false
# cas.httpWebRequest.header.xcontent=false
# cas.httpWebRequest.header.cache=false

##
# Http Web Request Properties
#
# cas.httpWebRequest.allowMultiValueParameters=false
# cas.httpWebRequest.onlyPostParams=username,password
# cas.httpWebRequest.paramsToCheck=ticket,service,renew,gateway,warn,method,target,SAMLart,pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token

##
# Cryptography Properties -> Signing
#
# cas.webflow.signing.key=C@$W3bSecretKey!
# cas.webflow.signing.keySize=512

##
# Webflow Properties -> Session
#
# cas.webflow.session.lockTimeout=30
# cas.webflow.session.compress=false
# cas.webflow.session.maxConversations=5
# cas.webflow.session.storage=true
# cas.webflow.session.hzLocation=classpath:/hazelcast.xml

##
# Cryptography Properties -> Encryption
#
# cas.webflow.encryption.keySize=16
# cas.webflow.encryption.key=

##
# Webflow Properties
#
# cas.webflow.autoconfigure=true
# cas.webflow.alwaysPauseRedirect=false
# cas.webflow.refresh=true
# cas.webflow.redirectSameState=false
# cas.webflow.alg=AES

##
# Person Dir Principal Resolver Properties
#
# cas.personDirectory.principalAttribute=
# cas.personDirectory.returnNull=false

##
# Registered Service Rest Properties
#
# cas.restServices.attributeName=
# cas.restServices.attributeValue=

##
# Slo Properties
#
# cas.slo.disabled=false
# cas.slo.asynchronous=true

##
# Metrics Properties
#
# cas.metrics.loggerName=perfStatsLogger
# cas.metrics.refreshInterval=30

##
# Google Apps Properties
#
# cas.googleApps.publicKeyLocation=file:/etc/cas/public.key
# cas.googleApps.keyAlgorithm=RSA
# cas.googleApps.privateKeyLocation=file:/etc/cas/private.key

```
