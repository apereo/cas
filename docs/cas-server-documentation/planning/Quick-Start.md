---
layout: default
title: CAS - Quick Start
description: "Build CAS in eight guided steps with passwordless and delegated login, SAML2, OIDC, MFA, LDAP, Redis, interrupt and Palantir."
quickStart: true
category: Planning
---

{% include variables.html %}

# Quick Start

This recipe takes you from nothing to a working CAS server on your own machine: run it, configure it, register an
application and log in. It is meant for learning and evaluation; [Getting Started](Getting-Started.html) explains how
to plan a real deployment.

<div class="alert alert-info">:information_source: <strong>Settings Are Links</strong><p>
Click any setting on this page, such as <code class="cas-setting">cas.server.name</code>, to see its description,
default value, module and other formats right here, without leaving the page. Press <kbd>Shift</kbd><kbd>Shift</kbd>
anywhere in the documentation to search all settings.
</p></div>

<div id="cas-quick-start" aria-label="Quick-start wizard"></div>

## 1. Try It With Docker

If you only want to see CAS running, start the official image. Replace `${tag}` with a CAS version listed on
[Docker Hub](https://hub.docker.com/r/apereo/cas/tags):

```bash
docker run --rm -p 8080:8080 \
  -e SERVER_SSL_ENABLED=false -e SERVER_PORT=8080 \
  apereo/cas:${tag}
```

Open `http://localhost:8080/cas/login` and log in as `casuser` with the password `Mellon`. The two variables
are `server.ssl.enabled`{: .cas-setting} and `server.port`{: .cas-setting} written as environment variables;
every setting can be passed this way. See [Docker Installation](../installation/Docker-Installation.html) for more.

The Docker image is a standalone preview. The remaining steps build your own overlay with the features you choose.
Stop the preview with <kbd>Ctrl</kbd>+<kbd>C</kbd> before starting your overlay on the same port.

## 2. Create an Overlay

A real deployment is built from a WAR overlay: a small Gradle project that pulls in CAS and holds only what you add or
change. With a JDK installed (see [Installation Requirements](Installation-Requirements.html)), generate one:

```bash
curl https://getcas.apereo.org/starter.tgz -d type=cas-overlay -d baseDir=cas | tar -xzvf -
cd cas
```

The [CAS Initializr](../installation/WAR-Overlay-Initializr.html) can also select the CAS version and modules for you.

### Choose Your Features

CAS login and a JSON service registry are included in this guide. Select any additions below; their instructions
will appear in the following steps. You can return here and change your choices at any time.
Without JavaScript, all optional recipes are shown.

<fieldset class="quick-start-features">
<legend>Add to your overlay</legend>
<label><input type="checkbox" value="ldap"> <span><strong>LDAP Authentication</strong>Authenticate against your directory</span></label>
<label><input type="checkbox" value="oidc"> <span><strong>OpenID Connect Provider</strong>Use CAS as an OpenID Provider</span></label>
<label><input type="checkbox" value="saml2"> <span><strong>SAML2 Identity Provider</strong>Use CAS as a SAML identity provider</span></label>
<label><input type="checkbox" value="gauth"> <span><strong>MFA with Google Authenticator</strong>Verify a time-based authenticator code</span></label>
<label><input type="checkbox" value="webauthn"> <span><strong>MFA with WebAuthn</strong>Use a security key or platform authenticator</span></label>
<label><input type="checkbox" value="duo"> <span><strong>MFA with Duo Security</strong>Authenticate through Duo Universal Prompt</span></label>
<label><input type="checkbox" value="passwordless"> <span><strong>Passwordless Authentication</strong>Sign in with an emailed one-time code</span></label>
<label><input type="checkbox" value="delegation"> <span><strong>Delegated Authentication</strong>Sign in through an external identity provider</span></label>
<label><input type="checkbox" value="interrupt"> <span><strong>Interrupt Notifications</strong>Show a notice during login</span></label>
<label><input type="checkbox" value="redis"> <span><strong>Redis Ticket Registry</strong>Store tickets outside the CAS process</span></label>
<label><input type="checkbox" value="palantir"> <span><strong>Palantir Admin Console</strong>Explore the administration console</span></label>
</fieldset>

## 3. Configure CAS

CAS reads its settings from `/etc/cas/config/cas.properties`; the directory can be changed with
`cas.standalone.configuration-directory`{: .cas-setting}. Create the directories once, owned by the account that
runs CAS, and then the file:

```bash
sudo mkdir -p /etc/cas/config /etc/cas/services
sudo chown -R "$USER" /etc/cas
```

```properties
cas.server.name=http://localhost:8080
cas.server.prefix=${cas.server.name}/cas
server.port=8080
server.ssl.enabled=false
```
{: .cas-settings-linked}

`cas.server.name`{: .cas-setting} is the address users reach CAS at, and `cas.server.prefix`{: .cas-setting} adds
the `/cas` context path. Plain HTTP is only for trying CAS locally; a deployment must use HTTPS.

<div data-quick-feature="oidc" markdown="1">

### OpenID Connect Provider

Add the OIDC module to the `dependencies` block in `build.gradle`:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc" %}

Append to `/etc/cas/config/cas.properties`:

```properties
cas.authn.oidc.core.issuer=http://localhost:8080/cas/oidc
cas.authn.oidc.jwks.file-system.jwks-file=file:/etc/cas/config/oidc.jwks
```
{: .cas-settings-linked}

CAS creates the signing keystore if it is missing; keep this directory writable by CAS and protect the generated
private keys. See [OIDC](../authentication/OIDC-Authentication.html) and
[JWKS storage](../authentication/OIDC-Authentication-JWKS-Storage.html).

</div>

<div data-quick-feature="saml2" markdown="1">

### SAML2 Identity Provider

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp" %}

Create a directory owned by the CAS process for its generated metadata, certificates and private keys:

```bash
mkdir -p /etc/cas/config/saml
```

Append to `cas.properties`:

```properties
cas.authn.saml-idp.core.entity-id=http://localhost:8080/cas/idp
cas.authn.saml-idp.metadata.file-system.location=file:/etc/cas/config/saml
```
{: .cas-settings-linked}

Keep the generated files across restarts. The entity id is CAS's stable SAML identifier; the IdP metadata endpoint
is `http://localhost:8080/cas/idp/metadata`. Register your service provider in step 4 before attempting SSO.
See [SAML2 Identity Provider](../authentication/Configuring-SAML2-Authentication.html).

</div>

<div data-quick-feature="redis" markdown="1">

### Redis Ticket Registry

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-redis-ticket-registry" %}

Start a local Redis instance in a separate terminal (port 6379 must be free):

```bash
docker run --rm --name cas-quickstart-redis -p 127.0.0.1:6379:6379 redis:7-alpine redis-server --maxmemory-policy noeviction
```

Append to `cas.properties`:

```properties
cas.ticket.registry.redis.host=localhost
cas.ticket.registry.redis.port=6379
```
{: .cas-settings-linked}

Keep Redis running while using CAS. This disposable container loses its data when removed. For a deployment,
configure persistence, authentication, TLS and shared ticket encryption keys; see
[Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html).

</div>

<div data-quick-feature="palantir" markdown="1">

### Palantir Admin Console

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-palantir" %}

Append to `cas.properties`, replacing the example password before starting CAS:

```properties
spring.security.user.name=casadmin
spring.security.user.password=replace-with-your-own-password
spring.security.user.roles=ADMIN
management.endpoints.web.exposure.include=*
management.endpoints.access.default=UNRESTRICTED
cas.monitor.endpoints.endpoint.defaults.access=AUTHENTICATED
```
{: .cas-settings-linked}

These are separate administrator credentials, not the `casuser` login. Palantir uses actuator endpoints;
keep them authenticated and restrict exposure before deployment. See the [Admin Console](../installation/Admin-Dashboard.html).

</div>

## 4. Register an Application

CAS only issues tickets to applications it knows. Add the JSON service registry to the `dependencies` block of
`build.gradle`:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-json-service-registry" %}

Point it at the services directory in `cas.properties`:

```properties
cas.service-registry.json.location=file:/etc/cas/services
```
{: .cas-settings-linked}

Then describe the application in `/etc/cas/services/Example-1.json`. The `serviceId` is a regular expression
matched against the address the application sends to CAS:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://app.example.org/.*",
  "name": "Example",
  "id": 1
}
```

See [JSON Service Registry](../services/JSON-Service-Management.html) for file naming and reloading, and
[Service Management](../services/Service-Management.html) for the policies an application can carry.

<div data-quick-feature="oidc" markdown="1">

### Register an OIDC Client

Keep the CAS example above and add `/etc/cas/services/OidcExample-2.json`:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "serviceId": "^http://localhost:9000/callback$",
  "name": "OidcExample",
  "id": 2,
  "clientId": "quickstart-client",
  "clientSecret": "replace-with-your-own-client-secret",
  "scopes": [
    "java.util.HashSet",
    [
      "openid"
    ]
  ],
  "supportedGrantTypes": [
    "java.util.HashSet",
    [
      "authorization_code"
    ]
  ],
  "supportedResponseTypes": [
    "java.util.HashSet",
    [
      "code"
    ]
  ]
}
```

