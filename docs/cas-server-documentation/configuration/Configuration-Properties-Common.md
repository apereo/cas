---
layout: default
title: CAS Common Properties Overview
category: Configuration
---

{% include variables.html %}

# CAS Common Properties

This document describes a number of suggestions and configuration 
options that apply to and are common amongst a selection of CAS modules and features. 
To see the full list of CAS properties, please [review this guide](Configuration-Properties.html).

## Naming Convention

- Settings and properties that are controlled by the CAS platform directly always begin with the prefix `cas`. All other settings are controlled 
and provided to CAS via other underlying frameworks and may have their own schemas and syntax. **BE CAREFUL** with the distinction.

- Unrecognized properties are rejected by CAS and/or frameworks upon which CAS depends. 
This means if you somehow misspell a property definition or fail to adhere to the dot-notation syntax and such, your setting 
is entirely refused by CAS and likely the feature it controls will never be activated in the way you intend.

## Indexed Settings

CAS settings able to accept multiple values are typically documented with an index, such as `cas.some.setting[0]=value`.
The index `[0]` is meant to be incremented by the adopter to allow for distinct multiple configuration blocks:

```properties
# cas.some.setting[0]=value1
# cas.some.setting[1]=value2
```

## Trust But Verify

If you are unsure about the meaning of a given CAS setting, do **NOT** turn it on without hesitation.
Review the codebase or better yet, [ask questions](/cas/Mailing-Lists.html) to clarify the intended behavior.

<div class="alert alert-info"><strong>Keep It Simple</strong><p>
If you do not know or cannot tell what a setting does, you do not need it.</p></div>

## Time Unit of Measure

All CAS settings that deal with time units, unless noted otherwise,
should support the duration syntax for full clarity on unit of measure:

```bash
"PT20S"     -- parses as "20 seconds"
"PT15M"     -- parses as "15 minutes"
"PT10H"     -- parses as "10 hours"
"P2D"       -- parses as "2 days"
"P2DT3H4M"  -- parses as "2 days, 3 hours and 4 minutes"
```

The native numeric syntax is still supported though you will have to refer to the docs
in each case to learn the exact unit of measure.


## Authentication Throttling

Certain functionality in CAS, such as [OAuth](../installation/OAuth-OpenId-Authentication.html) 
or [REST API](../protocol/REST-Protocol.html), allow you to throttle requests to specific endpoints in addition to the more 
generic authentication throttling functionality applied during the login flow and authentication attempts.

To fully deliver this functionality, it is expected that [authentication throttling](../installation/Configuring-Authentication-Throttling.html) is turned on.

## Authentication Credential Selection

A number of authentication handlers are allowed to determine whether they can operate on the provided credential
and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria
may be one of the following options:

- A regular expression pattern that is tested against the credential identifier
- A fully qualified class name of your own design that looks similar to the below example:

```java
import java.util.function.Predicate;
import org.apereo.cas.authentication.Credential;

public class PredicateExample implements Predicate<Credential> {
    @Override
    public boolean test(final Credential credential) {
        // Examine the credential and return true/false
    }
}
```

- Path to an external Groovy script that looks similar to the below example:

```groovy
import org.apereo.cas.authentication.Credential
import java.util.function.Predicate

class PredicateExample implements Predicate<Credential> {
    @Override
    boolean test(final Credential credential) {
        // test and return result
    }
}
```


## Cassandra Configuration

Control properties that are relevant to Cassandra,
when CAS attempts to establish connections, run queries, etc.

```properties
# ${configurationKey}.keyspace=
# ${configurationKey}.contact-points=localhost:9042
# ${configurationKey}.local-dc=
# ${configurationKey}.consistency-level=ANY|ONE|TWO|THREE|QUORUM|LOCAL_QUORUM|ALL|EACH_QUORUM|LOCAL_SERIAL|SERIAL|LOCAL_ONE
# ${configurationKey}.serial-consistency-level=ANY|ONE|TWO|THREE|QUORUM|LOCAL_QUORUM|ALL|EACH_QUORUM|LOCAL_SERIAL|SERIAL|LOCAL_ONE
# ${configurationKey}.timeout=PT5S
```


## Person Directory Principal Resolution

The following options related to Person Directory support in CAS when it attempts to resolve and build the authenticated principal, given the component's *configuration key*:

