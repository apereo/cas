---
layout: default
title: CAS - Webflow Customization
category: Webflow Management
---

{% include variables.html %}

# Webflow Single Sign-on & Services

By default, CAS will present a generic success page if the initial authentication request does not identify
the target application. In some cases, the ability to login to CAS without logging
in to a particular service may be considered a misfeature because in practice, too few users and institutions
are prepared to understand, brand, and support what is at best a fringe use case of logging in to CAS for the
sake of establishing an SSO session without logging in to any CAS-reliant service.

As such, CAS optionally allows adopters to not bother to prompt for credentials when no target application is presented
and instead presents a message when users visit CAS directly without specifying a service.

{% include_cached casproperties.html properties="cas.sso.services" %}