Replace the secret and configure your OIDC client with the same value. The callback must match your application's
actual redirect URI. This local HTTP example is for evaluation; use HTTPS in deployment.
See [OIDC Clients](../authentication/OIDC-Authentication-Clients.html).

</div>

<div data-quick-feature="saml2" markdown="1">

### Register a SAML2 Service Provider

Export metadata from your SAML application and save it as `/etc/cas/config/saml/sp-metadata.xml`.
Use metadata obtained from the application administrator; it supplies the trusted assertion consumer endpoints
and certificates. Replace the example entity id below with the exact SP entity id, escaped as a regular expression.
Create `/etc/cas/services/SamlExample-3.json`:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "^https://saml[.]example[.]org/sp$",
  "name": "SamlExample",
  "id": 3,
  "metadataLocation": "file:/etc/cas/config/saml/sp-metadata.xml"
}
```

Import CAS's IdP metadata into the application and configure it to trust CAS's signing certificate.
The example entity id is a placeholder; you need a running SP to complete this recipe.
See [SAML2 Service Management](../services/SAML2-Service-Management.html).

</div>

## 5. Customize Login

Keep the built-in `casuser` / `Mellon` account for a first run, or select LDAP in step 2.
The interrupt option can be used with either authentication source.

<div data-quick-feature="ldap" markdown="1">

### Connect Your Directory

This step is optional. Out of the box, CAS accepts the single account set in
`cas.authn.accept.users`{: .cas-setting}. To log in with LDAP accounts instead, add the LDAP module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap" %}

Then turn off the built-in account and describe your directory:

```properties
cas.authn.accept.enabled=false
cas.authn.ldap[0].type=AUTHENTICATED
cas.authn.ldap[0].ldap-url=ldaps://ldap.example.org:636
cas.authn.ldap[0].base-dn=ou=people,dc=example,dc=org
cas.authn.ldap[0].search-filter=uid={user}
cas.authn.ldap[0].bind-dn=cn=cas,ou=services,dc=example,dc=org
cas.authn.ldap[0].bind-credential=changeit
```
{: .cas-settings-linked}

[LDAP Authentication](../authentication/LDAP-Authentication.html) covers Active Directory, direct binds and
attribute retrieval.

</div>

<div data-quick-feature="interrupt" markdown="1">

### Show an Authentication Interrupt

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-interrupt-webflow" %}

Append to `cas.properties`:

```properties
cas.interrupt.core.trigger-mode=AFTER_AUTHENTICATION
cas.interrupt.json.location=file:/etc/cas/config/interrupt.json
```
{: .cas-settings-linked}

Create `/etc/cas/config/interrupt.json`. If using LDAP, replace `casuser` with your resolved principal id:

```json
{
  "casuser": {
    "message": "Welcome to your CAS evaluation. Continue to your application when ready.",
    "interrupt": true,
    "block": false,
    "ssoEnabled": true
  }
}
```

This notice allows the user to continue. [Authentication Interrupt](../webflow/Webflow-Customization-Interrupt.html)
explains blocking policies and other sources; the [JSON strategy](../webflow/Webflow-Customization-Interrupt-JSON.html)
is intended for demos and testing.

</div>

<div data-quick-feature="passwordless" markdown="1">

### Passwordless Authentication

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-passwordless-webflow" %}

For a local demo, start the same mail catcher used by the CAS browser scenarios. It captures mail without
sending it to a real recipient:

```bash
docker run --rm --name cas-quickstart-mail -p 127.0.0.1:25000:25000 -p 127.0.0.1:8282:8282 mmoayyed/mockmock:latest
```

Append to `cas.properties`:

```properties
spring.mail.host=localhost
spring.mail.port=25000
cas.authn.passwordless.tokens.mail.from=cas@example.org
cas.authn.passwordless.tokens.mail.subject=CAS sign-in code
cas.authn.passwordless.tokens.mail.html=false
cas.authn.passwordless.tokens.mail.text=Your sign-in code is ${token}
cas.authn.passwordless.accounts.json.location=file:/etc/cas/config/passwordless-accounts.json
```
{: .cas-settings-linked}

Create `/etc/cas/config/passwordless-accounts.json`:

```json
{
  "@class": "java.util.LinkedHashMap",
  "casuser": {
    "@class": "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "casuser",
    "email": "cas@example.org",
    "name": "CAS Demo",
    "delegatedAuthenticationEligible": "FALSE",
    "requestPassword": false
  }
}
```

With this module enabled, eligible applications use the passwordless login flow: enter `casuser` and the emailed
code instead of `Mellon`. LDAP credentials are not used for this JSON-backed account. The explicit delegation
setting keeps this account on the email-code path even when you also install an external provider.
Use an account store with verified contact details and a real, secured mail transport for deployment.
See [Passwordless Authentication](../authentication/Passwordless-Authentication.html) and
[per-application controls](../authentication/Passwordless-Authentication-PerApplication.html) to retain password login
for selected applications. Installing the Redis ticket registry does not by itself persist passwordless accounts
or configure a shared [passwordless token store](../authentication/Passwordless-Authentication-Tokens.html).

</div>

<div data-quick-feature="delegation" markdown="1">

### Delegated Authentication

This recipe makes CAS an OIDC client of an external identity provider. The separate OpenID Connect feature in
this wizard makes CAS the provider for your applications; either or both may be installed.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-oidc" %}

Register CAS as a confidential client at your external OIDC provider. Allow the callback
`http://localhost:8080/cas/login?client_name=ExternalOidc` (use your actual HTTPS CAS origin when configured).
Append to `cas.properties` with the client credentials and discovery URL supplied by that provider:

