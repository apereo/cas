---
layout: default
title: CAS - Account Profile Management
description: "The CAS account profile portal, where signed-in users review login activity, MFA devices, tokens and contact methods and manage their password and security questions."
category: Registration
---

{% include variables.html %}

# Account (Profile) Management

Account (Profile) management in CAS allows an authenticated end-user to browse and/or update certain aspects of their account in a *mini portal* like environment. Typical operations allowed by this portal might include resetting the password or updating security questions, browsing login activity, registered devices for multifactor authentication, etc.

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
If you are looking for ways to allow the end-user to sign up, register and create an account,
you should be looking at the <a href="Account-Registration-Overview.html">Account Registration</a> feature instead.</p>
</div>

{% include_cached featuretoggles.html features="AccountManagement" %}

## Password Management

To allow the end user to reset their password willingly and/or update their security questions, the password management functionality must be enabled in CAS using the instructions [specified here](../password_management/Password-Management.html).

## Audit Log Activity

The account management dashboard allows one to examine their login activity for the past `60` days by default. This information is fetched for the authenticated user from the [CAS audit log](../audits/Audits.html), which must be configured to record auditable login activity in a dedicated storage service, such as a relational database, etc. 

## Multifactor Registered Devices

If multifactor authentication is turned on in CAS, certain multifactor providers may able to present a list of 
registered MFA devices for the authenticated user. Multifactor providers
may also allow the user to register their device with CAS and opt into a registration workflow.

This capability [depends on the provider](../mfa/Configuring-Multifactor-Authentication.html) and whether it's able to support the 
account profile management feature. The following providers are supported:

- [Duo Security](../mfa/DuoSecurity-Authentication.html)
- [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html): devices can be registered from the profile, and each
  device can get a new set of scratch codes. The new codes are shown once, and the old codes stop working.
- [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html): passkeys and security keys can be registered and renamed.
- [YubiKey](../mfa/YubiKey-Authentication.html): devices are listed and removed, and another device can be registered from the profile
  when the user has none or when `cas.authn.mfa.yubikey.multiple-device-registration-enabled`{: .cas-setting} is turned on.

A device, session, token or trusted device is only removed, renamed or changed when it belongs to the signed-in user.

## Password Expiration

When the authentication handler reports that the password is about to expire, as configured by the
[password policy](../installation/Password-Policy-Enforcement.html), the overview shows how many days remain and the date the
password expires, with a shortcut to change it when password management is turned on.

## Sign-in Activity

When [authentication events](../authentication/Configuring-Authentication-Events.html) are recorded, the profile lists the
sign-in attempts of the last `30` days: successful sign-ins, failed attempts and attempts flagged as risky, with their date,
IP address, location and browser.

## Contact Methods

The profile shows the email addresses and phone numbers that receive one-time codes for
[simple multifactor authentication](../mfa/Simple-Multifactor-Authentication.html), taken from the principal attributes that
the provider is configured to read, and the email address and phone number that receive
[passwordless](../authentication/Passwordless-Authentication.html) sign-in tokens. When email registration is turned on with
`cas.authn.mfa.simple.mail.accepted-email-pattern`{: .cas-setting}, the user can add an email address: CAS sends a code to it,
and the address is registered once the code is entered. Phone numbers and passwordless contact details come from the account
store and are shown read-only.

## Acceptable Usage Policy

When the [acceptable usage policy](../webflow/Webflow-Customization-AUP.html) is turned on, the profile shows whether the user has
accepted it, together with the policy terms. Policy repositories do not record when the policy was accepted, so no date is shown.

## Impersonation

When [surrogate authentication](../authentication/Surrogate-Authentication.html) is turned on, the profile lists the accounts
the user may impersonate. When the current session is itself an impersonation session, the profile says on whose behalf the
user is signed in.

## QR Code Devices

When [QR code authentication](../authentication/QRCode-Authentication.html) keeps a list of authorized devices, the profile lists
the devices that may sign the user in and lets the user remove them.

## OpenID Connect Tokens

When [OpenID Connect](../authentication/OIDC-Authentication.html) is turned on, the profile lists the access tokens and the refresh
tokens (offline access) issued to the user. Revoking a refresh token also revokes the access tokens issued from it.
