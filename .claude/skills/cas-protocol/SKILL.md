---
name: cas-protocol
description: "Review and change CAS protocol ticket validation and responses (CAS 1/2/3, proxy tickets, SAML 1.1): validate controllers, ticket-state correctness, attacker-controlled pgtUrl/service values, response wire formats and the covering scenarios. Use when touching service validation, proxy or SAML 1.1 code."
---

# CAS protocol (v1/v2/v3, SAML 1.1)

## CAS protocol (v1/v2/v3 + SAML 1.1) review discipline

- Entry points to trace, in order: `AbstractServiceValidateController` ->
  `DefaultCentralAuthenticationService.validateServiceTicket` / `createProxyGrantingTicket` /
  `grantProxyTicket` -> `ServiceValidationViewFactory` -> the `protocol/{2.0,3.0}` mustache
  templates or `Saml10SuccessResponseView`. Review the whole chain; the interesting defects are in
  the ordering between these stages, not inside any one class.
- Ticket-state correctness rule for this codebase: read, check and mutate a ticket inside the same
  `lockRepository.execute(...)` block. `LockRepository` defaults to a JVM-local registry, so any
  invariant that must hold across nodes needs registry-level atomicity, not a lock.
- Protocol beans reached from a controller are singletons. Never introduce (or leave) per-request
  mutable state on them; bind request parameters into a local object instead.
- Anything derived from `pgtUrl`, `service`/`targetService`, or `TARGET` is attacker-controlled at
  the point CAS first touches it. Authorization checks on those values must be anchored/exact
  matches, and scheme restrictions must be enforced in code, not documented in Javadoc.
- Check response wire formats against the published CAS 3.0 protocol spec and the SAML 1.1
  browser/artifact profile, not from memory: required error codes, `<cas:proxies>` ordering,
  `InResponseTo`/`Recipient`/`Conditions` on the SAML 1.1 response.
- Cross-check every protocol change against these puppeteer scenarios:
  `cas-validation-protocol-v2`, `cas-validation-protocol-v3`, `ticket-validation-cas-renew`,
  `ticket-validation-saml1`, `ticket-validation-casv3-pgt`, `ticket-validation-casv3-pgtiou`,
  `ticket-validation-casv3-pgt-multiple-pt`, `ticket-validation-casv3-pgt-stateless`,
  `single-logout-cas-backchannel`, `single-logout-cas-frontchannel`. Note that some of them assert
  current non-compliant behavior (`ticket-validation-cas-renew` asserts
  `code="INVALID_TICKET"` where the spec wants `INVALID_TICKET_SPEC`), so a compliance fix means
  updating the scenario in the same change.
- Coverage gaps to be aware of: no scenario exercises concurrent validation of a single ticket, and
  `ticket-validation-saml1` always supplies a `RequestID`, so the SAML 1.1 default `InResponseTo`
  path is untested.
