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

Every tool call and resource read is recorded in the [audit log](../audits/Audits.html) under the MCP user who made it,
with the action `MCP_TOOL_CALL_SUCCESS`, `MCP_TOOL_CALL_DENIED` or `MCP_TOOL_CALL_FAILED` and, as the resource, the tool name
or resource URI along with its arguments. Calls refused for lack of the `MCP_ADMIN` role are recorded too.

{% include_cached featuretoggles.html features="AI.mcp-audit" %}

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
The tools return sensitive data, such as the attributes CAS resolves and releases for a person, single sign-on
session identifiers and full application definitions. Only grant the <code>MCP_ADMIN</code> role to trusted operators, keep the MCP
endpoint on a protected network and always use TLS.
</p></div>

## Tools

The following tools are available. All tools are read-only and are advertised to clients with the
`readOnlyHint` annotation. When the requested object cannot be found, the result says why in a `reason` field, except for
`findService` and `matchService`, which return nothing.

| Tool                          | Parameters                                                                               | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|-------------------------------|------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `findService`                 | `serviceId` (long)                                                                       | Look up a [registered service](../services/Service-Management.html) by its numeric id. The definition is returned as JSON, in the same format used by the JSON service registry, with client secrets, passwords, keys and inline key sets replaced by `<redacted>`.                                                                                                                                                                                                                                                                                                              |
| `matchService`                | `service` (string)                                                                       | Find the registered service that matches an application URL, the same way CAS does at login, and return its definition as JSON with secrets replaced by `<redacted>`.                                                                                                                                                                                                                                                                                                                                                                                                            |
| `evaluateAccess`              | `username`, `service` (string)                                                           | Evaluate the [access strategy](../services/Configuring-Service-Access-Strategy.html) of the matching registered service against the person's resolved attributes, and explain why access is denied.                                                                                                                                                                                                                                                                                                                                                                              |
| `previewAttributeRelease`     | `username`, `service` (string)                                                           | Preview the username and [attributes](../integration/Attribute-Release-Policies.html) CAS would release to the application for the person, along with the access decision.                                                                                                                                                                                                                                                                                                                                                                                                       |
| `listMultifactorProviders`    | none                                                                                     | List the configured [multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html) with their ids, names, order and failure modes.                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `evaluateMultifactorTriggers` | `username`, `service` (string), `clientIpAddress`, `userAgent`, `geoLocation` (optional) | Run the [multifactor authentication triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html) in order for the person and application, and report which provider is required and which trigger selected it.                                                                                                                                                                                                                                                                                                                                                         |
| `searchServices`              | `query` (string)                                                                         | Search registered services whose name, service id pattern, description or numeric id contains the text, and return a summary of each.                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `findSessions`                | `username` (string)                                                                      | Summarize the active single sign-on sessions of a person: a session reference, when each was created and last used, the authentication handlers and the applications accessed.                                                                                                                                                                                                                                                                                                                                                                                                   |
| `findTicket`                  | `ticketId` (string), `username` (optional)                                               | Look up a ticket by its id in the [ticket registry](../ticketing/Configuring-Ticketing-Components.html), or a single sign-on session by its session reference and the person's username, and summarize it: type, creation and last use, number of uses, expiration policy, the principal with the names of its principal and authentication attributes, the application of a service ticket, the session reference, and the applications of a single sign-on session. Attribute values, session ids and the serialized ticket are never returned. Expired tickets are not found. |
| `findPerson`                  | `username` (string)                                                                      | Resolve a person by username through the [attribute repositories](../integration/Attribute-Resolution.html) and return the principal with its attributes.                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `findSessionsWithAttribute`   | `attributeName`, `attributeValue` (string)                                               | Summarize the active single sign-on sessions, with their session reference and principal, whose principal or authentication attributes contain the value.                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `previewLogout`               | `ticketId` (string), `username` (optional)                                               | Preview which applications would receive a [single logout](Logout-Single-Signout.html) notification if the single sign-on session ended now, with the logout URL and channel of each. Takes a ticket-granting ticket id, or a session reference with the person's username. Nothing is logged out.                                                                                                                                                                                                                                                                               |
| `getRegistryStatistics`       | none                                                                                     | Report how many single sign-on sessions, service tickets and tickets the ticket registry holds, and how many registered services are loaded.                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `findTenant`                  | `tenantId` (string)                                                                      | Look up a [tenant](../multitenancy/Multitenancy-Overview.html) and its policies, along with the names of the settings it overrides; setting values are never returned.                                                                                                                                                                                                                                                                                                                                                                                                           |
| `findAuditRecords`            | `username` (string), `days` and `action` (optional)                                      | Return the most recent [audit records](../audits/Audits.html) of a person, optionally limited to a number of days and to actions that contain the given text.                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `findAuthenticationEvents`    | `username` (string), `days` (optional)                                                   | Return the most recent recorded [authentication events](../authentication/Configuring-Authentication-Events.html) of a person, such as single sign-on sessions created and destroyed and authentication failures, with the client IP address, user agent and location of each. Device fingerprints are never returned.                                                                                                                                                                                                                                                           |
| `describeDeployment`          | none                                                                                     | Describe the deployment: CAS version, server name and prefix, active profiles, the feature modules that are turned on, the CAS modules on the classpath, ticket and service registry types, authentication handlers and policies, attribute repositories, and which optional MCP tool groups are active along with the setting that turns each one on.                                                                                                                                                                                                                           |
| `explainLogin`                | `username`, `service` (string), `clientIpAddress`, `userAgent`, `geoLocation` (optional) | Walk through a login in one call, in the order CAS applies each step: the registered service and its access strategy, the risk of the attempt, the multifactor authentication provider, the interrupt and the released username and attributes. Returns the outcome and, for each step, its status and the result of the tool that evaluates it. Passwords are not checked.                                                                                                                                                                                                      |
| `validateServiceDefinition`   | `definition` (string), `urls` (optional list)                                            | Check a proposed registered service definition, given as JSON, without saving it: whether CAS can read it, whether its service id pattern is valid, whether its id or name collides with an existing service, and for each URL whether it matches, which existing service matches today and which of the two CAS would pick by evaluation priority and order. The definition is not returned.                                                                                                                                                                                    |
| `findThrottledLogins`         | `username`, `clientIpAddress` (optional)                                                 | Return the failed login attempts that [authentication throttling](../authentication/Configuring-Authentication-Throttling.html) tracks, with the username, client IP address, last failure, end of the lock and whether each is throttled now, along with the failure threshold, range and lock window.                                                                                                                                                                                                                                                                          |
| `findAttributeSources`        | `username` (string)                                                                      | Ask each [attribute repository](../integration/Attribute-Resolution.html) separately which attributes it returns for a person, and report the attribute names and any error per repository. Attribute values are not returned; `findPerson` has them.                                                                                                                                                                                                                                                                                                                            |
| `findAttributeDefinitions`    | `query` (optional)                                                                       | List the [attribute definitions](../integration/Attribute-Definitions.html) whose key, name or source attribute contains the text, with their type, scoping, encryption, canonicalization and hashing settings. Scripts are not returned, only whether a definition is scripted.                                                                                                                                                                                                                                                                                                 |

