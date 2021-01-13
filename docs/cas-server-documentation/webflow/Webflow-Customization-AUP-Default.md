---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Webflow Management
---

{% include variables.html %}

# Default Acceptable Usage Policy

By default the task of remembering the user's choice is kept in memory by default and will be lost upon
container restarts and/or in clustered deployments. This option is only useful during development, testing
and demos and is not at all suitable for production.

{% include casproperties.html properties="cas.acceptable-usage-policy.in-memory" %}

The scope of the default storage mechanism can be adjusted from the default of GLOBAL (described above) to
AUTHENTICATION which will result in the user having to agree to the policy during each authentication event.
The user will not have to agree to the policy when CAS grants access based on an existing ticket granting
ticket cookie.