```properties
cas.authn.pac4j.oidc[0].generic.client-name=ExternalOidc
cas.authn.pac4j.oidc[0].generic.id=replace-with-external-client-id
cas.authn.pac4j.oidc[0].generic.secret=replace-with-external-client-secret
cas.authn.pac4j.oidc[0].generic.discovery-uri=https://idp.example.org/.well-known/openid-configuration
cas.authn.pac4j.oidc[0].generic.scope=openid profile email
```
{: .cas-settings-linked}

You need a reachable provider and a user allowed to sign in there; the discovery URL above is a placeholder.
The callback must match the provider's registered redirect URI exactly. See
[generic OIDC delegation](../integration/Delegate-Authentication-Generic-OpenID-Connect.html) for provider-specific settings,
and [Delegated Authentication](../integration/Delegate-Authentication.html) for SAML2, CAS and other providers.

If you also select passwordless authentication, add this second entry alongside `casuser` in
`passwordless-accounts.json` to route a separate demo account to the external provider:

```json
{
  "delegateduser": {
    "@class": "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "delegateduser",
    "email": "delegateduser@example.org",
    "delegatedAuthenticationEligible": "TRUE",
    "allowedDelegatedClients": [
      "java.util.ArrayList",
      [
        "ExternalOidc"
      ]
    ]
  }
}
```

