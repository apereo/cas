---
layout: default
title: CAS - Vulnerability Response
---

# Security Vulnerability Response

Members involved in [CAS application security](/cas/Mailing-Lists.html):

- Proactively work to improve the security of CAS focusing on the Apereo CAS server, the protocols and various CAS clients
- Respond to potential vulnerabilities and create, maintain, and execute on vulnerability triage and notification policy, fielding handoffs from the Apereo security and otherwise. 
- Issue vulnerability reports and work to coordinate workarounds and fix responses to security concerns that arise.
- Produce artifacts that help potential CAS adopters to evaluate the security of CAS both as open source product and as they intend to locally implement the product. This includes threat modeling, data flow diagrams, etc.
- Create and maintain recommendations on good practices for CAS implementation around hardening, configuration, failing safe, security by default, etc.

To review the old archives of the application security working group, [please see this guide](https://wiki.jasig.org/display/CAS/CAS+AppSec+Working+Group).

## Zero Day Response Model

- Security issues are [privately reported to Apereo](/cas/Mailing-Lists.html).
- Project members acknowledge the issue and ascertain its merits. All discussion **MUST** happen privately on the [appropriate mailing list](/cas/Mailing-Lists.html).
- A fix is planned, produced and made available privately as a *direct commit* to the codebase.
- Project members or the original reporter **SHOULD** verify and confirm that the produced patch does indeed fix the issue.
- A patch release is made available to the wider public.

<div class="alert alert-warning"><strong>List Etiquette</strong><p>The security team asks that you please <strong>DO NOT</strong> create publicly-viewable issues or posts to address the problem. There is no good sense in creating panic and chaos. All community members <strong>SHOULD</strong> ignore all such public announcements and reports.</p></div>

## Community Notification

