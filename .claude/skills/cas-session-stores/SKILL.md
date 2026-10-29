---
name: cas-session-stores
description: "CAS session stores and browser-side state: CasSessionStore implementations (HTTP session, browser storage, ticket registry), the pac4j adapter used by OAuth/OIDC/UMA and delegated authentication, and the BROWSER_STORAGE cookie fallback in cas.js. Use when touching session stores, pending-request storage (SAML2 IdP, OAuth/OIDC, Duo, delegation) or browser storage reads and writes."
---

# Session stores and browser storage

Session stores kept on the ticket registry, and their limits with the stateless registry, are in `cas-ticket-registries`.

## CasSessionStore implementations

- Pending AuthnRequests live in `samlIdPDistributedSessionStore`, a `CasSessionStore` (`api/cas-server-core-api-web`)
  whose implementations sit in `core/cas-server-core-web-api` (`org.apereo.cas.web.session`): `DefaultCasSessionStore`
  (http session, prefix `SamlServerSupport`), `BrowserStorageCasSessionStore` (payload format unchanged from the pac4j
  store, context `SamlIdPSessionStore`) and the deprecated `TicketRegistryCasSessionStore`. The store is shared:
  Duo uses `BrowserStorageCasSessionStore` directly, and OAuth/OIDC/UMA and delegated authentication keep their pac4j
  `SessionStore` beans as `CasSessionStorePac4jAdapter` over these implementations (same prefixes). pac4j needs the
  full contract (session id, destroy, renew, and a store bound to a tracked `HttpSession` for back-channel logout), so
  keep those semantics when changing an implementation.

## Browser storage cookie fallback

- `BROWSER_STORAGE` state (Duo Universal Prompt, SAML IdP, stateless ticket registry, account profile) goes through the
  shared `storage/casBrowserStorageWriteView` and `ReadView`, which call `writeToBrowserStorage` / `readFromBrowserStorage`
  in `cas.js`. When local or session storage throws or is unavailable, the payload is written to chunked cookies
  (`CasBrowserStorage_<context>_<i>` plus a `_n` count cookie, Secure on https, `SameSite=Lax`, host-only, path = CAS
  context path from `casBrowserStorageCookiePath` in `fragments/scripts.html`). The read view merges them (cookies win,
  since a successful storage write clears the cookie copy) and posts the usual `browserStorage` parameter, so no server
  read path changed. The login fails only when cookies are refused too.
- The context segment of the cookie name is encoded identically in `cas.js` (`encodeBrowserStorageCookieToken`) and in
  `WebUtils.removeBrowserStorageCookies`: keep `A-Z a-z 0-9 - . ~`, percent-encode every other UTF-8 byte, so `_` is an
  unambiguous separator. Change both together.
- Do not clear the cookies on read generically: the stateless ticket registry reads its TGT payload on every login.
  Single-use consumers clear their own context server-side (Duo does, in `DuoSecurityUniversalPromptValidateLoginAction`);
  logout and the 422 page clear all of them.
- The Duo payload is the serialized, encrypted flow state, likely tens of KB and 10+ cookies (not yet measured in a run). CAS accepts 500KB headers, but
  fronting proxies often cap a header at 8-16KB; that is deployment configuration, documented on the Duo page.
- Scenario `mfa-duo-universal-login-storage-fails` covers both halves: storage broken with cookies falling back to a full
  Duo login, and storage plus script cookies broken showing the error panel.