Merge the `delegateduser` entry into the existing JSON object; do not replace the `casuser` entry.
The external provider still authenticates the user and determines the resulting identity.
See [passwordless delegation](../authentication/Passwordless-Authentication-Delegation.html).

</div>

### Try MFA Per Application

Each selected MFA recipe below registers its own demo application. You can enable several providers together
and test each separately without changing the baseline `Example-1.json` application. Adding a module alone does
not require MFA: the application's `multifactorPolicy` activates it. See
[per-application MFA triggers](../mfa/Configuring-Multifactor-Authentication-Triggers-PerApplication.html).

<div data-quick-feature="gauth" markdown="1">

### MFA with Google Authenticator

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth" %}

Use Google Authenticator or another TOTP application. The default device repository is in memory, suitable for
this local demo; registrations disappear when CAS restarts. Keep credential encryption enabled (the default).
For deployment, configure persistent [device storage](../mfa/GoogleAuthenticator-Authentication.html) and stable
`cas.authn.mfa.gauth.crypto.encryption.key`{: .cas-setting} and
`cas.authn.mfa.gauth.crypto.signing.key`{: .cas-setting} values.

Create `/etc/cas/services/GAUTHExample-4.json`:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://gauth[.]example[.]org/$",
  "name": "GAUTHExample",
  "id": 4,
  "multifactorPolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders": [
      "java.util.LinkedHashSet",
      [
        "mfa-gauth"
      ]
    ],
    "bypassEnabled": false,
    "forceExecution": true
  }
}
```

This policy requires `mfa-gauth` for this demo application's URL. Test it in step 7.

</div>

<div data-quick-feature="webauthn" markdown="1">

### MFA with WebAuthn

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn" %}

Append to `cas.properties`:

```properties
cas.authn.mfa.web-authn.core.application-id=https://localhost:8443
cas.authn.mfa.web-authn.core.relying-party-id=localhost
cas.authn.mfa.web-authn.core.relying-party-name=CAS Quick Start
cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443
cas.authn.mfa.web-authn.core.allow-primary-authentication=false
cas.authn.mfa.web-authn.core.allow-untrusted-attestation=true
```
{: .cas-settings-linked}

Use a WebAuthn-capable browser and a security key or platform authenticator. For this recipe, first configure
CAS at `https://localhost:8443` with a certificate trusted by your browser, following the
[HTTPS configuration guide](../installation/Configuring-Servlet-Container-Embedded.html). Set
`cas.server.name=https://localhost:8443`, `server.port=8443` and `server.ssl.enabled=true`, together with the
keystore settings from that guide. Replace the HTTP CAS origin in this wizard's login URLs, OIDC issuer and SAML
metadata as applicable. Keep the browser origin and relying-party id consistent when registering
and using credentials. The demo explicitly accepts untrusted attestation so authenticators without a trusted
attestation chain can enroll; choose an [attestation policy](../mfa/FIDO2-WebAuthn-Authentication-Attestation.html)
for deployment. Default registrations are in memory and disappear on restart; configure
[persistent registration storage](../mfa/FIDO2-WebAuthn-Authentication-Registration.html) for a deployment.
This recipe uses WebAuthn as a second factor after the password.