```properties
# ${configurationKey}.principal-attribute=uid,sAMAccountName,etc
# ${configurationKey}.return-null=false
# ${configurationKey}.principal-resolution-failure-fatal=false
# ${configurationKey}.use-existing-principal-id=false
# ${configurationKey}.attribute-resolution-enabled=true
# ${configurationKey}.active-attribute-repository-ids=StubRepository,etc
```


## InfluxDb Configuration

The following options related to InfluxDb support in CAS apply equally to a number of CAS components given the component's *configuration key*:

```properties
# ${configurationKey}.url=http://localhost:8086
# ${configurationKey}.username=root
# ${configurationKey}.password=root
# ${configurationKey}.retention-policy=autogen
# ${configurationKey}.drop-database=false
# ${configurationKey}.points-to-flush=100
# ${configurationKey}.batch-interval=PT5S
# ${configurationKey}.consistency-level=ALL
```

## Apache Kafka Configuration

The following options related to Kafka support in CAS apply equally to a number of CAS components given the component's *configuration key*:

```properties
# ${configurationKey}.bootstrap-address=localhost:9092
```

### Apache Kafka Topic Configuration

The following options related to Kafka support in CAS apply equally to a number of CAS components given the component's *configuration key*:

```properties
# ${configurationKey}.name=
# ${configurationKey}.partitions=1
# ${configurationKey}.replicas=1
# ${configurationKey}.compression-type=gzip
# ${configurationKey}.config.key=value
```

## Hazelcast Configuration

The following options related to Hazelcast support in CAS apply equally to a number of CAS components given the component's *configuration key*:

```properties
# ${configurationKey}.cluster.members=123.456.789.000,123.456.789.001
# ${configurationKey}.cluster.instance-name=localhost
# ${configurationKey}.cluster.port=5701

# ${configurationKey}.license-key=
# ${configurationKey}.enable-compression=false
# ${configurationKey}.enable-management-center-scripting=true
```

More advanced Hazelcast configuration settings are listed below, given the component's *configuration key*:

```properties
# ${configurationKey}.cluster.tcpip-enabled=true

# ${configurationKey}.cluster.partition-member-group-type=HOST_AWARE|CUSTOM|PER_MEMBER|ZONE_AWARE|SPI
# ${configurationKey}.cluster.map-merge-policy=PUT_IF_ABSENT|HIGHER_HITS|DISCARD|PASS_THROUGH|EXPIRATION_TIME|LATEST_UPDATE|LATEST_ACCESS

# ${configurationKey}.cluster.eviction-policy=LRU
# ${configurationKey}.cluster.max-no-heartbeat-seconds=300
# ${configurationKey}.cluster.logging-type=slf4j
# ${configurationKey}.cluster.port-auto-increment=true
# ${configurationKey}.cluster.max-size=85
# ${configurationKey}.cluster.backup-count=1
# ${configurationKey}.cluster.async-backup-count=0
# ${configurationKey}.cluster.max-size-policy=USED_HEAP_PERCENTAGE
# ${configurationKey}.cluster.timeout=5

# ${configurationKey}.cluster.local-address=
# ${configurationKey}.cluster.public-address=
# ${configurationKey}.cluster.network-interfaces=1,2,3,4

# ${configurationKey}.cluster.outbound-ports[0]=45000
```

### Static WAN Replication

```properties
# ${configurationKey}.cluster.wan-replication.enabled=false
# ${configurationKey}.cluster.wan-replication.replication-name=CAS

# ${configurationKey}.cluster.wan-replication.targets[0].endpoints=1.2.3.4,4.5.6.7
# ${configurationKey}.cluster.wan-replication.targets[0].publisher-className=com.hazelcast.enterprise.wan.replication.WanBatchReplication
# ${configurationKey}.cluster.wan-replication.targets[0].queue-full-behavior=THROW_EXCEPTION
# ${configurationKey}.cluster.wan-replication.targets[0].acknowledge-type=ACK_ON_OPERATION_COMPLETE
# ${configurationKey}.cluster.wan-replication.targets[0].queue-capacity=10000
# ${configurationKey}.cluster.wan-replication.targets[0].batch-size=500
# ${configurationKey}.cluster.wan-replication.targets[0].snapshot-enabled=false
# ${configurationKey}.cluster.wan-replication.targets[0].batch-maximum-delay-milliseconds=1000
# ${configurationKey}.cluster.wan-replication.targets[0].response-timeout-milliseconds=60000
# ${configurationKey}.cluster.wan-replication.targets[0].executor-thread-count=2

# ${configurationKey}.cluster.wan-replication.targets[0].consistency-check-strategy=NONE|MERKLE_TREES
# ${configurationKey}.cluster.wan-replication.targets[0].cluster-name=
# ${configurationKey}.cluster.wan-replication.targets[0].publisher-id=
# ${configurationKey}.cluster.wan-replication.targets[0].properties=
```

