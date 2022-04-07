---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC1 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA`
is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS releases are *strictly* time-based
releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain confidence in a particular
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs and
operates. If you consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option to consider.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## Overlay

In the `gradle.properties` of the [CAS WAR Overlay](../installation/WAR-Overlay-Installation.html), adjust the following setting:

```properties
cas.version=6.6.0-RC1
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
  
### Refresh Requests

The construction of Spring beans (as defined by the `@Bean` annotation) managed via auto-configuration classes has been enhanced to support [*configuration refresh* requests](../configuration/Configuration-Management-Reload.html) specially when beans are activated conditionally using a property value via the likes of `@ConditionalOnProperty`. This allows a bean to remain in a disabled state using a JDK dynamic proxy so that it can later on be replaced with a real implementation when/if configuration is refreshed. 

### Feature Toggles

Modest support for [feature toggles](../configuration/Configuration-Management-Extensions.html) is now available to allow multiple competing modules to be present the same web application where they can be toggled on and off va dedicated flags and settings. This could previously be handled via excluding specific CAS `@Configuration` classes which was risky and prone to breakage during upgrades as such classes are always considered an implementation detail. Starting with this release candidate, a specific feature can be enabled or disabled via a dedicated setting allowing all configuration modules and components to react accordingly without one having to know the internal details.

Please note that feature toggles are not yet supported by all CAS modules; this is a large effort and will likely require several more releases before this capability is finalized. Also, remember that feature toggles do require a restart.
    
### OpenID Connect - JMeter Performance Testing

[JMeter scripts](../high_availability/Performance-Testing-JMeter.html) used to run performance tests for CAS running as an [OpenID Connect Provider](../authentication/OIDC-Authentication.html) are now integrated with the CAS CI system to ensure the test script and the functionality under test continues to run smoothly. 

### Integration Tests

Several Docker images used for integration tests are now updated to their latest versions. These include:

- AWS Localstack
- Apache Cassandra
- Apache Couchbase
- AWS DynamoDb
- InfluxDb
- MariaDb
- MongoDb
- Apereo CAS
- Microsoft SQL Server
- MySQL
- PostgreSQL
- Spring Data
- Amazon SDK
- Apache Log4j
- Hazelcast
   
### HAL Explorer
 
Actuator endpoints exposed to or controlled by CAS can now be observed and 
managed via the [HAL Explorer](https://github.com/toedter/hal-explorer):

![](https://user-images.githubusercontent.com/1205228/155877447-c993b3d6-1e14-4dc8-8154-662d53ee2206.png)

As part of this change, CAS allows one to use version agnostic URLs for webjars. Using jQuery as an example, 
adding `/webjars/jquery/jquery.min.js` results in `/webjars/jquery/x.y.z/jquery.min.js` where `x.y.z` is the webjar version.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to add additional scenarios. At this point, there are approximately `255` test scenarios and we'll continue to add more in the coming releases. Test coverage has also slightly improved and now stands at `93%`. 
 
### Static Attribute Release Policy

A special [attribute release policy](../integration/Attribute-Release-Policies.html) is now available to authorize the release of static attributes to applications. Using this policy, static attribute values no longer need to be constructed at the attribute repository level, and their definition can be confined within the context of the application bound to receive attributes.

## Other Stuff
      
- Minor fixes to how `locale` changes are remembered and tracked across different browser sessions.  
- System properties and environment variables can now override properties that are found in CAS configuration files.
- Internal rewiring of CAS components, bean processors and event listeners to improve support for building native images.
- [CAS Initializr](../installation/WAR-Overlay-Initializr.html) is slightly reorganized to move Gradle tasks into a separate file.
- Server-side automatic redirects used for [delegated authentication](../integration/Delegate-Authentication.html) can now correctly recognize an existing SSO session.
- Internal improvements to the Gradle build to remove deprecated elements, in early preparation for Gradle 9.
- [CAS Initializr](../installation/WAR-Overlay-Initializr.html) is now able to produce WAR Overlay projects that take advantage of Gradle's support for BOMs, making it more predictable to handle dependency management issues and conflicts.
- [OpenID Connect support](../protocol/OIDC-Protocol.html) for handling signed authentication requests using the `request` parameter is now included.
- Signing [OpenID Connect](../protocol/OIDC-Protocol.html) ID tokens or profile payloads can now correctly filter signing algorithms based on CAS configuration. 

## Library Upgrades
     
- Spring Boot
- Aspectj
- Spring Data CosmosDb
- Spring Cloud AWS
- Bucket4j
- MongoDb Driver
- CosmosDb
- Spring Security
- Kryo
- Gson
- Nimbus OIDC
- Groovy
- Gradle
- Thymeleaf Dialect
- Apache Tomcat
- PostgreSQL
- Jose4j
- Amazon
- Okta
- Micrometer
- Grouper
