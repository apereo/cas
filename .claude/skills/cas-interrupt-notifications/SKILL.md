---
name: cas-interrupt-notifications
description: "Review and change interrupt notifications: InterruptWebflowConfigurer, inquirers, the tracking cookie, passive requests, SSO reuse by protocol modules. Use when touching support/cas-server-support-interrupt*."
---

# Interrupt notifications

## Interrupt notifications

- Review the whole webflow path in `InterruptWebflowConfigurer`, not a single action: inquiry is wired at several
  states, and the request-scoped finalized marker decides whether later checkpoints re-run the inquirers.
- Treat the tracking cookie as a user-held acknowledgement. Changes to finalize/tracking must keep blocked
  responses unacknowledgeable and must consider which principal the cookie was issued for.
- Puppeteer interrupt scenarios often log out between logins, which removes the tracking cookie; a regression step for tracking must log in again without logging out.
- Passive requests (CAS `gateway`, OIDC `prompt=none`, SAML `IsPassive`) must not render the interrupt view. When adding an event to an action that is prepended to an existing action state, add the matching transition to that state; an unmatched event silently continues to the next action.
- A check that only runs in the login webflow is bypassed wherever a protocol module reuses the SSO session directly (for example the SAML2 IdP). For interrupts this is accepted for now; do not close it by vetoing SSO participation from the interrupt module without agreeing on the approach with the maintainer.
- Inquirer errors currently fail open; any fix must state the intended behavior for blocking policies.
- Reloadable inquirer state (files, watchers) must be published atomically; a failed reload keeps the last good contents rather than failing open.
- Payloads map onto `InterruptResponse`, whose no-arg constructor means "interrupt". Parse external responses only after the status check, and test error bodies against random-port `MockWebServer` instances.
