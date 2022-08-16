---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policies

The attribute release policy decides how attributes are selected and provided to a given application in the final
CAS response. Additionally, each policy has the ability to apply an optional filter to weed out their attributes based on their values.

The following settings are shared by all attribute release policies:

| Name                                          | Value                                                                                                                                                                                                                                                                                                           |
|-----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `authorizedToReleaseCredentialPassword`       | Boolean to define whether the service is authorized to [release the credential as an attribute](ClearPass.html).                                                                                                                                                                                                |
| `authorizedToReleaseProxyGrantingTicket`      | Boolean to define whether the service is authorized to [release the proxy-granting ticket id as an attribute](../authentication/Configuring-Proxy-Authentication.html).                                                                                                                                         |
| `excludeDefaultAttributes`                    | Boolean to define whether this policy should exclude the default global bundle of attributes for release.                                                                                                                                                                                                       |
| `authorizedToReleaseAuthenticationAttributes` | Boolean to define whether this policy should exclude the authentication/protocol attributes for release. Authentication attributes are considered those that are not tied to a specific principal and define extra supplementary metadata about the authentication event itself, such as the commencement date. |
| `principalIdAttribute`                        | An attribute name of your own choosing that will be stuffed into the final bundle of attributes, carrying the CAS authenticated principal identifier.                                                                                                                                                           |

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Think <strong>VERY CAREFULLY</strong> before turning on 
the above settings. Blindly authorizing an application to receive a proxy-granting ticket or the user credential
may produce an opportunity for security leaks and attacks. Make sure you actually need to enable those features and that 
you understand the why. Avoid where and when you can, specially when it comes to sharing the user credential.</p></div>

CAS makes a distinction between attributes that convey metadata about the authentication event versus
those that contain personally identifiable data for the authenticated principal.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="releaseAttributes" casModule="cas-server-support-reports" %}

## Authentication Attributes

During the authentication process, a number of attributes get captured and collected by CAS
to describe metadata and additional properties about the nature of the authentication event itself.
These typically include attributes that are documented and classified by the underlying protocol
or attributes that are specific to CAS which may describe the type of credentials used, successfully-executed
authentication handlers, date/time of the authentication, etc.

Releasing authentication attributes to service providers and applications can be
controlled to some extent.

{% include_cached casproperties.html properties="cas.authn.authentication-attribute-release" %}

Protocol/authentication attributes may also be released conditionally on a per-service basis.

## Principal Attributes

Principal attributes typically convey personally identifiable data about the authenticated user,
such as address, last name, etc. Release policies are available in CAS and documented below
to explicitly control the collection of attributes that may be authorized for release to a given application.

<div class="alert alert-info"><strong>Remember</strong><p>Depending on the protocol used and the type/class of service (i.e. relying party) registered with CAS,
additional release policies may become available that allow more fine-tuned control over attribute release, catering better to the needs of the particular
authentication protocol at hand. Remember to verify attribute release capabilities of CAS by visiting and studies the appropriate documentation for each protocol.</p></div>

| Policy               | Resource                                                          |
|----------------------|-------------------------------------------------------------------|
| Default Bundle       | [See this page](Attribute-Release-Policy-DefaultBundle.html).     |
| Deny All             | [See this page](Attribute-Release-Policy-DenyAll.html).           |
| Return All           | [See this page](Attribute-Release-Policy-ReturnAll.html).         |
| Return Static        | [See this page](Attribute-Release-Policy-ReturnStatic.html).      |
| Return Allowed       | [See this page](Attribute-Release-Policy-ReturnAllowed.html).     |
| Return Encrypted     | [See this page](Attribute-Release-Policy-ReturnEncrypted.html).   |
| Return Mapped        | [See this page](Attribute-Release-Policy-ReturnMapped.html).      |
| Mapped Groovy File   | [See this page](Attribute-Release-Policy-ExternalGroovy.html).    |
| Mapped Inline Groovy | [See this page](Attribute-Release-Policy-InlineGroovy.html).      |
| Return MultiMapped   | [See this page](Attribute-Release-Policy-ReturnMultiMapped.html). |
| Pattern Matching     | [See this page](Attribute-Release-Policy-PatternMatching.html).   |
| Groovy Script        | [See this page](Attribute-Release-Policy-GroovyScript.html).      |
| REST                 | [See this page](Attribute-Release-Policy-REST.html).              |
| Script Engines       | [See this page](Attribute-Release-Policy-ScriptEngines.html).     |

## Attribute Repository Filtering

Attribute release policies can be assigned a `principalAttributesRepository` to consult attribute sources
defined and controlled by [Person Directory](Attribute-Resolution.html) attribute repositories
to fetch, resolve, cache and release attributes. 

To learn more about this topic, please [see this guide](Attribute-Release-Caching.html).

## Chaining Policies

Attribute release policies can be chained together to
process multiple rules. [See this guide](Attribute-Release-Policies-Chain.html) to learn more.

## Attribute Value Filters

While each policy defines what principal attributes may be allowed for a given service,
there are optional attribute filters that can be set per policy to further weed out attributes based on their **values**.

[See this guide](Attribute-Value-Release-Policies.html) to learn more.
