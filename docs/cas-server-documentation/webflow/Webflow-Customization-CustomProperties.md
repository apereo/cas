---
layout: default
title: CAS - Webflow Customization
category: Webflow Management
---

{% include variables.html %}

# Webflow Custom Properties

All webflow components and CAS views have access to the entire bundle of CAS settings 
defined from a variety of configuration sources. This allows one to extend and modify 
any CAS view or webflow component using the variable `casProperties` to gain access to 
a specific setting. Remember that this syntax only allows access to settings 
that are *owned* by CAS, noted by its very own prefix.

{% include_cached casproperties.html properties="cas.custom.properties" %}

<div class="alert alert-info">:information_source: <strong>Usage</strong>
<p>If you are extending CAS to define your own components and business logic and need to find a way to define your own settings, 
we generally recommend that you define your own setting namespace and prefix using your own <code>@ConfigurationProperties</code>
conventions. Hijacking a CAS-owned configuration namespace should be seen as a poor design choice and must be avoided.</p></div>
