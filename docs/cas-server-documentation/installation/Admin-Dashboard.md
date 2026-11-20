---
layout: default
title: CAS - Admin Console
description: "Palantir, the CAS admin dashboard, for managing registered services, inspecting sessions and tickets and operating the server from the browser."
category: Installation
---

{% include variables.html %}
 
# Admin Console & Dashboard
   
CAS provides a number of facilities and dashboard that can be used to administer and manage the CAS server deployment.
Such options usually are not mutually exclusive and are designed to work together and present various aspects
of the CAS configuration and build that might include application registrations, configuration properties, etc.

{% tabs casadminoptions %}

{% tab casadminoptions Palantir %}
   
Palantir is the next generation of the [CAS Management](https://github.com/apereo/cas-management) tool. It is now part of the 
CAS codebase in an attempt to both streamline the development and release processes and to ensure the tool
remains consistent and up to date. Its intention is to act as the overall admin management tool and console for the CAS server,
and presents a user interface to allow one to add and modify application registrations, observe CAS server status, 
state of single sign-on sessions, access strategy and authorization, protocol payload simulations and a lot more.

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-palantir" %}

Accessing the Palantir dashboard will by default require a form-based user authentication. The credentials
used to access this feature are those presented by Spring Security configuration:

{% include_cached casproperties.html thirdPartyStartsWith="spring.security.user" %}
                                   
You need to make sure the authenticated user is assigned a `ROLE_ADMIN` role/authority to be able to access critical functionality.

Palantir also supports routing authentication requests to the CAS server itself. This feature is 
disabled by default and needs to be explicitly enabled.

{% include_cached featuretoggles.html features="Palantir.cas-authentication" %}

Once enabled, you may create an application registration entry for Palantir and assign a specific access strategy
to it to only allow access to authorized users, or you may instruct Palantir to reject authentication attempts
that do not pass the access requirement rules.

{% include_cached casproperties.html properties="cas.palantir.cas-authentication" %}
        
By default, authenticated users are assigned a `ROLE_USER` role/authority. To access critical functionality as an admin,
you will need to resolve and release a `role` attribute to Palantir with a value of `ADMIN` or `ROLE_ADMIN` 

Furthermore, note that Palantir requires a number of actuator endpoints, such as `info`, `env`, etc that are 
provided by CAS and Spring Boot. It essentially sits on top of actuator endpoints and consumes and renders data. You
will need to make sure these endpoints are enabled and exposed for your CAS deployment. There are startup checks in 
place to ensure required actuator endpoints are present and configured. For best results, you may want to enable
and expose all actuator endpoints and certainly require authentication access for each endpoint.

Once enabled, Palantir is available at `/cas/palantir`.

Palantir follows the light or dark appearance of the operating system. The theme button in its navigation
bar switches between light and dark, and the settings dialog can return Palantir to the operating system's choice;
the choice is remembered by the browser. *Reset preferences*, in the settings dialog, removes everything Palantir keeps
in the browser, such as the theme, views, filters, editor options and drafts of unsaved edits, and reloads Palantir
with its defaults; the sign-in session is kept.

Palantir opens on *Home* (<kbd>g</kbd>), a page of widgets: the server and its health, the health checks charted as on
the system page, heap and processor use, HTTP requests (a minute, average and slowest time, errors), the URLs that
answer slowest on average, leaving out Palantir's own, actuator and static requests, runtime metrics
(garbage collection pauses, errors logged, system load), single sign-on sessions, applications by type, leaving out
the services CAS registers for itself, and the latest changes to applications with who made them. With
[service change history](../services/Configuring-Service-Version-History.html) installed, the changes of every CAS
server are listed from that shared history; without it, those of the server Palantir runs on since it started. A widget's title opens the page it summarizes. Widgets can
be dragged to another place, moved with their arrow buttons or hidden; *Customize* brings hidden widgets back or resets
the page, and the browser remembers the order and the hidden widgets. The page is drawn from one summary that the server
puts together and keeps for a few seconds per part, so it costs one request per refresh, and only the parts the shown
widgets need are put together: hiding a widget that is slow to fill takes its work off the server for that person, and
the server widget then says only that CAS is running when the health widget is hidden;
a widget whose data comes from an actuator endpoint that is not exposed is left out. Reloading Palantir stays on the
page that was showing.

The search button in the navigation bar, also opened with `Ctrl+K` (`⌘K` on macOS) or `/`, finds any page or
section of Palantir, including the sections inside a section such as the key rotation of OpenID Connect, a registered application by its name, identifier or service id, and common actions such as
creating an application, switching the theme, opening the settings or signing out. Arrow keys move through the matches and
`Enter` opens one. Typing a username also offers to look that user up. It opens with the last search, selected so typing
replaces it, and with the match chosen last marked, so `Enter` opens it again; this is remembered in the browser.

The *Users* page gathers what CAS holds about one user: single sign-on sessions, registered multifactor devices,
trusted devices, attribute consent decisions, resolved attributes, accounts the user may impersonate, the passwordless
account, password history, throttled login attempts, the interrupt the user would see at login and recent audit events, over the last day, week or month. Each source that has something
recorded for the user gets a card, which expands to show its records and its usual actions, such as ending a session,
removing a device or revoking a consent decision; the first card starts expanded. A source is only consulted when the
actuator endpoint behind it is enabled and exposed, and the page itself is hidden when none of them is.

<kbd>Shift</kbd>+<kbd>Z</kbd> turns zen mode on and off: the CAS header, the navigation bar and the footer are put away so
the page has the whole window, while the keyboard shortcuts and the search keep working. The browser remembers the
choice, and a small button at the top right leaves zen mode.

The *Throttles* page lists every throttled login attempt, or only those of one username, matched regardless of case.
The *Consent* page and the audit events likewise show the consent decisions or the audit records of one username.

The multifactor section lists the [multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html)
CAS registers by rank, each with how it is doing: for Duo Security, whether Duo answers and how quickly, from the Duo
ping endpoint, and for providers that keep registrations, how many users registered. The selected provider shows its
rank, failure mode, registered users, bypass rules and how many of its settings differ from their defaults, and its
settings grouped as identity and ranking, behavior, bypass, storage and security. Each setting appears once, with the
value that takes effect and the property source it comes from, its description and whether it keeps its default;
secrets stay hidden. A setting can be looked up in the configuration metadata or overridden when the configuration
can be changed at runtime, and settings left at their default can be shown as well. A filter narrows the providers
and their settings. A Duo Security provider also looks a user up in Duo: how Duo treats their next sign-in, and, with the Duo
Admin API, their name, email address, account status, enrollment, last login, devices, groups and bypass codes.
From there, new bypass codes can be created and shown once, and the account's status, name and email address changed.

Under *Multifactor Credential Repositories*, the multifactor section has a card for each repository of registered
[Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html) accounts,
[WebAuthn](../mfa/FIDO2-WebAuthn-Authentication-Registration.html) devices and
[YubiKey](../mfa/YubiKey-Authentication.html) accounts whose actuator endpoint is enabled, with how many records it
holds and for how many users. *Export* downloads every record as a zip, *Import* takes such a zip or a single JSON
record, so records can move from one server or repository to another, and *Delete All* removes every record after a
confirmation. Removing one user's devices is done from the *Users* page or the devices section.

*System Metrics* opens on a board of pinned metrics, each with its current value in its own unit, a trend while the
page is open and a warning when usage passes three quarters or nine tenths; the pins are kept in the browser. The
explorer lists every metric by family, filterable by name, and shows the selected one's statistics, with the average
where it has a count and a total, a live trend, and a breakdown by any of its tags; clicking a bar narrows the metric
to that tag value, and the metric can be pinned to the board as narrowed.

When the history of application definitions is recorded, the change history of an application lists its versions,
latest first with who changed each one, and shows the selected version side by side with the current definition or with the version before it,
highlighting the lines and words that differ. *Restore this version* makes the selected version the current definition
again, after a confirmation.

The authentication section lists, under *Interrupts*, which [interrupt](../webflow/Webflow-Customization-Interrupt.html)
a user would see at login, optionally signing in to a given application: the message, links and data of the interrupt,
whether it blocks the login, and what each interrupt source answered.
Under *Impersonation*, it shows the accounts a user may [impersonate](../authentication/Surrogate-Authentication.html)
as cards, or says that the user may impersonate any account or none. When attribute resolution is available, each card
can fetch the attributes of its account, which opens attribute resolution with that account filled in.

Writing a new application definition by hand starts from a template: CAS ships templates for the protocols the
deployment supports, such as releasing selected attributes, requiring multifactor authentication, persistent SAML2
NameIDs, public OpenID Connect clients with PKCE or pairwise subject identifiers, which can be filtered by protocol and
searched, or the definition can start blank, be pasted or be read from a file. The editor then checks the JSON as it is
typed, marks the line of a problem CAS reports when validating or saving, and can switch to the wizard, listing any
setting the wizard does not cover with a way back. Templates of your own can be offered by placing application
definitions as JSON files in a folder that the dashboard reads every time it is shown.

{% include_cached casproperties.html properties="cas.palantir.templates-directory" %}

*Import* accepts an application definition in JSON or YAML, or a zip of JSON definitions such as the one *Export*
produces; an imported application replaces one with the same id. While the file uploads and CAS imports it, the
dashboard shows the progress of the upload and that the server is busy.

Before changes to an existing application are saved, the application is read again. When someone else saved or
deleted it after the edit started, also when the edit was kept as a draft and resumed later, the dashboard shows what
they changed and asks whether to keep editing or to save anyway.

An endpoint whose access level is read-only only answers reads, so the dashboard disables what would change something
through it, such as saving an application, ending a session or changing a logger, and explains why.

The wizard asks for what an application answers, such as its service id, redirect URI or entity id, in the section of
its protocol, next to the protocol's other settings.
Among its advanced sections, the wizard sets an application's delegated authentication policy, when its access
strategy checks the required and rejected attributes, and which users an interrupt notification applies to; its
consent policy can leave attributes and services out of consent. Access can be decided by Cerbos, Permit.io or
Amazon Verified Permissions, or, for surrogate sessions, by the surrogate's attributes or a Groovy script; OpenID
Connect claims can be released per scope, for a custom scope or regardless of scope, with claims mapped to attributes;
SAML2 service providers can be given the attributes released to attribute queries or those mapped from entity
attributes. The wizard also matches services by the start of their URL, keeps applications out of single sign-on
altogether, sets the salt and source attribute of anonymous and pairwise identifiers, and sets how OpenID Connect relying parties
sign backchannel authentication requests and whether those requests may carry a user code.

Fields of the wizard whose values come from a standard or from the server suggest them as they are typed, each with a
short description, while still taking any value: SAML2 NameID formats, authentication context classes, bindings,
XML signature, digest and encryption algorithms, entity attributes and their values, such as entity categories, OpenID Connect signing and encryption
algorithms, client authentication methods, subject types and the default issuer, OAuth token types and the client ids
of other applications as audiences, the WS-Federation protocol, token types, namespaces and default realm, CAS ticket
lifetimes and uses, durations, time units, HTTP methods and header names, locales and time zones, and the attributes,
authentication handlers and multifactor providers of the server. Fields that hold a list are completed one entry at a time.

When adding a SAML2 service provider, *From Metadata* fills the wizard from the provider's metadata: from a URL,
which CAS reads on the dashboard's behalf without registering, caching or storing it, or from metadata XML pasted or
chosen as a file. When the metadata is an aggregate, the dashboard asks which service provider to use. The entity id,
name, description, information and privacy URLs, logo, metadata location, a single declared NameID format, assertion
signing, an attribute release policy for a research and scholarship category or for the attributes the provider
requests, and its contacts are filled in for review before saving.

When SAML2 service provider metadata management is available, *Upload Metadata* beside the metadata location stores
the provider's metadata with a metadata manager and points the application at it, and *Manage Metadata* closes the
wizard, asking first when it holds unsaved changes, and shows the entries of the application's metadata manager under
*Protocols*.

The applications chart and the authentication flow diagrams can be magnified to fill the window, with their
magnify button or a click on them; a magnified diagram can be zoomed and moved.

The feature catalog lists the CAS features present in the deployment and their modules. When dynamic configuration
sources are available, the menu of a feature or module turns it off or on by storing its feature flag in the
configuration source the operator chooses; CAS decides which features run when it starts, so the catalog marks what
changes after a restart, and a feature turned off stays listed so it can be turned on again.

The dynamic configuration sources, whose properties can be changed at runtime, list their properties with a menu
to view, update or delete a value. When [configuration security](../configuration/Configuration-Properties-Security-CAS.html)
is set up, the menu also encrypts a plain value or decrypts an encrypted one, stores the result in the same source and
offers to refresh CAS so that the change takes effect.

A SAML2 application whose metadata comes from a web address links to it; one whose metadata is held by a metadata
manager, such as MongoDB, a relational database or Git, is tagged with that source and links to the service provider
metadata management under protocols with that manager selected, and a file or classpath location is tagged as such.

*Check Metadata Health*, in the menu of a SAML2 application, reports on the provider's metadata: whether CAS can
download it from its location, on the dashboard's behalf, and finds the service provider in it, whether CAS resolves
it the way it does at login, with its signature and validity checks, and when the metadata and the certificates the
provider publishes expire, warning a month ahead. The metadata that was read is shown. Nothing is registered, stored
or removed from the metadata cache.

An application can be disabled from its context menu, and enabled again, after confirming; disabled applications are
marked in the table and the cards. An access strategy that has no switch of its own is combined with a disabled
default strategy while the application is disabled, and left as it was once it is enabled again. Applications can be
selected by clicking their card or row, and clicked again to deselect them, with Shift to select a range, or all of
those the search and type filter show at once, and the selection exported,
as a zip of their definitions that *Import* accepts, disabled or enabled together (those already in that state are
left alone), or deleted together; *Delete All* removes every application from the service registry after the operator confirms.

*Duplicate Application* opens a copy of an application in the wizard as a new application, named after the original,
with its service id left empty and, where the application has them, a new client id and secret. When the wizard or the
definition editor holds changes that are not saved, closing it asks whether to keep editing, keep a draft and close, or
discard the changes. Drafts are kept by the browser, never on the server, while changes stay unsaved, so a reload or a closed
tab loses nothing; reopening the application, or a new application of the same type, offers to resume the draft. A draft
is dropped once it is thirty days old.

Pressing `?` outside a text field, or choosing *Keyboard shortcuts* in the search palette, lists every keyboard shortcut.
Rows with actions in Palantir's tables can be reached with the keyboard: `Enter` chooses the focused row and `Shift+F10`
opens its actions, as a right click does.

{% endtab %}

{% tab casadminoptions Spring Boot Admin %}

CAS takes advantage of Spring Boot Admin server to manage and monitor its internal state visually. As a Spring Boot Admin client, CAS registers 
itself with the Spring Boot Admin server over HTTP and reports back its status and health to the server’s web interface.
     
More details about this integration is [available here](../monitoring/Configuring-SpringBootAdmin.html).

{% endtab %}

{% endtabs %}
