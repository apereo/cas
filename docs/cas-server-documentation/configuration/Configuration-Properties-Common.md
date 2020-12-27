---
layout: default
title: CAS Common Properties Overview
category: Configuration
---

{% include variables.html %}

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


If multifactor authentication bypass is determined via REST, 
RESTful settings are available [here](#restful-integrations) under the configuration key `${configurationKey}.bypass.rest`.


## Webflow Auto Configuration

Control aspects of webflow that relate to auto-configuration of webflow states, transitions and execution order.

```properties
# ${configurationKey}.order=
```

