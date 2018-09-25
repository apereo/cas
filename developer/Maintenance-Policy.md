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

## Policy

- CAS adopters **MAY EXPECT** a CAS release to be maintained for one calendar year, starting from the original release date.
- Maintenance during this year includes bug fixes, security patches and general upkeep of the release.
- Once the year is passed, maintenance of the release is **STRICTLY** limited to security patches and fixing vulnerabilities for another calendar year.
- The lifespan of a release **MAY** be extended beyond a single year, to be decided by the [CAS PMC](Project-Commitee.html) and the community at large when and where reasonable.

By “CAS Release”, we mean anything that is a feature release and above. (i.e. `4.1.x`, `4.2.x`, `5.0.x`, `5.1.x`, etc).

## EOL Schedule

The following CAS releases will transition into a security-patch mode (SPM) only and will be EOLed at the indicated dates.

| Release        | SPM Starting Date  | Full EOL  |
| -------------- |:-------------:| --------------:|
| `5.3.x`        | June 29th, 2019  | June 29th, 2020 |
| `5.2.x`        | November 27th, 2018  | November 27th, 2019 |
| `5.1.x`        | May 30th, 2018  | May 30th, 2019 |
| `5.0.x`        | October 30th, 2017  | October 30th, 2018 |

All previous releases absent in the above table are considered EOLed.

