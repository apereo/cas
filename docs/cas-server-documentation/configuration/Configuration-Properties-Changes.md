---
layout: default
title: CAS - Configuration Properties Changes
description: "CAS settings added, removed, replaced or given a new default since the previous CAS version, generated from the configuration catalogs."
category: Configuration
---

{% include variables.html %}

# Configuration Properties Changes

This page compares the CAS configuration catalog of this version with the catalog of the version before it, and lists
the CAS settings that were added, removed, replaced by others or given a new default value. It is generated when the
documentation is built, so it complements the [release notes](../release_notes/Overview.html), which explain why
things changed. Select a setting to see its description, type and module without leaving the page. The same list is
published as JSON for scripts and tools, next to the [configuration catalog](Configuration-Properties.html).

{% assign settingsChanges = site.data[siteDataVersion]["settings-changes"] %}
{% include settings-changes.html changes=settingsChanges %}
