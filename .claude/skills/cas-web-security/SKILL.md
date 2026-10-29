---
name: cas-web-security
description: "Rules for the single Spring Security filter chain CAS builds from CasWebSecurityConfigurer beans, shared by actuators, Palantir, the MCP server, OAuth/OIDC and others: declaring CSRF-protected endpoints and registering authentication entry points without breaking other modules. Use when adding or changing a CasWebSecurityConfigurer, CSRF protection or a login entry point in any module."
---

# The shared Spring Security chain

Every module contributes to one chain, so a configurer can break endpoints it never mentions. Area-specific notes stay
with their area: the chain as the actuator security boundary in `cas-actuators`, the MCP bearer filter and challenges
in `cas-mcp-server`.

## CSRF protection

- Never call `http.csrf(...)` in a `CasWebSecurityConfigurer`: all configurers share one `CsrfConfigurer`, so the last one
  would replace the others. Declare the paths with `getCsrfProtectedEndpoints()`: full paths within the application, covering
  every mapping of the controller (Spring 7's `PathPattern` accepts a leading `/**/`, as in the CIBA controller's own mappings); `CasWebSecurityConfigurerAdapter.configureCsrfProtection` protects their state-changing requests
  once, with the `casCsrfTokenRepository` bean and `CasCsrfTokenRequestHandler` (accepts the token masked or raw). CIBA
  verification is declared this way by `oidcProtocolEndpointConfigurer`.

## Authentication entry points

- Palantir's CAS login (`CasPalantirCasAuthenticationConfiguration`, `Palantir.cas-authentication`) shares the single
  Spring Security chain with actuators and the MCP server: register its entry point with
  `defaultAuthenticationEntryPointFor(entryPoint, /palantir/**)`, never `authenticationEntryPoint(...)`, which replaces every
  other entry point and sends API clients to the CAS login page. Spring Security uses a lone registered entry point for every
  request, so a test context needs a second one (an `AUTHENTICATED` actuator endpoint adds Basic) before an API request can
  be shown to escape the CAS redirect; `DashboardControllerTests.CasAuthenticationTests` checks both.