### Multicast Discovery

```properties
# ${configurationKey}.cluster.multicast-trusted-interfaces=
# ${configurationKey}.cluster.multicast-enabled=false
# ${configurationKey}.cluster.multicast-port=
# ${configurationKey}.cluster.multicast-group=
# ${configurationKey}.cluster.multicast-timeout=2
# ${configurationKey}.cluster.multicast-time-to-live=32
```

### AWS EC2 Discovery

```properties
# ${configurationKey}.cluster.discovery.enabled=true

# ${configurationKey}.cluster.discovery.aws.access-ley=
# ${configurationKey}.cluster.discovery.aws.secret-ley=

# ${configurationKey}.cluster.discovery.aws.iam-role=

# ${configurationKey}.cluster.discovery.aws.region=us-east-1
# ${configurationKey}.cluster.discovery.aws.host-header=
# ${configurationKey}.cluster.discovery.aws.security-group-name=
# ${configurationKey}.cluster.discovery.aws.tag-key=
# ${configurationKey}.cluster.discovery.aws.tag-value=
# ${configurationKey}.cluster.discovery.aws.port=-1
# ${configurationKey}.cluster.discovery.aws.connection-timeout-seconds=5
```

### Apache jclouds Discovery

```properties
# ${configurationKey}.cluster.discovery.enabled=true

# ${configurationKey}.cluster.discovery.jclouds.provider=
# ${configurationKey}.cluster.discovery.jclouds.identity=
# ${configurationKey}.cluster.discovery.jclouds.credential=
# ${configurationKey}.cluster.discovery.jclouds.endpoint=
# ${configurationKey}.cluster.discovery.jclouds.zones=
# ${configurationKey}.cluster.discovery.jclouds.regions=
# ${configurationKey}.cluster.discovery.jclouds.tag-keys=
# ${configurationKey}.cluster.discovery.jclouds.tag-values=
# ${configurationKey}.cluster.discovery.jclouds.group=
# ${configurationKey}.cluster.discovery.jclouds.port=-1
# ${configurationKey}.cluster.discovery.jclouds.role-name=
# ${configurationKey}.cluster.discovery.jclouds.credential-path=
```

### Kubernetes Discovery

```properties
# ${configurationKey}.cluster.discovery.enabled=true

# ${configurationKey}.service-dns=
# ${configurationKey}.service-dns-timeout=-1
# ${configurationKey}.service-name=
# ${configurationKey}.service-label-name=
# ${configurationKey}.service-label-value=
# ${configurationKey}.cluster.discovery.kubernetes.namespace=
# ${configurationKey}.resolve-not-ready-addresses=false
# ${configurationKey}.cluster.discovery.kubernetes.kubernetes-master=
# ${configurationKey}.api-token=
```

### Docker Swarm Discovery

```properties
# ${configurationKey}.cluster.discovery.enabled=true

# ${configurationKey}.cluster.discovery.docker-swarm.dns-provider.enabled=true
# ${configurationKey}.cluster.discovery.docker-swarm.dns-provider.service-name=
# ${configurationKey}.cluster.discovery.docker-swarm.dns-provider.service-port=5701
# ${configurationKey}.cluster.discovery.docker-swarm.dns-provider.peer-services=service-a,service-b,etc

# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.enabled=true
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.group-name=
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.group-password=
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.docker-network-names=
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.docker-service-names=
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.docker-service-labels=
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.swarm-mgr-uri=
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.skip-verify-ssl=false
# ${configurationKey}.cluster.discovery.docker-swarm.member-provider.hazelcast-peer-port=5701
```

### Microsoft Azure Discovery

```properties
# ${configurationKey}.cluster.discovery.enabled=true

# ${configurationKey}.cluster.discovery.azure.subscription-id=
# ${configurationKey}.cluster.discovery.azure.client-id=
# ${configurationKey}.cluster.discovery.azure.client-secret=
# ${configurationKey}.cluster.discovery.azure.tenant-id=
# ${configurationKey}.cluster.discovery.azure.cluster-id=
# ${configurationKey}.cluster.discovery.azure.group-name=
```

