---
layout: default
title: CAS - Vulnerability Response
---

# Security Vulnerability Response

Members involved in [CAS application security](/cas/Mailing-Lists.html):

- Proactively work to improve the security of CAS focusing on the Apereo CAS server, the protocols and various CAS clients.
- Respond to potential vulnerabilities and create, maintain, and execute on vulnerability triage and notification policy, fielding handoffs from the Apereo security and otherwise. 
- Issue vulnerability reports and work to coordinate workarounds and fix responses to security concerns that arise.
- Produce artifacts that help potential CAS adopters to evaluate the security of CAS both as open source product and as they intend to locally implement the product. This includes threat modeling, data flow diagrams, etc.
- Create and maintain recommendations on good practices for CAS implementation around hardening, configuration, failing safe, security by default, etc.

To review the old archives of the application security working group, [please see this guide](https://wiki.jasig.org/display/CAS/CAS+AppSec+Working+Group).

## Response Model

- Security issues are [privately reported to Apereo](/cas/Mailing-Lists.html).
- Project members acknowledge the issue and ascertain its merits. All discussion **MUST** happen privately on the [appropriate mailing list](/cas/Mailing-Lists.html).
- A fix is planned, produced and made available privately as a *direct commit* to the codebase.
- Project members or the original reporter **SHOULD** verify and confirm that the produced patch does indeed fix the issue.
- A patch release is made available to the wider public.

<div class="alert alert-warning"><strong>List Etiquette</strong><p>The security team asks that you please <strong>DO NOT</strong> create publicly-viewable issues or posts to address the problem. There is no good sense in creating panic and chaos. All community members <strong>SHOULD</strong> ignore all such public announcements and reports.</p></div>

## CVEs

The CAS project does not request or provide CVEs when handling security issues. This is a task that must entirely be handled by you,
should you need to obtain a CVE in order to justify an unplanned upgrade, etc. The CAS project, generally speaking, moves much quicker
to announce and disclose security issues than any CVE procurement process; we simply cannot delay security notifications and announcements
for as long as it could take to receive a CVE. 

If you wish to volunteer to become a point person to get the CVE creation process going, by all means, speak up.

## Community Notification

Once the release is made available, the following procedure may be observed:

- Community members [are notified](/cas/Mailing-Lists.html) about the release availability. 
- The notification message SHOULD discuss:
  - The security issue in generic terms providing only enough detail to cause concern and attract community attention, but not to cause chaos or create an opportunity for adversaries to take advantage of the issue.
  - The severity of the security issue and nature of the patch release.
  - Affected CAS versions
  - Recommended guidelines for upgrades and applications of the patch.
- At the end of a four-week grace period, a public security announcement is posted that should fully disclose the issue and nuances of the security patch or workarounds.

An example notification message follows:

```
CAS x.y.z has been released. This release addresses a rather serious security issue where successfully exercising this vulnerability 
may allow an adversary to gain insight into the running CAS server. If you have deployed **any version** of CAS x.y.z, you **MUST** 
take action to upgrade. If you have deployed any **other** versions of CAS, disregard this issue.
```

[Keep it simple](https://www.wikiwand.com/en/KISS_principle).

<div class="alert alert-info"><strong>Be Careful</strong><p>At the risk of stating the obvious, remember to only heed and accept community notifications about security fixes from <a href="Project-Commitee.html">trusted project contacts</a> and members. Posts detailing <i>security fixes</i> from random folks should entirely be ignored.</p></div>

### Trusted Contacts

Please note that the CAS project cannot disclose details about the security issue and its effects to private individuals without first verifying
their identity and Apereo foundation membership status. If you consider yourself a trusted contact for your institution who is registered and vetted
by the Apereo foundation, please provide the project with enough background information so we can happily proceed to explain the details in a relaxed
and trusted environment. 

To learn more about how to become an Apereo foundation member and a registered trusted contact, please [contact Apereo directly](https://www.apereo.org).

