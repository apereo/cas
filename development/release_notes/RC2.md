---
layout: default
title: CAS - Release Notes
category: Planning
---

# RC2 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set 
you up for unpleasant surprises. A `GA` is simply [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note that CAS 
releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks, statistics or completion of features. To gain 
confidence in a particular release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership) 
and financially support the project at a capacity that best suits your deployment. Note that all development activity 
is performed *almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better 
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support, 
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner. Funding will 
ensure support for the software you rely on and you gain an advantage and say in the way Apereo, and the CAS project at that, runs 
and operates. If you consider your CAS deployment to be a critical part of the identity and access management ecosystem, this is a viable option to consider.

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
cas.version=6.3.0-RC2
```

<div class="alert alert-info">
  <strong>System Requirements</strong><br/>There are no changes to the minimum system/platform requirements for this release.
</div>

## New & Noteworthy

The following items are new improvements and enhancements presented in this release. 

### Amazon SDK v2

CAS is now using the Amazon SDK v2, which effectively upgrades and impacts the functionality for DynamoDb, SNS, SSM, 
S3 and all other features based on Amazon Web Services.

### YubiKey Device Management via REST

YubiKey devices used for [multifactor authentication](../mfa/YubiKey-Authentication.html) can now be externally managed via a REST API.

### Test Coverage via CodeCov

CAS test coverage across all modules in the codebase has now reached `83%` and continues to climb. Additional validation rules are also applied 
to fail all pull requests that fall below this threshold. This area will be closely monitored and improved
as progress is made with the goal of hopefully reaching at least `85%` before the final GA release. Of course, this will not be a blocker for the final release.

### YubiKey Multiple Devices

Multiple YubiKey devices can now be registered with CAS for [multifactor authentication](../mfa/YubiKey-Authentication.html). This ability can be controlled via CAS settings.

![image](https://user-images.githubusercontent.com/1205228/88883051-8b9caa80-d248-11ea-9ad5-487c6071fbc5.png)

![image](https://user-images.githubusercontent.com/1205228/88883117-bf77d000-d248-11ea-98c9-e88088fdd975.png)

<div class="alert alert-warning">
  <strong>WATCH OUT!</strong><br />This may be a breaking change. The underlying data models and repository implementations that manage device records for users are modified to handle a collection of devices per user. This does affect database or filesystem schemas and API calls where a collection is expected instead of a single result.
</div>

### Amazon S3 Service Registry

CAS registered service definitions can now be natively stored in [Amazon S3 buckets](../services/AmazonS3-Service-Management.html).

### Okta SDK v2

CAS is now using the Okta SDK v2 mainly used to handle the integration between CAS and Okta for authentication and attribute resolution.

### Attribute Consent Activation

Activation rules for [Attribute Consent](../integration/Attribute-Release-Consent.html) are re-design to allow per-application overrides of the global policy activation rules. Additional documentation
updates are now present to demonstrate how multiple attribute consent policies may be chained together.

<div class="alert alert-warning">
  <strong>WATCH OUT!</strong><br />This may be a breaking change since the data model for the <code>DefaultRegisteredServiceConsentPolicy</code> has remove the <code>enabled</code>
  field, replacing it with <code>status</code>. Review the documentation to adjust for proper syntax.
</div>

## Other Stuff

- Adjustments to SAML2 metadata resolution to cache to ensure enough capacity for resolved metadata providers. 
- Minor fixes to SQL query execution when pushing CAS audit logs to Oracle databases.
- SAML2 SLO endpoints are now able to redirect to a final logout url after logout operations, if the url is assigned to the SAML service definition.
- The expiration of access tokens is now correctly communicated back to OAuth relying parties, specially if the access token expiration policy is defined per application.
- The handling of authentication requests, set to force CAS to challenge the user credentials, is reviewed and adjusted to ensure such requests can properly honor multifactor authentication flows for qualifying requests per configured triggers. 
- The logout handling strategy is slightly broken apart to introduce a `LogoutRedirectionStrategy`, mainly responsible for handling follow-up redirects to authorized applications/endpoints as appropriate for each authentication protocol.
- Signed SAML authentication requests that embed the signature in URLs are reviewed and adjusted to avoid creating long URLs exceeding browser limits.

## Library Upgrades

- Commons Lang
- Mockito
- DropWizard
- Spring
- Spring Boot
- Amazon SDK
- Spotbugs
- Okta
- Ldaptive
- Inspektr
- Person Directory
- Azure DocumentDb
- Grouper Client