## RADIUS Configuration

The following options related to RADIUS support in CAS apply equally to a number of CAS components (authentication, etc) 
given the component's *configuration key*.

`server` parameters defines identification values of authenticated service (CAS server), primarily `server.protocol`
 for communication to RADIUS server identified by `client`.

`client` parameters defines values for connecting RADIUS server. 
Parameter `client.inetAddress` has possibility to contain more addresses separated by comma to define failover servers 
when `failoverOnException` is set.   

```properties
# ${configurationKey}.server.nas-port-id=-1
# ${configurationKey}.server.nas-real-port=-1
# ${configurationKey}.server.protocol=EAP_MSCHAPv2
# ${configurationKey}.server.retries=3
# ${configurationKey}.server.nas-port-type=-1
# ${configurationKey}.server.nas-port=-1
# ${configurationKey}.server.nas-ip-address=
# ${configurationKey}.server.nas-ipv6-address=
# ${configurationKey}.server.nas-identifier=-1
# ${configurationKey}.client.authentication-port=1812
# ${configurationKey}.client.shared-secret=N0Sh@ar3d$ecReT
# ${configurationKey}.client.socket-timeout=0
# ${configurationKey}.client.inet-address=localhost
# ${configurationKey}.client.accounting-port=1813
# ${configurationKey}.failover-on-exception=false
# ${configurationKey}.failover-on-authentication-failure=false
```

## CouchDb Configuration

The following options related to CouchDb support in CAS apply equally to a number of CAS components (ticket registries, etc) given the component's *configuration key*:

```properties
# ${configurationKey}.couch-db.url=http://localhost:5984
# ${configurationKey}.couch-db.username=
# ${configurationKey}.couch-db.password=
# ${configurationKey}.couch-db.socket-timeout=10000
# ${configurationKey}.couch-db.connection-timeout=1000
# ${configurationKey}.couch-db.drop-collection=false
# ${configurationKey}.couch-db.max-connections=20
# ${configurationKey}.couch-db.enable-ssl=
# ${configurationKey}.couch-db.relaxed-ssl-settings=
# ${configurationKey}.couch-db.caching=false
# ${configurationKey}.couch-db.max-cache-entries=1000
# ${configurationKey}.couch-db.max-object-size-bytes=8192
# ${configurationKey}.couch-db.use-expect-continue=true
# ${configurationKey}.couch-db.cleanup-idle-connections=true
# ${configurationKey}.couch-db.create-if-not-exists=true
# ${configurationKey}.couch-db.proxy-host=
# ${configurationKey}.couch-db.proxy-port=-1

# Defaults are based on the feature name.
# ${configurationKey}.couch-db.db-name=

# For the few features that can't have update conflicts automatically resolved.
# ${configurationKey}.couch-db.retries=5

# Depending on the feature at hand, CAS may perform some actions asynchronously.
# ${configurationKey}.couch-db.asynchronous=true
```


## DDL Configuration

Note that the default value for Hibernate's DDL setting is `create-drop` which may not be appropriate 
for use in production. Setting the value to `validate` may be more desirable, but any of the following options can be used:

| Type                 | Description
|----------------------|----------------------------------------------------------
| `validate`           | Validate the schema, but make no changes to the database.
| `update`             | Update the schema.
| `create`             | Create the schema, destroying previous data.
| `create-drop`        | Drop the schema at the end of the session.
| `none`               | Do nothing.

Note that during a version migration where any schema has changed `create-drop` will result
in the loss of all data as soon as CAS is started. For transient data like tickets this is probably
not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe
for data, is confirmed to result in invalid database state. `validate` or `none` settings
are likely the only safe options for production use.