The `service` parameter is the application URL, such as the `service` parameter an application sends to CAS at login.
Access evaluations are recorded in the [audit log](../audits/Audits.html) like any other access strategy decision, and
session summaries are limited to the most recent fifty sessions; more about a session can be fetched with `findTicket`.

A ticket-granting ticket id grants access to the single sign-on session it identifies, so the tools never return one.
Sessions are identified by a *session reference* instead, such as `session:3f2a…`, derived from the ticket id with a
one-way digest. `findTicket` and `previewLogout` accept a session reference together with the username of the person,
and look for it among that person's sessions only. Ticket ids passed to the tools are masked in the audit log the same way
CAS masks them in its own audit records.

Multifactor authentication triggers are evaluated against the person's resolved attributes and a request that carries
no parameters, headers, cookies or query string, so triggers that depend on the HTTP request, such as a request parameter or header,
are not activated during the evaluation. The optional client IP address, user agent and location describe the login attempt
for adaptive triggers: they become the client address, the `User-Agent` header and the `geolocation` parameter that the
triggers see. Without them the client is unknown; the address of the MCP request, which belongs to the operator, is never used. Counting tickets may require a scan of the whole ticket registry,
depending on the registry in use.

Audit records can only be returned when CAS is configured with an audit storage option that supports querying,
such as a database or Redis; the default audit log that is written to the CAS logs cannot be searched.
Search results are limited to the first fifty matches, and audit lookups to the most recent hundred records.

Authentication events are only returned when CAS records them in an
[events repository](../authentication/Configuring-Authentication-Events.html), such as memory, a database or Redis;
lookups are limited to the most recent hundred events.

`explainLogin` runs `evaluateAccess`, `evaluateAuthenticationRisk`, `evaluateMultifactorTriggers`, `previewInterrupt` and
`previewAttributeRelease` in turn and stops at the first step that denies the login, with the outcome `SERVICE_NOT_REGISTERED`,
`ACCESS_DENIED`, `BLOCKED_AS_RISKY`, `BLOCKED_BY_INTERRUPT` or `LOGIN_SUCCEEDS`. Each step has the status `PASSED`, `TRIGGERED`
(a requirement such as multifactor authentication is added), `BLOCKED`, `SKIPPED` or `FAILED`. Risk is evaluated only when a
client IP address and user agent are given and risk-based authentication is installed, and a step whose module is not installed
is skipped. The call is recorded once in the audit log as `explainLogin`; the tools it runs are not recorded separately.

