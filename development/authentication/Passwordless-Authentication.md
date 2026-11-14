# Passwordless Authentication

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication.html

Passwordless Authentication is a form of authentication in CAS where passwords take the form of tokens that expire after a configurable period of time. Using this strategy, users are asked for an identifier (i.e. username) which is used to locate the user record that contains forms of contact such as email and phone number. Once located, the CAS-generated token is sent to the user via the configured notification strategies (i.e. email, sms, etc) where the user is then expected to provide the token back to CAS in order to proceed.

> **No Magic Link**
>
> Presently, there is no support for magic links that would remove the task of providing the token back to CAS allowing the user to proceed automagically. This variant may be worked out in future releases.

In order to successfully implement this feature, configuration needs to be in place to contact account stores that hold user records who qualify for passwordless authentication. Similarly, CAS must be configured to manage issued tokens in order to execute find, validate, expire or save operations in appropriate data stores.

Qualifying passwordless accounts may also directly be routed to selected [multifactor authentication](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-MFA.html) providers or [delegated to external identity providers](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Delegation.html) for further verification. Alternatively, the passwordless account may be instructed to allow the user to select from a [menu of available authentication options](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-UserSelectionMenu.html).

## Passwordless Variants

Passwordless authentication can also be activated using [QR Code Authentication](https://apereo.github.io/cas/development/authentication/QRCode-Authentication.html), allowing end users to login by scanning a QR code using a mobile device.

Passwordless authentication can also be achieved via [FIDO2 WebAuthn](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication.html) which lets users verify their identities without passwords and login using FIDO2-enabled devices. When WebAuthn is allowed to act as a primary authentication strategy, the passwordless flow itself [offers passkeys](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Passkeys.html).

## Overview

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-passwordless-webflow"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.core.delegated-authentication-selector-script.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.passwordless.core.passwordless-account-customizer-script.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.passwordless.core.delegated-authentication-activated` (Optional; type `Boolean`; default `false`): Allow passwordless authentication to skip its own flow in favor of delegated authentication providers that may be available and defined in CAS. If delegated authentication is activated, CAS will skip its normal passwordless authentication flow in favor of the requested delegated authentication provider. If no delegated providers are available, passwordless authentication flow will commence as usual.
- `cas.authn.passwordless.core.enabled` (Optional; type `Boolean`; default `true`): Flag to indicate if paswordless authentication is enabled.
- `cas.authn.passwordless.core.multifactor-authentication-activated` (Optional; type `Boolean`; default `false`): Allow passwordless authentication to skip its own flow in favor of multifactor authentication providers that may be available and defined in CAS. If multifactor authentication is activated, and defined MFA triggers in CAS signal availability and eligibility of an MFA flow for the given passwordless user, CAS will skip its normal passwordless authentication flow in favor of the requested multifactor authentication provider. If no MFA providers are available, or if no triggers require MFA for the verified passwordless user, passwordless authentication flow will commence as usual.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Account Stores

Please see [this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Account-Storage.html).

## Token Management

Please see [this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Tokens.html).

### Messaging & Notifications

Please [see this](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Notifications.html) for details.

## Passwordless Authentication Per Application

Please see [this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-PerApplication.html) for details.

## reCAPTCHA Integration

Please see [this guide](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Recaptcha.html) for details.
