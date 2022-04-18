---
layout: default
title: CAS - Account Profile Management
category: Registration
---

{% include variables.html %}

# Account (Profile) Management

Account (Profile) management in CAS allows an authenticated end-user to browse and/or update certain aspects of their account in a *mini portal* like environment. Typical operations allowed by this portal might include resetting the password or updating security questions, browsing login activity, registered devices for multifactor authentication, etc.

<div class="alert alert-info"><strong>Remember</strong><p>
If you are looking for ways to allow the end-user to sign up, register and create an account,
you should be looking at the <a href="Account-Registration-Overview.html">Account Registration</a> feature instead.</p>
</div>

{% include_cached featuretoggles.html features="AccountManagement" %}

## Password Management

To allow the end user to reset their password willingly and/or update their security questions, the password management functionality must be enabled in CAS using the instructions [specified here](../password_management/Password-Management.html).

## Audit Log Activity

The account management dashboard allows one to examine their login activity for the past `60` days by default. This information is fetched for the authenticated user from the [CAS audit log](../audits/Audits.html), which must be configured to record auditable login activity in a dedicated storage service, such as a relational database, etc. 

## Multifactor Registered Devices

If multifactor authentication is turned on in CAS, certain multifactor providers may able to present a list of registered MFA devices for the authenticated user. This capability [depends on the provider](../mfa/Configuring-Multifactor-Authentication.html) and whether it's able to support the account profile management feature.
