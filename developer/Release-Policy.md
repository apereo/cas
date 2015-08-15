---
layout: default
title: CAS - Release Policy
---

# Release Policy

CAS follows the below release strategy in determining the kinds of changes different releases can
accommodate and how lines of development are managed. CAS adopters can also expect the following 
for each release type. All new improvements, fixes, enhancements, and changes are weighed against 
the release strategy for consideration in future release.

## SECURITY 

Security releases are a critical minimal change on a release to address a serious confirmed 
security issue. That is, `2.5.0.1` would be `2.5.0` with a minimal change to patch a vulnerability.
All adopters are strongly urged to upgrade to a SECURITY releases as soon as possible. 
CAS maven overlays should build with no changes, unless required and highlighted in the release notes.

## PATCH 

A conservative incremental improvement that includes bug fixes, enhancements and new features 
and is absolutely backward compatible with previous PATCH releases of the same 
MINOR release. (i.e. `2.4.15` is a drop in replacement for `2.4.14`, `2.4.13`, `2.4.12`, etc.). 
Adopters can expect that major APIs, integration points, default behavior, and general 
configuration is mostly the same. CAS maven overlays should build with little to no changes, 
unless required and highlighted in the release notes and change logs.

## MINOR 

An *evolutionary* incremental improvement that includes all PATCH release improvements 
along with fixes and enhancements that could not easily be accommodated without 
breaking backward compatibility or changing default behavior. (i.e. Transitioning from `3.4.x` to `3.5.x`, etc.) 
Adopters can expect general improvements that require moderate changes in APIs, integration points, 
default behavior, and general configuration. Overall the CAS server code along the MINOR 
development line looks pretty much the same from release to release with clear moderate evolutionary 
changes. MINOR releases may have a theme or focus that helps coordinate development. 
CAS maven overlays may require minor to moderate changes, some APIs may have changed or 
been deprecated, default behavior and configuration may have changed. 
While implementation APIs may change, [CAS Core APIs](https://github.com/Jasig/cas/tree/master/cas-server-core-api) 
will remain unchanged between MINOR versions.

## MAJOR 
 
A revolutionary change accommodating sweeping architecture, approach, and
implementation changes. (i.e. Transitioning from `3.5.x` to `4.0.x`, etc.) 
Significant changes in APIs, default behavior, and configuration can be expected. 
Maven overlays may require significant changes and possibly a complete reworking. 
Overall the CAS server code line may looked markedly different and integration 
points may require reworking. 