For more information on configuration of transaction levels and propagation behaviors,
please review [this guide](http://docs.spring.io/spring-framework/docs/current/javadoc-api/).

## SAML2 Service Provider Integrations

The settings defined for each service provider simply attempt to automate the creation of 
a [SAML service definition](../installation/Configuring-SAML2-Authentication.html#saml-services) and nothing more. If you find the 
applicable settings lack in certain areas, it is best to fall back onto the native configuration strategy for registering 
SAML service providers with CAS which would depend on your service registry of choice.

Each SAML service provider supports the following settings:

| Name                  |  Description
|-----------------------|---------------------------------------------------------------------------
| `metadata`            | Location of metadata for the service provider (i.e URL, path, etc)
| `name`                | The name of the service provider registered in the service registry.
| `description`         | The description of the service provider registered in the service registry.
| `nameIdAttribute`     | Attribute to use when generating name ids for this service provider.
| `nameIdFormat`        | The forced NameID Format identifier (i.e. `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress`).
| `attributes`          | Attributes to release to the service provider, which may virtually be mapped and renamed.
| `signatureLocation`   | Signature location to verify metadata.
| `entityIds`           | List of entity ids allowed for this service provider.
| `signResponses`       | Indicate whether responses should be signed. Default is `true`.
| `signAssertions`      | Indicate whether assertions should be signed. Default is `false`.

The only required setting that would activate the automatic configuration for a service provider is the presence and definition of metadata. All other settings are optional. 

The following options apply equally to SAML2 service provider integrations, given the provider's *configuration key*:

```properties
# ${configurationKey}.metadata=/etc/cas/saml/dropbox.xml
# ${configurationKey}.name=SP Name
# ${configurationKey}.description=SP Integration
# ${configurationKey}.name-id-attribute=mail
# ${configurationKey}.name-id-format=
# ${configurationKey}.signature-location=
# ${configurationKey}.attributes=
# ${configurationKey}.entity-ids=
# ${configurationKey}.sign-responses=
# ${configurationKey}.sign-assertions=
```

## Multifactor Authentication Providers

All configurable multifactor authentication providers have these base properties available given the provider's *configuration key*:

```properties
# ${configurationKey}.rank=
# ${configurationKey}.id=
# ${configurationKey}.name=
# ${configurationKey}.failure-mode=UNDEFINED
```

## Multifactor Authentication Bypass

The following bypass options apply equally to multifactor authentication providers given the provider's *configuration key*:

```properties
# ${configurationKey}.bypass.principal-attribute-name=bypass|skip
# ${configurationKey}.bypass.principal-attribute-value=true|enabled.+

# ${configurationKey}.bypass.authentication-attribute-name=bypass|skip
# ${configurationKey}.bypass.authentication-attribute-value=allowed.+|enabled.+

# ${configurationKey}.bypass.authentication-handler-name=AcceptUsers.+
# ${configurationKey}.bypass.authentication-method-name=LdapAuthentication.+

# ${configurationKey}.bypass.credential-class-type=UsernamePassword.+

# ${configurationKey}.bypass.http-request-remote-address=127.+|example.*
# ${configurationKey}.bypass.http-request-headers=header-X-.+|header-Y-.+

# ${configurationKey}.bypass.groovy.location=file:/etc/cas/config/mfa-bypass.groovy
```

If multifactor authentication bypass is determined via REST, 
RESTful settings are available [here](#restful-integrations) under the configuration key `${configurationKey}.bypass.rest`.

## Couchbase Integration Settings

The following options are shared and apply when CAS is configured to integrate with Couchbase (i.e ticket registry, etc), given the provider's *configuration key*:

```properties
# ${configurationKey}.addresses[0]=localhost
# ${configurationKey}.cluster-username=
# ${configurationKey}.cluster-password= 

# ${configurationKey}.bucket=testbucket    

# ${configurationKey}.connection-timeout=PT60S
# ${configurationKey}.search-timeout=PT30S
# ${configurationKey}.query-timeout=PT30S
# ${configurationKey}.view-timeout=PT30S
# ${configurationKey}.kv-timeout=PT30S 
# ${configurationKey}.max-http-connections=PT30S
# ${configurationKey}.idle-connection-timeout=PT30S
# ${configurationKey}.query-threshold=PT30S
# ${configurationKey}.scan-consistency=NOT_BOUNDED|REQUEST_PLUS
```


## Memcached Integration Settings

The following  options are shared and apply when CAS is configured to integrate with memcached (i.e ticket registry, etc), given the provider's *configuration key*:

```properties
# ${configurationKey}.memcached.servers=localhost:11211
# ${configurationKey}.memcached.locator-type=ARRAY_MOD
# ${configurationKey}.memcached.failure-mode=Redistribute
# ${configurationKey}.memcached.hash-algorithm=FNV1_64_HASH
# ${configurationKey}.memcached.should-optimize=false
# ${configurationKey}.memcached.daemon=true
# ${configurationKey}.memcached.max-reconnect-delay=-1
# ${configurationKey}.memcached.use-nagle-algorithm=false
# ${configurationKey}.memcached.shutdown-timeout-seconds=-1
# ${configurationKey}.memcached.op-timeout=-1
# ${configurationKey}.memcached.timeout-exception-threshold=2
# ${configurationKey}.memcached.max-total=20
# ${configurationKey}.memcached.max-idle=8
# ${configurationKey}.memcached.min-idle=0

# ${configurationKey}.memcached.transcoder=KRYO|SERIAL|WHALIN|WHALINV1
# ${configurationKey}.memcached.transcoder-compression-threshold=16384
# ${configurationKey}.memcached.kryo-auto-reset=false
# ${configurationKey}.memcached.kryo-objects-by-reference=false
# ${configurationKey}.memcached.kryo-registration-required=false
```

## Webflow Auto Configuration

Control aspects of webflow that relate to auto-configuration of webflow states, transitions and execution order.

```properties
# ${configurationKey}.order=
``` 

## Delegated Authentication Settings

The following options are shared and apply when CAS is configured to delegate authentication 
to an external provider such as Yahoo, given the provider's *configuration key*:

```properties
# ${configurationKey}.id=
# ${configurationKey}.secret=
# ${configurationKey}.client-name=My Provider
# ${configurationKey}.auto-redirect=false
# ${configurationKey}.css-class=
# ${configurationKey}.callback-url=
# ${configurationKey}.principal-attribute-id=
# ${configurationKey}.enabled=true
# ${configurationKey}.callback-url-type=PATH_PARAMETER|QUERY_PARAMETER|NONE

```

The following types are supported with callback URL resolution:

| Type               | Description              
|--------------------|--------------------------------------------------------------------------------------
| `PATH_PARAMETER`   | When constructing a callback URL, client name is added to the url as a path parameter.
| `QUERY_PARAMETER`  | When constructing a callback URL, client name is added to the url as a query parameter.
| `NONE`             | No client name is added to the url.

### Delegated Authentication OpenID Connect Settings

The following options are shared and apply when CAS is configured to delegate authentication 
to an external OpenID Connect provider such as Azure AD, given the provider's *configuration key*:

```properties
# ${configurationKey}.discovery-uri=
# ${configurationKey}.logout-url=
# ${configurationKey}.max-clock-skew=
# ${configurationKey}.scope=
# ${configurationKey}.use-nonce=false
# ${configurationKey}.disable-nonce=false
# ${configurationKey}.preferred-jws-algorithm=
# ${configurationKey}.response-mode=
# ${configurationKey}.response-type=
# ${configurationKey}.custom-params.param1=value1
# ${configurationKey}.read-timeout=PT5S
# ${configurationKey}.connect-timeout=PT5S
# ${configurationKey}.expire-session-with-token=false
# ${configurationKey}.token-expiration-advance=0
```

## LDAP Connection Settings

The following  options apply  to features that integrate with an LDAP server (i.e. authentication, attribute resolution, etc) given the provider's *configuration key*:

```properties
# ${configurationKey}.ldap-url=ldaps://ldap1.example.edu ldaps://ldap2.example.edu
# ${configurationKey}.bind-dn=cn=-directory -manager,dc=example,dc=org
# ${configurationKey}.bind-credential=Password

# ${configurationKey}.pool-passivator=NONE|BIND
# ${configurationKey}.connection-strategy=
# ${configurationKey}.connect-timeout=PT5S
# ${configurationKey}.trust-certificates=
# ${configurationKey}.trust-store=
# ${configurationKey}.trust-store-password=
# ${configurationKey}.trust-store-type=JKS|JCEKS|PKCS12
# ${configurationKey}.keystore=
# ${configurationKey}.keystore-password=
# ${configurationKey}.keystore-type=JKS|JCEKS|PKCS12
# ${configurationKey}.disable-pooling=false
# ${configurationKey}.min-pool-size=3
# ${configurationKey}.max-pool-size=10
# ${configurationKey}.validate-on-checkout=true
# ${configurationKey}.validate-periodically=true
# ${configurationKey}.validate-period=PT5M
# ${configurationKey}.validate-timeout=PT5S
# ${configurationKey}.fail-fast=true
# ${configurationKey}.idle-time=PT10M
# ${configurationKey}.prune-period=PT2H
# ${configurationKey}.block-wait-time=PT3S

# ${configurationKey}.use-start-tls=false
# ${configurationKey}.response-timeout=PT5S
# ${configurationKey}.allow-multiple-dns=false
# ${configurationKey}.allow-multiple-entries=false
# ${configurationKey}.follow-referrals=false
# ${configurationKey}.binary-attributes=objectGUID,someOtherAttribute
# ${configurationKey}.name=
```

### Connection Initialization

LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters:

| Behavior                               | Description              
|----------------------------------------|-------------------------------------------------------------------
| `bindDn`/`bindCredential` provided     | Use the provided credentials to bind when initializing connections.
| `bindDn`/`bindCredential` set to `*`   | Use a fast-bind strategy to initialize the pool.   
| `bindDn`/`bindCredential` set to blank | Skip connection initializing; perform operations anonymously.
| SASL mechanism provided                | Use the given SASL mechanism to bind when initializing connections.

### Passivators

The following options can be used to passivate objects when they are checked back into the LDAP connection pool:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `NONE`                  | No passivation takes place.
| `BIND`                  | The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.

#### Why Passivators?

You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it's no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request.

Furthermore if you are seeing errors in the logs that resemble a *<Operation exception encountered, reopening connection>* type of message, this usually is an indication that the connection pool's validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session's timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session's timeout.

### Connection Strategies

If multiple URLs are provided as the LDAP url, this describes how each URL will be processed.

| Provider              | Description              
|-----------------------|-----------------------------------------------------------------------------------------------
| `ACTIVE_PASSIVE`      | First LDAP will be used for every request unless it fails and then the next shall be used.    
| `ROUND_ROBIN`         | For each new connection the next url in the list will be used.      
| `RANDOM`              | For each new connection a random LDAP url will be selected.
| `DNS_SRV`             | LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.  

### LDAP SASL Mechanisms

```properties
# ${configurationKey}.sasl-mechanism=GSSAPI|DIGEST_MD5|CRAM_MD5|EXTERNAL
# ${configurationKey}.sasl-realm=EXAMPLE.COM
# ${configurationKey}.sasl-authorization-id=
# ${configurationKey}.sasl-mutual-auth=
# ${configurationKey}.sasl-quality-of-protection=
# ${configurationKey}.sasl-security-strength=

```

### LDAP Connection Validators

The following LDAP validators can be used to test connection health status:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `NONE`                  | No validation takes place.
| `SEARCH`                | Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero.
| `COMPARE`               | Validates a connection is healthy by performing a compare operation.

```properties
# ${configurationKey}.validator.type=NONE|SEARCH|COMPARE
# ${configurationKey}.validator.base-dn=
# ${configurationKey}.validator.search-filter=(object-class=*)
# ${configurationKey}.validator.scope=OBJECT|ONELEVEL|SUBTREE
# ${configurationKey}.validator.attribute-name=objectClass
# ${configurationKey}.validator.attribute-value=top
# ${configurationKey}.validator.dn=

```

### LDAP SSL Hostname Verification

The following LDAP validators can be used to test connection health status:

| Type                    | Description
|-------------------------|------------------------------------
| `DEFAULT`               | Default option to enable and force hostname verification of the LDAP SSL configuration.
| `ANY`                   | Skip and ignore the hostname verification of the LDAP SSL configuration.

```properties
#${configurationKey}.hostname-verifier=DEFAULT|ANY
```

### LDAP SSL Trust Managers

Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, 
and for deciding whether credentials presented by a peer should be accepted.

| Type                    | Description
|-------------------------|---------------------------------------------------------------------------------------------
| `DEFAULT`               | Enable and force the default JVM trust managers.
| `ANY`                   | Trust any client or server.

```properties
#${configurationKey}.trust-manager=DEFAULT|ANY
```

### LDAP Types

A number of components/features in CAS allow you to explicitly indicate a `type` for the LDAP server, specially in cases where CAS needs to update an attribute, etc in LDAP (i.e. consent, password management, etc). The relevant setting would be:

```properties
#${configurationKey}.type=AD|FreeIPA|EDirectory|Generic
```

The following types are supported:

| Type                    | Description
|-------------------------|--------------------------------------------------
| `AD`                                                     | Active Directory.
| `FreeIPA`                                    | FreeIPA Directory Server.
| `EDirectory`                         | NetIQ eDirectory.
| `GENERIC`                              | All other directory servers (i.e OpenLDAP, etc).

### LDAP Authentication/Search Settings

In addition to common LDAP connection settings above, there are cases where CAS simply need to execute 
authenticate against an LDAP server to fetch an account or set of attributes or execute a search query in general. 
The following  options apply  given the provider's *configuration key*:

**Note:** Failure to specify adequate properties such as `type`, `ldapUrl`, etc will simply deactivate LDAP  altogether silently.

```properties
# ${configurationKey}.type=AD|AUTHENTICATED|DIRECT|ANONYMOUS

# ${configurationKey}.base-dn=dc=example,dc=org
# ${configurationKey}.subtree-search=true
# ${configurationKey}.search-filter=cn={user}
# ${configurationKey}.page-size=0

# ${configurationKey}.enhance-with-entry-resolver=true
# ${configurationKey}.deref-aliases=NEVER|SEARCHING|FINDING|ALWAYS
# ${configurationKey}.dn-format=uid=%s,ou=people,dc=example,dc=org
# ${configurationKey}.principal-attribute-password=password

```

The following authentication types are supported:

| Type                    | Description                            
|-------------------------|----------------------------------------------------------------------------------------------------
| `AD`                    | Active Directory - Users authenticate with `sAMAccountName` typically using a DN format.     
| `AUTHENTICATED`         | Manager bind/search type of authentication. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it.
| `DIRECT`                | Compute user DN from a format string and perform simple bind. This is relevant when no search is required to compute the DN needed for a bind operation. This option is useful when all users are under a single branch in the directory, e.g. `ou=Users,dc=example,dc=org`, or the username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=exmaple,dc=org`
| `ANONYMOUS`             | Similar semantics as `AUTHENTICATED` except no `bindDn` and `bindCredential` may be specified to initialize the connection. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it.

### LDAP Search Entry Handlers

```properties
# ${configurationKey}.search-entry-handlers[0].type=

# ${configurationKey}.search-entry-handlers[0].case-change.dn-case-change=NONE|LOWER|UPPER
# ${configurationKey}.search-entry-handlers[0].case-change.attribute-name-case-change=NONE|LOWER|UPPER
# ${configurationKey}.search-entry-handlers[0].case-change.attribute-value-case-change=NONE|LOWER|UPPER
# ${configurationKey}.search-entry-handlers[0].case-change.attribute-names=

# ${configurationKey}.search-entry-handlers[0].dn-attribute.dn-attribute-name=entryDN
# ${configurationKey}.search-entry-handlers[0].dn-attribute.add-if-exists=false

# ${configurationKey}.search-entry-handlers[0].primary-group-id.group-filter=(&(object-class=group)(object-sid={0}))
# ${configurationKey}.search-entry-handlers[0].primary-group-id.base-dn=

# ${configurationKey}.search-entry-handlers[0].merge-attribute.merge-attribute-name=
# ${configurationKey}.search-entry-handlers[0].merge-attribute.attribute-names=

# ${configurationKey}.search-entry-handlers[0].recursive.search-attribute=
# ${configurationKey}.search-entry-handlers[0].recursive.merge-attributes=

```

The following types are supported:

| Type                    | Description                            
|-------------------------|----------------------------------------------------------------------------------------------------
| `CASE_CHANGE` | Provides the ability to modify the case of search entry DNs, attribute names, and attribute values.
| `DN_ATTRIBUTE_ENTRY` | Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020.
| `MERGE` | Merges the values of one or more attributes into a single attribute.
| `OBJECT_GUID` | Handles the `objectGUID` attribute fetching and conversion. 
| `OBJECT_SID` | Handles the `objectSid` attribute fetching and conversion. 
| `PRIMARY_GROUP` | Constructs the primary group SID and then searches for that group and puts it's DN in the 'memberOf' attribute of the original search entry. 
| `RANGE_ENTRY` |  Rewrites attributes returned from Active Directory to include all values by performing additional searches.
| `RECURSIVE_ENTRY` | This recursively searches based on a supplied attribute and merges those results into the original entry.

### LDAP Multiple Base DNs

There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating 
the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character.
The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs 
are found. Otherwise the first DN found is returned.

```properties
# ${configurationKey}.base-dn=subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net
```