Create `/etc/cas/services/WebAuthnExample-5.json`:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://webauthn[.]example[.]org/$",
  "name": "WebAuthnExample",
  "id": 5,
  "multifactorPolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders": [
      "java.util.LinkedHashSet",
      [
        "mfa-webauthn"
      ]
    ],
    "bypassEnabled": false,
    "forceExecution": true
  }
}
```

This policy requires `mfa-webauthn` for this demo application's URL. Test it in step 7.

</div>

<div data-quick-feature="duo" markdown="1">

### MFA with Duo Security

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-duo" %}

Create a Duo application for Universal Prompt and obtain its client id, client secret and API hostname.
Use a CAS URL and callback accepted by your Duo application's configuration. For a deployment, configure a trusted
HTTPS endpoint using the [servlet container guide](../installation/Configuring-Servlet-Container-Embedded.html).
If changing the CAS origin, update `cas.server.name`, OIDC issuer, WebAuthn origins/relying-party id and SAML metadata
as applicable, and use the new origin for every login URL in this guide.

Append to `cas.properties`, replacing the placeholders with your Duo application's values:

```properties
cas.authn.mfa.duo[0].duo-integration-key=replace-with-duo-client-id
cas.authn.mfa.duo[0].duo-secret-key=replace-with-duo-client-secret
cas.authn.mfa.duo[0].duo-api-host=api-example.duosecurity.com
```
{: .cas-settings-linked}

The primary CAS username must match a user allowed by your Duo policy. Enroll that user's device in Duo;
CAS needs network access to the Duo API. These are Universal Prompt credentials, not Duo Admin API credentials.
See [Duo Security](../mfa/DuoSecurity-Authentication.html).

Create `/etc/cas/services/DuoExample-6.json`:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://duo[.]example[.]org/$",
  "name": "DuoExample",
  "id": 6,
  "multifactorPolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders": [
      "java.util.LinkedHashSet",
      [
        "mfa-duo"
      ]
    ],
    "bypassEnabled": false,
    "forceExecution": true
  }
}
```

