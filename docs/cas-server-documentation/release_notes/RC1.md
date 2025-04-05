---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.2.0-RC1 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note 
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, 
statistics or completion of features. To gain confidence in a particular
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. 

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## System Requirements

The JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

## New & Noteworthy

The following items are new improvements and enhancements presented in this release. 

### Spring Boot 3.5

The migration of the entire codebase to Spring Boot `3.5` is ongoing, and at the moment is waiting for the wider ecosystem 
of supporting frameworks and libraries to catch up to changes. We anticipate the work to finalize in the next few 
release candidates and certainly prior to the final release.
   
### Apache Tomcat 11

CAS is now able to run on and requires Apache Tomcat `11.x` and the codebase has been updated to support the latest
version of Apache Tomcat. You can technically still run the CAS server on Apache Tomcat `10.x` but that
is not recommended at all and could very likely break CAS core functionality or cause unexpected behavior.

### OpenRewrite Recipes

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images

A CAS server installation and deployment process can be tuned to build and run 
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints.
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `511` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`. Furthermore, a large number of test categories that group internal unit tests
are now configured to run with parallelism enabled.      
  
### Java 24

As described earlier, the JDK baseline requirement for this CAS release is and **MUST** be JDK `21`. CAS is however
able to compile and run with Java `24` as well with an updated version of the Gradle build tool. Again, remember that 
the baseline requirement will remain unchanged and this is just a preparatory step to ensure CAS is ready for the next version of Java.
 
### Apple Notifications

You can now use APN for sending notifications to [Apple devices](../notifications/Notifications-Configuration-APN.html).

### Multitenancy

CAS can now be configured to run in a [multitenant mode](../multitenancy/Multitenancy-Overview.html).
Each registered tenant with CAS may define its own various policies, allowing the server to compose and combine
multiple configuration options in one single deployment.

### Google Authenticator Multifactor Authentication

Deleting a registered device during Google Authenticator multifactor authentication flows will require the device
removal to be verified with a multifactor authentication challenge. Likewise, registering additional devices
and accounts will also require the user to verify the action with a multifactor authentication challenge.

### SAML2 Delegated Authentication

[SAML2 Delegated Authentication](../integration/Delegate-Authentication-SAML2-Metadata-Aggregate.html) flows 
are modified to support identity provider metadata aggregates, removing the need to register individual identity
providers and simplifying the configuration process.

### Twilio Multifactor Provider

Twilio [can now be used](../mfa/Twilio-Multifactor-Authentication.html) as a standalone multifactor authentication provider.
        
### Amazon Firehose Audits

[AWS Firehose](../audits/Audits-AWS-Firehose.html) can be used to send audit events to Amazon Firehose.
     
### Azure Monitor Application Insights

[Azure Monitor](../monitoring/Configuring-Monitoring-AzureInsights.html) can be used to 
send monitoring events to Azure Monitor Application Insights.

## Other Stuff
              
- Support for SignalFX metrics has been removed, given its deprecation status in Micrometer.
- Apache Kafka integration tests have switched to testing against Apache Kafka `4.0.0`.
- [CAS command-line shell](../installation/Configuring-Commandline-Shell.html) can now run in interactive mode. Furthermore, `exit` and `quit` command functionality is also restored.
- Connection mode for [CosmosDb Service Registry](../services/CosmosDb-Service-Management.html) can now be configured to either `GATEWAY` or `DIRECT`.
- [Duo Security Multifactor Authentication](../mfa/DuoSecurity-Authentication.html) is given the option to track session data in browser storage or ticket registry.
