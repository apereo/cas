---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# PAR - OpenID Connect Authentication

[Pushed Authorization Request (PAR)](https://tools.ietf.org/html/draft-ietf-oauth-par) allows clients to push the payload of an OIDC authorization request to CAS via a direct request. The result of this push provides clients with a request URI that is used as reference to the data in a subsequent call to the authorization endpoint via the user-agent.

PAR fosters security by providing clients a simple means for a confidential and integrity protected authorization request. Clients requiring an even higher security level, especially cryptographically confirmed non-repudiation, are able to use JWT-based request objects in conduction with a pushed authorization request.

PAR allows CAS to authenticate the client before any user interaction happens. The increased confidence in the identity of the client during the authorization process allows the authorization server to refuse illegitimate requests much earlier in the process, which can prevent attempts to spoof clients or otherwise tamper with or misuse an authorization request.

<div class="alert alert-warning"><strong>Workers Ahead</strong><p>This feature is a work-in-progress and will receive additional
updates in future releases. Please check back later.</p></div>

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.par" %}