`findThrottledLogins` reads the throttling records that CAS keeps in memory or in Hazelcast. Throttling backed by a database,
MongoDB or Redis keeps its records in the audit trail instead, which `findAuditRecords` returns with the action
`THROTTLED_LOGIN_ATTEMPT`; the `auditBased` field of the result says when that is the case. At most a hundred records are returned,
most recent first.

### Structured Output

Every tool except `findService` and `matchService`, whose definitions are free-form, advertises an `outputSchema` in `tools/list`
and returns its result as `structuredContent`, along with the same JSON as text for clients that do not read structured content.
Fields without a value are left out of the result rather than set to `null`.

### Optional Tools

Additional tools become available when the feature they examine is included in the CAS build. They are listed by the MCP
server only when the corresponding module is present, and each group can be turned off via feature toggles.

| Tool                         | Requires                                                                                 | Parameters                                                                               | Description                                                                                                                                                                                                                                                                                                                                      |
|------------------------------|------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `findConsentDecisions`       | [Attribute consent](../integration/Attribute-Release-Consent.html)                       | `username` (string)                                                                      | Return the consent decisions of a person: the application, when consent was given, the reminder settings and the names of the attributes consented to. Attribute values are never returned.                                                                                                                                                      |
| `resolveSamlMetadata`        | [SAML2 identity provider](../authentication/Configuring-SAML2-Authentication.html)       | `entityId` (string)                                                                      | Resolve the metadata of a service provider the way CAS does, and report the matching registration and metadata source, the validity period, assertion consumer and single logout endpoints, name id formats and certificates with their expiration dates, or the error when resolution fails.                                                    |
| `findOidcClient`             | [OpenID Connect](../authentication/OIDC-Authentication.html)                             | `clientId` (string)                                                                      | Summarize an OAuth or OpenID Connect client registration: redirect URI pattern, grant and response types, scopes, client authentication, token settings, issuer and ID token settings. Client secrets are never returned.                                                                                                                        |
| `listJwksKeys`               | [OpenID Connect](../authentication/OIDC-Authentication.html)                             | none                                                                                     | List the keys of the CAS OpenID Connect keystore with their key id, type, algorithm, use and rotation state. Key material is never returned.                                                                                                                                                                                                     |
| `findTrustedDevices`         | [Trusted devices](../mfa/Multifactor-TrustedDevice-Authentication.html)                  | `username` (string)                                                                      | Return the devices a person has registered as trusted for multifactor authentication: the device name, the multifactor authentication provider, when it was registered, when it expires and whether it has expired. Device fingerprints and record keys are never returned.                                                                      |
| `findSurrogateAccounts`      | [Surrogate authentication](../authentication/Surrogate-Authentication.html)              | `username` (string), `service` (optional)                                                | Return the accounts a person may impersonate, optionally for an application, and whether the person may impersonate any account.                                                                                                                                                                                                                 |
| `previewInterrupt`           | [Authentication interrupts](../webflow/Webflow-Customization-Interrupt.html)             | `username`, `service` (string)                                                           | Ask each interrupt source in order which notification CAS would show the person after authenticating to the application, and report the first interrupt with its message, links and whether it blocks the login. The interrupt policy of the matching registered service is reported, but its attribute and script conditions are not evaluated. |
| `evaluateAuthenticationRisk` | [Risk-based authentication](../authentication/Configuring-RiskBased-Authentication.html) | `username`, `service`, `clientIpAddress`, `userAgent` (string), `geoLocation` (optional) | Score a login attempt from the given client against the person's recorded sign-ins, reporting each risk calculator's score, the overall score, the threshold and whether CAS would treat the attempt as risky.                                                                                                                                   |
| `findOneTimeTokenAccounts`   | [Google Authenticator](../mfa/GoogleAuthenticator-Authentication.html)                   | `username` (string)                                                                      | Return the one-time password devices a person has registered: the device name, when it was registered and last used, where the registration came from and how many scratch codes are left. Secret keys, validation codes and scratch codes are never returned.                                                                                   |
| `findWebAuthnDevices`        | [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html)                              | `username` (string)                                                                      | Return the security keys and passkeys a person has registered: the nickname, registration time, transports, whether it is a discoverable credential, the AAGUID of the authenticator model and the signature count. Credential ids, user handles and public keys are never returned.                                                             |
| `findPasswordlessAccount`    | [Passwordless authentication](../authentication/Passwordless-Authentication.html)        | `username` (string)                                                                      | Return the passwordless account of a person: masked email address and phone number, whether a password is still requested, the selection menu, multifactor and delegated authentication eligibility, allowed identity providers and the account source. Account attributes are not returned.                                                     |
| `listIdentityProviders`      | [Delegated authentication](../integration/Delegate-Authentication.html)                  | none                                                                                     | List the external identity providers CAS can delegate to with their name, type, display name, callback URL, auto-redirect setting and principal attribute. Client secrets and other provider settings are never returned.                                                                                                                        |
| `findAuthorizableResources`  | [Heimdall](../authorization/Heimdall-Authorization-Overview.html)                        | `namespace` (optional)                                                                   | Return the resources of a Heimdall namespace: URI pattern and method, AuthZEN resource type, id pattern and actions, whether every policy must grant, the policy types and the property names. Policy definitions and property values are never returned; without a namespace, the known namespaces are listed.                                  |

