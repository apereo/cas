---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC6 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set 
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS 
releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, 
statistics or completion of features. To gain 
confidence in a particular release, it is strongly recommended that you start early by experimenting with 
release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we 
invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership) 
and financially support the project at a capacity that best suits your deployment. Note that all development activity 
is performed *almost exclusively* on a voluntary basis with no expectations, commitments 
or strings attached. Having the financial means to better 
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support, 
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will 
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs 
and operates. If you consider your CAS deployment to be a critical part of the identity and access 
management ecosystem, this is a viable option to consider.

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
cas.version=6.4.0-RC6
```

Alternatively and for new deployments, [CAS Initializr](../installation/WAR-Overlay-Initializr.html) has 
been updated and can also be used
to generate an overlay project template for this release.

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### SAML2 Identity Provider w/ Delegation

[SAML2 authentication requests](../authentication/Configuring-SAML2-Authentication.html) can now be translated and adapted, to 
contribute to the properties of the authentication request sent off to a delegated SAML2 identity provider. For example, a 
requested SAML2 authentication context class can now be passed to an external SAML2 identity provider using
[delegated authentication](../integration/Delegate-Authentication-SAML.html).

### OpenID Connect Dynamic Issuers

Modifying the [OpenID Connect](../protocol/OIDC-Protocol.html) issuer setting to include dynamic path segments is now possible,
where CAS can now allow specification of issuer values that would match `https://sso.example.org/cas/custom/oidc/issuer/value-here`.
All applications can now be given a custom issuer to locate the OpenID Connect discovery document and looks for appropriate URLs.
Applications that have hardcoded the OpenID Connect URL endpoints 
such as `authorization_endpoint` or `token_endpoint`, etc need to revisit and update those values.

### OpenID Connect Relying Parties

The registration of [OpenID Connect relying parties](../authentication/OIDC-Authentication-Clients.html)
is now able to specify and override the ID token's issuer. Furthermore, the registration entry can specify a `kid`
that could be used to locate the proper key in the keystore for crypto operations, mainly useful for key rotation scenarios.

<div class="alert alert-warning"><strong>Be Careful</strong><p>
You should only override the ID Token's issuer when absolutely necessary, and only in special circumstances
after careful evaluation and consideration. Do <strong>NOT</strong> use this setting carelessly as the 
ID token's issuer <strong>MUST ALWAYS</strong> match the identity provider's issuer.
</p></div>

### Actuator Endpoints Documentation

Actuator endpoints that provided by either CAS or third-party frameworks such as Spring Boot or Spring Cloud 
are now automatically documented. [This](../monitoring/Monitoring-Statistics.html) would be a good example.

### Test Coverage

CAS test coverage stands steady at `92%`. The collection of browser/functional tests has now grown to ~156 scenarios.

## Other Stuff
       
- Service registry lookup enforcements to ensure located service definition types can be supported by the enabled protocols.
- [Ignite Ticket Registry](../ticketing/Ignite-Ticket-Registry.html) is able to delete expired tickets correctly.
- Evaluation of [authentication policies](../authentication/Configuring-Authentication-Policy.html) is now 
  able to consider the entire authentication history.
- Person Directory [principal resolution](../authentication/Configuring-Authentication-PrincipalResolution.html) now 
  can receive the credential type and id as query attributes.
- JDBC attribute repositories are able to specify query attributes for advanced `WHERE` clauses in query builders.
- Execution order of [authentication throttlers](../authentication/Configuring-Authentication-Throttling.html) for 
  OAuth and OpenID Connect protocols is now restored and corrected.
- OpenID Connect id tokens can now accurately report back the `auth_time` claim by referring to the original authentication timestamp.
- Scheduled jobs can now be activated conditionally using a regular expression matched against the running CAS node hostname.

## Library Upgrades

- Apache Tomcat
- Twilio
- DropWizard
- PostgreSQL Driver
- Hazelcast Kubernetes
- Azure DocumentDb
- Amazon SDK
- Lettuce
- Hazelcast
- Pac4j
- Spring Session
