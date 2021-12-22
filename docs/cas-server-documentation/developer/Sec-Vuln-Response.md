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

- Security issues are [privately reported to Apereo](/cas/Mailing-Lists.html) via `cas-appsec-private@apereo.org`.
- Project members acknowledge the issue and ascertain its merits. All discussion **MUST** happen privately on the [appropriate mailing list](/cas/Mailing-Lists.html).
- A fix is planned, produced and made available privately as a *direct commit* to the codebase.
- Project members or the original reporter **SHOULD** verify and confirm that the produced patch does indeed fix the issue.
- A patch release is made available to the wider public.

<div class="alert alert-warning"><strong>List Etiquette</strong><p>The security team asks that you please <strong>DO NOT</strong> create publicly-viewable issues or posts to address the problem. There is no good sense in creating panic and chaos. All community members <strong>SHOULD</strong> ignore all such public announcements and reports.</p></div>

### Dependency Upgrades

Given the amount of effort involved in planning and releasing a security patch and disclosing the issue, communicating descriptions, intent,
attack window and fixes, etc it is generally best to keep security releases reserved for issues that actually can be reproduced based on
a concrete use case or those that truly and in practice affect the inner workings of the CAS software in a real way. If you could have such a
use case at some point, please be sure to supply details and steps to reproduce issues. Supposed *vulnerabilities* that are picked up and reported by security scanners, static code analyzers and such affecting a third party library used by CAS generally do not qualify, unless there is solid evidence provided by the reporter to indicate a real, practical issue affecting CAS daily ops. Such warnings often appear superficial in the context of a CAS deployment. Usually, the best course of action would be to make the upgrades either locally to the installation script or switch to a more recent CAS version that might remove such warnings.

### Security Bounty

Apereo CAS project does *not* provide a bounty or reward for discovering and reporting security issues. Security reports are received
on an as-is basis without any warranties, guarantees, or promises of any kind.

### Report Format

When you are preparing to communicate a security issue to the appropriate channel privately, please make sure your 
report contains enough diagnostics data to expedite reviews and feedback:

- Indicate the *exact CAS version numbers* that exhibit the seemingly-faulty behavior.
- Describe your deployment/development environment in sufficient relevant detail.
- Include error logs, debug logs, screenshots and other useful snippets of your configuration.
- Include steps to explain how the issue might be reproduced.
- Include an overlay project that can duplicate the issue in practice.
- If possible, prepare and share unit/integration tests to recreate the issue.

Before posting a possible security issue, please make sure the affected CAS release 
line is still [under maintenance](/cas/developer/Maintenance-Policy.html). Releases
that are considered EOL will not receive further updates and/or attention from designated project members.

### Time to Fix

Remember that activity on the mailing lists and all other support channels is entirely voluntary. There is no official meaning or sense of urgency built into the response model and as such, fixes to potential security issues are 100% dependent on individuals' availability and willingness. We strongly recommend that you study the project's license for more information on this matter. If you are interested in contractual obligations, SLAs and a response model based on calculated levels of urgency, please consider negotiating a professional support agreement with Apereo commercial [service/support providers](/cas/Support.html).
 
### Security Fixes

The CAS application security group encourages all reporters to not include instructions and steps to reproduce and verify a reported issue, but
to also include and propose fixes in form of patch files that are shared privately with the group in the same issue report. Most, if not all, security 
fixes that are applied to the CAS codebase, regardless of contribution type (i.e. direct developer commit or outsider contribution), are almost always
made privately. The details of the fix and application of the patch are kept privately, and are not exposed in the public 
commit log, until due time. Security fixes and contributions in form of publicly-viewable pull requests are *automatically closed* 
with a cautious warning to the contributor to follow due process to get a patch fix applied and verified correctly.

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

<div class="alert alert-info"><strong>Be Careful</strong><p>At the risk of stating the obvious, remember to only heed and accept community notifications about security fixes from <a href="/cas/developer/Project-Commitee.html">trusted project contacts</a> and members. Posts detailing <i>security fixes</i> from random folks should entirely be ignored.</p></div>

### Trusted Contacts

Please note that the CAS project cannot disclose details about the security issue and its effects to private individuals without first verifying
their identity and Apereo foundation membership status. If you consider yourself a trusted contact for your institution who is registered and vetted
by the Apereo foundation, please provide the project with enough background information so we can happily proceed to explain the details in a relaxed
and trusted environment. If you are not a trusted member yet, please be patient during the grace period and allow other trusted contacts to properly
and securely take action to remedy the issue before the issue publicly disclosed to the wider user community.

To learn more about how to become an Apereo foundation member and a registered trusted contact, please [contact Apereo directly](https://www.apereo.org).