Interrupt sources run as they do at login: a source that fails is recorded and skipped, and sources such as a REST
endpoint or a Groovy script are called for real, so `previewInterrupt` is advertised with the `openWorldHint` annotation.
Risk is scored for the client details passed to `evaluateAuthenticationRisk`, never those of the MCP request, which describe
the operator; a person with no recorded sign-ins is always regarded as risky, and each evaluation is recorded in the
[audit log](../audits/Audits.html).

## Resources

The MCP server offers the CAS settings catalog as an [MCP resource](https://modelcontextprotocol.io/specification/2025-11-25/server/resources)
template, `cas://settings/{name}`, which clients read with `resources/read`, for example `cas://settings/cas.server.name`.
The JSON result describes the setting: its description, type, default value, the CAS module that provides it, whether it is
required, deprecated, or accepts durations, regular expressions or expressions, and whether it is defined in this deployment.
The value of a setting is never returned, since settings carry keys, passwords and other secrets. Reading a resource requires
the `ROLE_MCP_ADMIN` role/authority like any tool.

To find the name of a setting, the `searchSettings` tool takes a `query` of at least three characters, matched against
setting names and descriptions (every word must match), and an optional CAS `module` such as `cas-server-support-ldap`. It returns up to
fifty settings with their name, a short description, module, type, deprecation and the `cas://settings/{name}` URI to read.

Registered services are offered as the resource template `cas://services/{id}`, such as `cas://services/1001`, which returns
the definition as JSON with the same secret redaction as `findService`.

Resources are advertised only when the resource capability of the MCP server is turned on, which CAS does by default with
`spring.ai.mcp.server.capabilities.resource=true`{: .cas-setting}.

Clients that support [argument completion](https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion)
can complete the setting name of `cas://settings/{name}` from at least two typed characters, and the service id of
`cas://services/{id}` from the first digits of the id or part of the service name. MCP completion applies to resource
templates and prompts only, not to tool parameters. Completion is turned on by default with
`spring.ai.mcp.server.capabilities.completion=true`{: .cas-setting}, and requires the `ROLE_MCP_ADMIN` role/authority.

{% include_cached featuretoggles.html features="AI.mcp-settings" %}

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

### Browser Clients

MCP clients that run in a web browser, such as web-based MCP inspectors and playgrounds, call the MCP endpoint from another
origin, so the browser first sends a CORS preflight request. CAS does not answer cross-origin requests unless
[CORS](../planning/Security-Guide.html#cors) is turned on; without it, such clients report a CORS or network error even though
the server is reachable. Clients that do not run in a browser, such as desktop applications and command-line tools,
are not affected.

When CORS is turned on for a browser client, allow the origin of that client, the `POST`, `GET`, `DELETE` and `OPTIONS`
methods, and the `Authorization`, `Content-Type`, `Accept`, `Mcp-Session-Id`, `MCP-Protocol-Version` and `Last-Event-ID`
request headers. The `Mcp-Session-Id` response header must also be exposed; otherwise the browser hides the session id
returned by `initialize` and every later call fails.

{% include_cached casproperties.html properties="cas.http-web-request.cors" %}

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
CORS settings apply to every CAS endpoint, not only to the MCP endpoint. Allow only the exact origins of the clients you trust,
never all origins, and do not allow credentials: browser clients send their credentials in the <code>Authorization</code>
header, and allowing credentials would let other origins use a CAS session cookie of the browser.
</p></div>

## Feature Toggle

The MCP server tools are enabled by default when the module is present, and can be turned off via feature toggles.
When turned off, CAS registers no tools and no longer opens the MCP endpoint to authenticated users.

{% include_cached featuretoggles.html features="AI.mcp" %}
