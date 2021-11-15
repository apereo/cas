---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC2 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting
for a `GA` release is only going to set you up for unpleasant surprises. A `GA`
is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS
releases are *strictly* time-based releases; they are not scheduled or based on
specific benchmarks, statistics or completion of features. To gain confidence in
a particular release, it is strongly recommended that you start early by
experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we
invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your
deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings
attached. Having the financial means to better sustain engineering activities will allow
the developer community to allocate *dedicated and committed* time for long-term
support, maintenance and release planning, especially when it comes to addressing
critical and security issues in a timely manner. Funding will ensure support for
the software you rely on and you gain an advantage and say in the way Apereo, and
the CAS project at that, runs and operates. If you consider your CAS deployment to
be a critical part of the identity and access management ecosystem, this is a viable option to consider.

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
cas.version=6.5.0-RC2
```

<div class="alert alert-info">
<strong>System Requirements</strong><br/>There are no changes to the 
minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.
     
### CGLib Proxies

All CAS auto-configuration components are now adjusted to use JDK dynamic proxies rather than CGLib proxies. JDK Dynamic proxies 
are generally the preferred choice and offer several advantages over CGLib proxies. The removal of CGLib proxies should assist 
with native CAS builds in the future using the likes of GraalVM. One side-effect (or intended benefit, if you prefer) of 
this move is that `@Configuration` classes that are home to CAS `@Bean` methods can no longer invoke 
each other directly since `proxyBeanMethods` is now forcefully turned off. Rather, references 
must be passed and injected dynamically. This has led to a significant cleanup effort to 
ensure all field injections are removed and all circular dependencies and injections are re-adjusted.
                                        
Given the number of variations and combinations across modules, it's quite likely that there will be accidental 
mishaps and misconfigurations at runtime leading to circular dependencies 
issues. Additional test scenarios and scripts will continuously
be added to ensure validity of as many combinations as possible.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to add additional scenarios. At this point, there are 
approximately `200` test scenarios and we'll continue to add more in the coming releases.

### Global Groovy Access Strategy

Access strategy and authorization decision can be carried [using a Groovy script](../services/Configuring-Service-Access-Strategy.html) 
for all services and applications.
  
### SAML2 Attribute Queries
              
Various improvements to SAML2 attribute query requests:
       
- SAML2 attribute query responses are now encoded with the correct bindings and content type headers.
- SAML2 subject `NameID`s should now correctly resolve for SAML2 attribute queries that contain encrypted `NameID` elements.
- SAML2 attribute query tracking tokens now gain their own expiration policy in the CAS configuration schema.
- The expiration policies of the SAML2 attribute query token and its linked ticket-granting ticket are now taken into account. 
- Attributes released in response to an attribute query pass through relevant attribute release policies for the service provider.
  
### Attribute Repository States

An [attribute repository](../integration/Attribute-Resolution.html) can be put into standby mode; this means the repository 
configuration is processed and registered into the application runtime and is 
effectively available as a Spring `@Bean`. However, it is not registered into 
the resolution plan and can only be called and invoked explicitly when needed.

### Bootstrap Themes
       
There is now additional, optional support for [Bootstrap](http://getbootstrap.com "Bootstrap") themes. The collection of themes
that ship with CAS now include a special `twbs` theme that is based on Bootstrap support.
  
### Docker Integration Tests

Several Docker images that are used for integration tests are now updated to use more recent versions. Those are:

- MongoDb
- Apache Cassandra
- AWS Localstack
- DynamoDb 
- InfluxDb
- MariaDb
- MySQL
- MS SQL Server
- Redis
- Apache CouchDb

### Reusing Chained Attributes

Attribute release policies, when grouped and ordered together in a chain are now able to reuse and build on top of previously-selected
attributes, tagged for release by previous policies in the same chain. For example, a typical scenario might be that the first attribute
release policy in the first releases attribute `A1` and the second attribute policy creates attribute `A2`  whose 
value is constructed based on a dynamic construct such as `A1 + '-Hello'`.

## Other Stuff
     
- Delegated identity providers are now allowed to go through a [post-processing phase](../integration/Delegate-Authentication-PostProcessing.html).
- Cipher executor components, particularly those that operate on binary data, are now allowed to use larger key sizes (`128`, `256`, etc.) for encryption operations.
- Configuration schema is now updated to support multiple MongoDb instances for monitoring and health indication. 
- [MongoDb Ticket Registry](../ticketing/MongoDb-Ticket-Registry.html) is adjusted to work well with timeout expiration policy assigned to ticket-granting tickets. 
- [Duo Security Universal Prompt](../mfa/DuoSecurity-Authentication.html) can 
  handle [impersonation requests](../authentication/Surrogate-Authentication.html) that require user selection.
- Bypassing OAuth or OpenID Connect approval screen can now be done globally via CAS configuration.

## Library Upgrades
            
- Pac4j
- Lombok
- Okta SDK
- Spring Data
- PostgreSQL Driver
- Infinispan
- Micrometer
- Hibernate
- Bootstrap
- Spring Boot
- MySQL Driver
- Amazon SDK
- Twilio
- Spring Security
- Spring Session
- Nimbus OIDC
- Spring
