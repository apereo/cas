---
layout: default
title: CAS - Quick Start
description: "A short recipe to run CAS on your machine: try the Docker image, build a WAR overlay, set the essential settings, register an application, connect LDAP and log in."
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

## 2. Create an Overlay

A real deployment is built from a WAR overlay: a small Gradle project that pulls in CAS and holds only what you add or
change. With a JDK installed (see [Installation Requirements](Installation-Requirements.html)), generate one:

```bash
curl https://getcas.apereo.org/starter.tgz -d type=cas-overlay -d baseDir=cas | tar -xzvf -
cd cas
```

The [CAS Initializr](../installation/WAR-Overlay-Initializr.html) can also select the CAS version and modules for you.

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

## 5. Connect Your Directory

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

## 6. Build and Run

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

## Next Steps

- [Getting Started](Getting-Started.html): plan a deployment and gather use cases.
- [Configuration Properties](../configuration/Configuration-Properties.html): search every setting.
- [Authentication](../authentication/Configuring-Authentication-Components.html): add other sources and
  [multifactor authentication](../mfa/Configuring-Multifactor-Authentication.html).
- [Protocols](../protocol/CAS-Protocol.html): connect applications with CAS, SAML2 or OpenID Connect.
