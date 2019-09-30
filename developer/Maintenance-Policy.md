---
layout: default
title: CAS - Maintenance Policy
---

# Maintenance Policy

This document describes the official CAS policy as it regards maintenance and management of released CAS server versions.

In particular, the following questions are addressed:

- How long should a CAS release be maintained?
- What is the appropriate scope for release maintenance once a release is retired?

## Schedules

The project release schedule is [available here](https://github.com/apereo/cas/milestones). 

<div class="alert alert-info"><strong>Which version is more stable?</strong><p>CAS releases are strictly time-based releases; they 
are not scheduled or based on specific benchmarks, statistics or completion of features or bug fixes. To gain confidence in a 
release, you should start early by experimenting with release candidates and/or follow-up snapshots.
In terms of stabillity, all versions of the Apereo CAS software are loosely based on the <i>Schrödinger's Cat</i> theory: there are no guarantees 
until you open the box. Software distributed under the project license is distributed on an "AS IS" basis without warranties or conditions 
of any kind, either express or implied. See the project license file for the specific language.
</p></div>

## Policy

- CAS adopters MAY expect a CAS release to be maintained for **six months**, starting from the [original release date](https://github.com/apereo/cas/releases).
- Maintenance during this period includes bug fixes, security patches and general upkeep of the release. Patch releases may be expected during this period per the release schedule.
- Afterwards, maintenance of the release is *strictly* limited to security patches and fixing vulnerabilities for another **six months** (i.e. SPM mode).
- The lifespan of a release MAY be extended beyond a single year, to be decided by the [CAS PMC](Project-Commitee.html) and the community at large when and where reasonable.

“CAS Release” is any feature release and above. (i.e. `4.1.x`, `4.2.x`, `5.0.x`, `5.1.x`, etc).

<div class="alert alert-info"><strong>Uh...Maintenance?</strong><p>
In this context, maintenance strictly means that the release line and the target destination branch in the CAS codebase remains open to
  accept patches and <strong>contributions from the community</strong> and that there will be follow-up binary releases forthcoming until the designated dates. 
</p></div>

## EOL Schedule

The following CAS releases will transition into a security-patch mode (SPM) only and will be EOLed at the indicated dates.

| Release        | SPM Starting Date    | Full EOL  |
| -------------- |:--------------------:| -------------------:|
| `6.1.x`        | December 1st, 2019   | June 1st, 2020      |
| `6.0.x`        | November 1st, 2019   | October 1st, 2019 |
| `5.3.x`        | October 29th, 2019   | October 29th, 2020     |

All previous releases absent in the above table are considered EOLed.

