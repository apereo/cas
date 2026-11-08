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
the choice is remembered by the browser.

The search button in the navigation bar, also opened with `Ctrl+K` (`⌘K` on macOS) or `/`, finds any page or
section of Palantir, a registered application by its name, identifier or service id, and common actions such as
creating an application, switching the theme, opening the settings or signing out. Arrow keys move through the matches and
`Enter` opens one. Typing a username also offers to look that user up.

The *Users* page gathers what CAS holds about one user: single sign-on sessions, registered multifactor devices,
trusted devices, attribute consent decisions, resolved attributes, accounts the user may impersonate, the passwordless
account, password history, throttled login attempts and recent audit events, over the last day, week or month. Each source that has something
recorded for the user gets a card, which expands to show its records and its usual actions, such as ending a session,
removing a device or revoking a consent decision; the first card starts expanded. A source is only consulted when the
actuator endpoint behind it is enabled and exposed, and the page itself is hidden when none of them is.

When the history of application definitions is recorded, the change history of an application lists its versions,
latest first, and shows the selected version side by side with the current definition or with the version before it,
highlighting the lines and words that differ. *Restore this version* makes the selected version the current definition
again, after a confirmation.

{% endtab %}

{% tab casadminoptions Spring Boot Admin %}

CAS takes advantage of Spring Boot Admin server to manage and monitor its internal state visually. As a Spring Boot Admin client, CAS registers 
itself with the Spring Boot Admin server over HTTP and reports back its status and health to the server’s web interface.
     
More details about this integration is [available here](../monitoring/Configuring-SpringBootAdmin.html).

{% endtab %}

{% endtabs %}
