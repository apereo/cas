---
layout: default
title: CAS - Audit Configuration
---

# Audits
CAS uses the [Inspektr framework](https://github.com/Jasig/inspektr) for auditing purposes
and statistics. The Inspektr project allows for non-intrusive auditing and logging of the
coarse-grained execution paths e.g. Spring-managed beans method executions by using annotations
and Spring-managed `@Aspect`-style aspects.

## Configuration
CAS server auto-configures all the relevant Inspektr components with Spring `@Configuration` class. All the available configuration
options that are injected to Inspektr classes are available to deployers via relevant properties in `application.properties`

### File-based Audits
By default, audit messages appear in log files via the `Slf4jLoggingAuditTrailManager` and are routed to
a `cas_audit.log` file defined in the `log4j2.xml` configuration as well as the usual `cas.log` file.

```properties
# cas.audit.singleline=true
# cas.audit.singleline.separator=|
# cas.audit.format=DEFAULT
# cas.audit.ignore.failures=false
# cas.audit.appcode=CAS
```

#### Sample Log Output
```bash
WHO: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: supplied credentials: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
ACTION: AUTHENTICATION_SUCCESS
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22

WHO: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: TGT-9-qj2jZKQUmu1gQvXNf7tXQOJPOtROvOuvYAxybhZiVrdZ6pCUwW-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22
```

### Database Audits
If you intend to use a database
for auditing functionality, adjust the audit manager mapping in `application.properties` to match the configuration below:

```properties
#CAS components mappings
auditTrailManager=jdbcAuditTrailManager
```

and also include this import in local `deployerConfigContext.xml`:

```xml
<import resource="classpath:inspektr-jdbc-audit-config.xml" />
```

#### Database Audit Configuration
Configuration consists of:

```properties
#cas.audit.max.agedays=
#cas.audit.database.dialect=
#cas.audit.database.batchSize=
#cas.audit.database.ddl.auto=
#cas.audit.database.gen.ddl=
#cas.audit.database.show.sql=
#cas.audit.database.driverClass=
#cas.audit.database.url=
#cas.audit.database.user=
#cas.audit.database.password=
#cas.audit.database.pool.maxSize=
#cas.audit.database.pool.maxIdleTime=
#cas.audit.database.pool.maxWait=
```
