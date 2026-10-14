---
layout: default
title: CAS - MCP Server
description: "Expose CAS as a Model Context Protocol (MCP) server so AI assistants and agents can look up applications, tickets and people."
category: Installation
---

{% include variables.html %}

# MCP Server

CAS can act as a [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server, allowing AI assistants,
agents and other MCP clients to examine the state of a CAS deployment through a set of read-only tools. Typical use cases
include troubleshooting an application registration, inspecting a single sign-on session or verifying the attributes
CAS resolves for a given person, all from within the MCP client of your choice.

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mcp" %}

## Transport

The MCP server uses the [Streamable HTTP](https://modelcontextprotocol.io/specification/2025-11-25/basic/transports#streamable-http)
transport and is available by default at `/cas/mcp`. The endpoint accepts JSON-RPC messages over HTTP `POST` and answers
with either JSON or server-sent events, as the specification requires. The server name, version, instructions,
transport protocol and endpoint path along with the capabilities advertised to clients can be adjusted via CAS settings.

{% include_cached casproperties.html thirdPartyStartsWith="spring.ai.mcp.server" %}

## Security

Every request to the MCP endpoint must be authenticated with HTTP Basic authentication. The credentials are
verified by the same user stores that protect CAS actuator endpoints, such as the Spring Security user,
JSON, JDBC, LDAP or JAAS user details sources.

{% include_cached casproperties.html thirdPartyStartsWith="spring.security.user" %}

Authenticated users may initialize a session and list the available tools. Calling a tool additionally requires
the `ROLE_MCP_ADMIN` role/authority. A tool call made by a user without this role is answered with an error result
and the tool is not executed.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
The tools return sensitive data, such as ticket-granting tickets with their authentication and principal attributes,
and full application definitions. Only grant the <code>MCP_ADMIN</code> role to trusted operators, keep the MCP
endpoint on a protected network and always use TLS.
</p></div>

## Tools

The following tools are available. All tools are read-only and are advertised to clients with the 
`readOnlyHint` annotation; nothing is returned when the requested object cannot be found.

| Tool                          | Parameters                                          | Description                                                                                                                                                                                                                                                                                                            |
|-------------------------------|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `findService`                 | `serviceId` (long)                                  | Look up a [registered service](../services/Service-Management.html) by its numeric id. The definition is returned as JSON, in the same format used by the JSON service registry.                                                                                                                                       |
| `matchService`                | `service` (string)                                  | Find the registered service that matches an application URL, the same way CAS does at login, and return its definition as JSON.                                                                                                                                                                                        |
| `evaluateAccess`              | `username`, `service` (string)                      | Evaluate the [access strategy](../services/Configuring-Service-Access-Strategy.html) of the matching registered service against the person's resolved attributes, and explain why access is denied.                                                                                                                    |
| `previewAttributeRelease`     | `username`, `service` (string)                      | Preview the username and [attributes](../integration/Attribute-Release-Policies.html) CAS would release to the application for the person, along with the access decision.                                                                                                                                             |
| `listMultifactorProviders`    | none                                                | List the configured [multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html) with their ids, names, order and failure modes.                                                                                                                                                         |
| `evaluateMultifactorTriggers` | `username`, `service` (string)                      | Run the [multifactor authentication triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html) in order for the person and application, and report which provider is required and which trigger selected it.                                                                                               |
| `searchServices`              | `query` (string)                                    | Search registered services whose name, service id pattern, description or numeric id contains the text, and return a summary of each.                                                                                                                                                                                  |
| `findSessions`                | `username` (string)                                 | Summarize the active single sign-on sessions of a person: when each was created and last used, the authentication handlers and the applications accessed.                                                                                                                                                              |
| `findTicket`                  | `ticketId` (string)                                 | Look up a ticket by its id in the [ticket registry](../ticketing/Configuring-Ticketing-Components.html) and return it as JSON. Expired tickets are not returned.                                                                                                                                                       |
| `findPerson`                  | `username` (string)                                 | Resolve a person by username through the [attribute repositories](../integration/Attribute-Resolution.html) and return the principal with its attributes.                                                                                                                                                              |
| `findSessionsWithAttribute`   | `attributeName`, `attributeValue` (string)          | Summarize the active single sign-on sessions whose principal or authentication attributes contain the value.                                                                                                                                                                                                           |
| `previewLogout`               | `ticketId` (string)                                 | Preview which applications would receive a [single logout](Logout-Single-Signout.html) notification if the single sign-on session ended now, with the logout URL and channel of each. Nothing is logged out.                                                                                                           |
| `getRegistryStatistics`       | none                                                | Report how many single sign-on sessions, service tickets and tickets the ticket registry holds, and how many registered services are loaded.                                                                                                                                                                           |
| `findTenant`                  | `tenantId` (string)                                 | Look up a [tenant](../multitenancy/Multitenancy-Overview.html) and its policies, along with the names of the settings it overrides; setting values are never returned.                                                                                                                                                 |
| `findAuditRecords`            | `username` (string), `days` and `action` (optional) | Return the most recent [audit records](../audits/Audits.html) of a person, optionally limited to a number of days and to actions that contain the given text.                                                                                                                                                          |
| `findAuthenticationEvents`    | `username` (string), `days` (optional)              | Return the most recent recorded [authentication events](../authentication/Configuring-Authentication-Events.html) of a person, such as single sign-on sessions created and destroyed and authentication failures, with the client IP address, user agent and location of each. Device fingerprints are never returned. |

The `service` parameter is the application URL, such as the `service` parameter an application sends to CAS at login.
Access evaluations are recorded in the [audit log](../audits/Audits.html) like any other access strategy decision, and
session summaries are limited to the most recent fifty sessions; the full ticket of a session can be fetched with `findTicket`.

Multifactor authentication triggers are evaluated against the person's resolved attributes and a request that carries
no parameters, headers, cookies or query string, so triggers that depend on the HTTP request, such as a request parameter or header,
are not activated during the evaluation. Counting tickets may require a scan of the whole ticket registry,
depending on the registry in use.

Audit records can only be returned when CAS is configured with an audit storage option that supports querying,
such as a database or Redis; the default audit log that is written to the CAS logs cannot be searched.
Search results are limited to the first fifty matches, and audit lookups to the most recent hundred records.

Authentication events are only returned when CAS records them in an
[events repository](../authentication/Configuring-Authentication-Events.html), such as memory, a database or Redis;
lookups are limited to the most recent hundred events.

### Optional Tools

Additional tools become available when the feature they examine is included in the CAS build. They are listed by the MCP
server only when the corresponding module is present, and each group can be turned off via feature toggles.

| Tool                    | Requires                                                                           | Parameters                                | Description                                                                                                                                                                                                                                                                                   |
|-------------------------|------------------------------------------------------------------------------------|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `findConsentDecisions`  | [Attribute consent](../integration/Attribute-Release-Consent.html)                 | `username` (string)                       | Return the consent decisions of a person: the application, when consent was given, the reminder settings and the names of the attributes consented to. Attribute values are never returned.                                                                                                   |
| `resolveSamlMetadata`   | [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html) | `entityId` (string)                       | Resolve the metadata of a service provider the way CAS does, and report the matching registration and metadata source, the validity period, assertion consumer and single logout endpoints, name id formats and certificates with their expiration dates, or the error when resolution fails. |
| `findOidcClient`        | [OpenID Connect](../authentication/OIDC-Authentication.html)                       | `clientId` (string)                       | Summarize an OAuth or OpenID Connect client registration: redirect URI pattern, grant and response types, scopes, client authentication, token settings, issuer and ID token settings. Client secrets are never returned.                                                                     |
| `listJwksKeys`          | [OpenID Connect](../authentication/OIDC-Authentication.html)                       | none                                      | List the keys of the CAS OpenID Connect keystore with their key id, type, algorithm, use and rotation state. Key material is never returned.                                                                                                                                                  |
| `findTrustedDevices`    | [Trusted devices](../mfa/Multifactor-TrustedDevice-Authentication.html)            | `username` (string)                       | Return the devices a person has registered as trusted for multifactor authentication: the device name, the multifactor authentication provider, when it was registered, when it expires and whether it has expired. Device fingerprints and record keys are never returned.                   |
| `findSurrogateAccounts` | [Surrogate authentication](../authentication/Surrogate-Authentication.html)        | `username` (string), `service` (optional) | Return the accounts a person may impersonate, optionally for an application, and whether the person may impersonate any account.                                                                                                                                                              |
| `previewInterrupt`     | [Authentication interrupts](../webflow/Webflow-Customization-Interrupt.html) | `username`, `service` (string) | Ask each interrupt source in order which notification CAS would show the person after authenticating to the application, and report the first interrupt with its message, links and whether it blocks the login. The interrupt policy of the matching registered service is reported, but its attribute and script conditions are not evaluated. |
| `evaluateAuthenticationRisk` | [Risk-based authentication](../authentication/Configuring-RiskBased-Authentication.html) | `username`, `service`, `clientIpAddress`, `userAgent` (string), `geoLocation` (optional) | Score a login attempt from the given client against the person's recorded sign-ins, reporting each risk calculator's score, the overall score, the threshold and whether CAS would treat the attempt as risky. |

Interrupt sources run as they do at login: a source that fails is recorded and skipped, and sources such as a REST
endpoint or a Groovy script are called for real, so `previewInterrupt` is advertised with the `openWorldHint` annotation.
Risk is scored for the client details passed to `evaluateAuthenticationRisk`, never those of the MCP request, which describe
the operator; a person with no recorded sign-ins is always regarded as risky, and each evaluation is recorded in the
[audit log](../audits/Audits.html).

{% include_cached featuretoggles.html features="AI.mcp-consent,AI.mcp-saml-idp,AI.mcp-oidc,AI.mcp-trusted-mfa,AI.mcp-surrogate,AI.mcp-interrupt,AI.mcp-electrofence" %}

## Client Configuration

Most MCP clients can connect to a remote Streamable HTTP server using a configuration similar to the following:

```json
{
  "mcpServers": {
    "cas": {
      "type": "http",
      "url": "https://sso.example.org/cas/mcp",
      "headers": {
        "Authorization": "Basic <base64-encoded username:password>"
      }
    }
  }
}
```

## Feature Toggle

The MCP server tools are enabled by default when the module is present, and can be turned off via feature toggles.
When turned off, CAS registers no tools and no longer opens the MCP endpoint to authenticated users.

{% include_cached featuretoggles.html features="AI.mcp" %}