This policy requires `mfa-duo` for this demo application's URL. Test it in step 7.

</div>

## 6. Build and Run

Before building, check that you added the modules and files for every selected feature. All property snippets
belong in the same `/etc/cas/config/cas.properties` file; all module snippets belong in the overlay's `dependencies` block.

<div class="quick-start-summary" hidden></div>

```bash
./gradlew clean build
java -jar build/libs/cas.war
```

CAS prints a `READY` banner when it has started.

## 7. Log In

Open `http://localhost:8080/cas/login?service=https://app.example.org/` and log in. CAS sends the browser back to
the application with a `ticket` parameter. The example application does not exist, so copy the `ST-…` ticket from
the address bar and validate it the way an application would:

```bash
curl "http://localhost:8080/cas/p3/serviceValidate?service=https://app.example.org/&ticket=ST-…"
```

The response names the user in `<cas:user>`. A service ticket works once and only for a few seconds, as set by
`cas.ticket.st.time-to-kill-in-seconds`{: .cas-setting}; log in again if CAS reports it as invalid. The
[CAS Protocol](../protocol/CAS-Protocol.html) describes this exchange in full.

<div data-quick-feature="oidc" markdown="1">

### Verify OpenID Connect

Check discovery:

```bash
curl --fail http://localhost:8080/cas/oidc/.well-known/openid-configuration
```

The `issuer` should be `http://localhost:8080/cas/oidc`. Configure an OIDC application with that issuer,
client id `quickstart-client`, your chosen secret, redirect URI `http://localhost:9000/callback`, scope `openid`
and authorization-code flow. Start login from that application and confirm it receives and validates an ID token.
Discovery alone does not test a full login; the callback must be served by your OIDC application.

</div>

<div data-quick-feature="interrupt" markdown="1">

### Verify the Notice

Log out at `http://localhost:8080/cas/logout`, then log in again as the user named in `interrupt.json`.
Confirm the notice appears and that Continue lets you proceed to the application.

</div>

<div data-quick-feature="redis" markdown="1">

### Verify Redis Tickets

After logging in, inspect ticket keys without displaying ticket contents:

```bash
docker exec cas-quickstart-redis redis-cli --scan --pattern 'CAS_TICKET:*'
```

Expect ticket keys while the login session is active. Check the CAS startup logs if no keys appear.

</div>

<div data-quick-feature="palantir" markdown="1">

### Open Palantir

Open `http://localhost:8080/cas/palantir` and sign in as `casadmin` with the administrator password you chose.
Confirm the dashboard loads and inspect your registered applications. Available panels depend on the modules installed.

</div>

<div data-quick-feature="saml2" markdown="1">

### Verify SAML2

```bash
curl --fail http://localhost:8080/cas/idp/metadata
```

Confirm the metadata contains your configured entity id. Start an SP-initiated login from the application
registered in step 4, authenticate at CAS and confirm the SP accepts the signed response and establishes a
session. Downloading metadata alone does not verify SSO.

</div>

<div data-quick-feature="gauth" markdown="1">

### Verify MFA with Google Authenticator

Log out of CAS, then open `http://localhost:8080/cas/login?service=https://gauth.example.org/` and enter your
primary credentials. On first use, scan the enrollment QR code with your authenticator application and confirm a current code.
Log out and log in again; verify that CAS requires a new TOTP code from the registered device.

