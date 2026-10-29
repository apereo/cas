---
layout: default
title: CAS - OAuth Authentication - CSRF Cookie
category: Authentication
---
{% include variables.html %}

# OAuth Authentication - CSRF Cookie

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

CAS no longer issues a CSRF cookie at the OAuth authorization endpoint, and the settings below have no effect.
The consent page is protected by an approval key computed from the client, the user and the requested scopes, and
the [device approval](OAuth-ProtocolFlow-DeviceAuthorization.html) page by an approval token tied to the single sign-on
session of the user.

{% include_cached casproperties.html properties="cas.authn.oauth.csrf-cookie" %}