The demo application URL is a placeholder: a redirect carrying a `ticket` shows that CAS completed login.
Validate it using step 7's `p3/serviceValidate` command with `service=https://gauth.example.org/` and the new ticket.

</div>

<div data-quick-feature="webauthn" markdown="1">

### Verify MFA with WebAuthn

Log out of CAS, then open `https://localhost:8443/cas/login?service=https://webauthn.example.org/` and enter your
primary credentials. On first use, complete device registration using the browser prompt. Log out and log in again; verify that
CAS asks you to use the registered authenticator after your password. Use the HTTPS CAS origin for ticket validation too.

The demo application URL is a placeholder: a redirect carrying a `ticket` shows that CAS completed login.
Validate it using step 7's `p3/serviceValidate` command with `service=https://webauthn.example.org/` and the new ticket.

</div>

<div data-quick-feature="duo" markdown="1">

### Verify MFA with Duo Security

Log out of CAS, then open `http://localhost:8080/cas/login?service=https://duo.example.org/` and enter your
primary credentials. Confirm CAS redirects to Duo Universal Prompt. Complete an enrolled factor and verify that Duo returns you
to CAS and CAS issues the service ticket. Use your configured CAS origin if it differs from localhost.

The demo application URL is a placeholder: a redirect carrying a `ticket` shows that CAS completed login.
Validate it using step 7's `p3/serviceValidate` command with `service=https://duo.example.org/` and the new ticket.

</div>

<div data-quick-feature="passwordless" markdown="1">

### Verify Passwordless Authentication

Log out, then open `http://localhost:8080/cas/login?service=https://app.example.org/` (or your configured HTTPS origin).
Enter `casuser`. Open the local mail catcher at `http://localhost:8282`, read the newest sign-in code and enter it
at CAS. Confirm CAS issues a service ticket without asking for `Mellon`. When trying the MFA demo applications, follow the additional provider prompt required by their application policy.
Keep the mail catcher running while testing and replace it with a real mail service before deployment.

</div>

<div data-quick-feature="delegation" markdown="1">

### Verify Delegated Authentication

Log out and open `http://localhost:8080/cas/login?service=https://app.example.org/`, using your configured CAS origin.
Choose `ExternalOidc`, authenticate at the external provider and confirm you return through CAS to the application
with a service ticket. If passwordless authentication is also selected, enter `delegateduser` from the combined
account example instead to start delegation. Validate the ticket as shown above and confirm the returned principal
is the identity expected from your external provider.

</div>

## 8. Before Production

- Set your own signing and encryption keys for the single sign-on cookie and the login flow:
  `cas.tgc.crypto.encryption.key`{: .cas-setting}, `cas.tgc.crypto.signing.key`{: .cas-setting},
  `cas.webflow.crypto.encryption.key`{: .cas-setting} and `cas.webflow.crypto.signing.key`{: .cas-setting}.
  When they are missing, CAS generates them at startup and logs them; keep those values and use them on every node.
- Serve CAS over HTTPS only, through the [embedded](../installation/Configuring-Servlet-Container-Embedded.html)
  or an [external](../installation/Configuring-Servlet-Container-External.html) servlet container.
- With more than one node, choose a shared [ticket registry](../ticketing/Configuring-Ticketing-Components.html)
  and keep the service definitions identical on every node.
- Read the [Security Guide](Security-Guide.html).

Also replace demo accounts and client secrets, protect the OIDC keystore, secure and persist Redis, and restrict
Palantir and actuator access when you enable these features. Protect SAML signing keys, persist MFA device
registrations, keep Google Authenticator encryption keys stable and configure your WebAuthn attestation and Duo access policies.

## Next Steps

- [Getting Started](Getting-Started.html): plan a deployment and gather use cases.
- [Configuration Properties](../configuration/Configuration-Properties.html): search every setting.
- [Authentication](../authentication/Configuring-Authentication-Components.html): add other sources and
  [multifactor authentication](../mfa/Configuring-Multifactor-Authentication.html).
- [Protocols](../protocol/CAS-Protocol.html): connect applications with CAS, SAML2 or OpenID Connect.
