---
name: cas-passwordless-webauthn
description: "Review and change passwordless authentication (token repositories, webflow) and FIDO2 WebAuthn/passkeys (Yubico webauthn-server-core, attestation, backup flags, passkey endpoints, conditional create, webauthn.js). Use when touching support/cas-server-support-passwordless* or support/cas-server-support-webauthn*."
---

# Passwordless and FIDO2 WebAuthn

## Passwordless authentication review discipline

- `AcceptPasswordlessAuthenticationAction` must not compare tokens itself. The submitted token goes to the
  `AuthenticationManager` as a `OneTimePasswordCredential`, so wrong tokens are audited (`AUTHENTICATION_FAILED`, principal =
  username); an earlier version compared first and only authenticated a match, which left failed guesses unaudited.
- The same credential is authenticated more than once per login: by the action, again by `DefaultCasDelegatingWebflowEventResolver`
  (it re-authenticates whatever credential is in the flow), and a third time by `ServiceTicketRequestWebflowEventResolver` when an
  SSO session exists. So the handler must not consume the token. The action consumes it after `super.doExecuteInternal` returns a
  non-failure event and before any ticket exists (the resolver only puts the result builder; the TGT is created in a later state),
  and treats `deleteToken(...) == false` as a lost race. Removing the credential from the flow to avoid re-authentication breaks the
  SSO/renew path, which then resolves the principal from the existing session.
- View-state entry actions run on every re-entry. `STATE_ID_PASSWORDLESS_DISPLAY` creates and sends a token on entry, and the
  accept failure transition re-enters it, so any change to the failure path changes how many emails/SMS a guess costs.
- The MFA branch replacing the token is by design: there MFA is the only factor, and deployments either disable device
  registration or put it behind MFA. Do not report `DetermineMultifactorPasswordlessAuthenticationAction` building a
  credential-less authentication as a bypass.
- View-state entry action results are ignored, so the create-token action returning `error()` does not change the flow; what it
  does decide is whether a token is stored. `emailToken`/`smsToken` report true only for an actual delivery.
- Token repositories must agree on the `PasswordlessTokenRepository` contract: `findToken` returns only unexpired tokens,
  `deleteToken` removes the token `findToken` returned and returns true only for the caller that removed it (affected rows,
  `getDeletedCount()`, `Map.remove`, a `2xx` from REST), and `clean()` removes expired rows. The encoded record is written before
  the store assigns an id, so `findToken` sets the id from the stored entity (`withId`); the id inside the decoded record is null. The existing
  `verifyCleaner` tests in the JPA and Mongo modules asserted that `clean()` removes a live token, i.e. they encoded the inverted
  query; they now assert the contract. `clean()` is global, so keep exactly one test per class calling it and give every other
  test a live token of its own.
- `PasswordlessTokenAuthenticationHandler.supports` accepts any `OneTimePasswordCredential` subclass (Duo passcodes included)
  because it is registered globally; check the credential type hierarchy before widening or relying on it.
- When reviewing wallet/passkey integration, check against the current WebAuthn Level 3 Recommendation and the W3C Digital
  Credentials API plus OpenID4VP 1.0 (DC API response modes) rather than older drafts.

## FIDO2 WebAuthn / passkeys review discipline

- Review against WebAuthn Level 3 (W3C Recommendation, 2026) and the Yubico `webauthn-server-core` version in
  `gradle/libs.versions.toml`; the ceremony logic lives in the vendored `com.yubico.core.WebAuthnServer`, the browser side in
  `support/cas-server-support-thymeleaf/.../static/js/webauthn/webauthn.js`.
- Already present, do not re-report: related origins (`/.well-known/webauthn`), conditional mediation on the passwordless
  user-id view, `signalAllAcceptedCredentials`/`signalCurrentUserDetails`, session-bound challenges, and the MFA handler's
  check that the asserted user handle maps to the in-progress principal.
- Yubico enforces UV only when the request says `REQUIRED`; `userVerificationRequirement` unset means UV is not checked.
- The library copies `backupEligible`/`backupState`/transports only if the repository returns them from `lookup` and
  `getCredentialIdsForUsername`, and it can only store transports the browser sent (`response.transports`).
- All WebAuthn puppeteer registration scenarios set `allow-untrusted-attestation=true`; the defaults reject `none`
  attestation, which synced passkey providers return. Keep that in mind before calling a scenario representative of defaults.
- `allow-untrusted-attestation` and `user-verification-requirement` are operator decisions: do not change their defaults;
  set them explicitly in tests and scenarios. UV `REQUIRED` needs a `ctap2` virtual authenticator (`cas.js` defaults to `u2f`).
- Yubico requests `credProps` on every registration by itself; read the answer from `RegistrationResult.isDiscoverable()`.
- `webauthn.js` uses the native `PublicKeyCredential.parse*OptionsFromJSON` and `toJSON()` with no fallback. Yubico parses
  with `FAIL_ON_UNKNOWN_PROPERTIES=true`; `toJSON()` output passes only because `publicKey`/`publicKeyAlgorithm` are ignored
  and `authenticatorData` is `@JsonIgnore`d, so check Yubico's `@JsonCreator`s before sending any new client field.
- `signalUnknownCredential` is destructive (Chrome's virtual authenticator deletes the passkey). Report
  `unknownCredential` only from the owning account's registrations (user handle, else request username), never from the
  node-local credential index in `BaseWebAuthnCredentialRepository`, which lags other nodes by up to a minute.
  User handles are random and resolve only through the account's registrations, so after the last passkey is deleted a
  login with it cannot be traced to an account; the account profile therefore signals on delete, from the device detail
  `relyingPartyId` (`WebAuthnUtils.determineRelyingPartyId` in the config module) and the device id (the credential id).
  Chrome's `u2f` virtual authenticators ignore signals; use `ctap2` (resident or not) to assert a signal in puppeteer.
- Verification here: Maven Central is blocked, so check Yubico APIs by cloning `github.com/Yubico/java-webauthn-server` at the
  version in `libs.versions.toml`. `webauthn.js` is too deep to stage; copy it under the ignored `build/` folder, stage that,
  and exercise it in Playwright's Chromium with a CDP virtual authenticator. After `device_commit_files`, check the file on
  the device (size or a grep): one commit reported success while the device kept the old content.
- `/.well-known/passkey-endpoints` follows the W3C Passkey Endpoints Working Draft (Jan 2026): 200, `application/json`, no
  redirect, `{}` allowed. CAS has no direct URL into WebAuthn registration, so the defaults point at the plain account
  profile (`/account`), never at a panel fragment such as `#divMfaRegisteredAccounts`, and only when
  `CasFeatureModule.FeatureCatalog.AccountManagement.isRegistered()`. `WebAuthnControllerMvcTests` enables account
  management, so its wired document carries both URLs; `{}` only appears with the feature off.
- Resident key: the discoverable button asks `REQUIRED`; the default button asks `PREFERRED` only when
  `allow-primary-authentication` is on (`WebAuthnServer.determineResidentKeyRequirement`), so MFA-only deployments do not
  spend security-key slots. `passwordless-login-passkey` registers with the default button to cover this.
- `isBrowserSupported()` in `webauthn.js` is synchronous on purpose (call sites test it directly) and must not require a
  platform authenticator: headless Chrome's virtual authenticators and security-key users would fail it.
- Backup flags live on the stored `RegisteredCredential` (`backupEligible`/`backupState`, deprecated-experimental in Yubico
  2.9 but stable in WebAuthn Level 3); Yubico rejects an assertion whose BE differs from a stored BE. `lookup`/`lookupAll`
  return the stored credential as is, so never rebuild it field by field; `updateSignatureCount` refreshes BS and fills BE
  only for records stored before BE was kept. Transports live on `CredentialRegistration` and go out through
  `getCredentialIdsForUsername`. Chrome's CDP virtual authenticator sets BE/BS via `defaultBackupEligibility`/`defaultBackupState`.
- Provider names: `CredentialRegistration.aaguid` (UUID string, absent for the all-zero AAGUID) is set at registration
  only; older records cannot recover it. `webauthn-passkey-providers.json` is a snapshot of
  `passkeydeveloper/passkey-authenticator-aaguids` `aaguid.json` (name, `icon_light`, `icon_dark`; upstream has no
  license file and may empty the list; icons are provider logos the list publishes for RP display), refreshed with
  `jq 'with_entries(.key |= ascii_downcase) | map_values({name, icon_light, icon_dark} | with_entries(select(.value != null)))'`
  and kept pretty-printed. `WebAuthnUtils.getPasskeyProvider` keeps only SVG data URIs (light first). The device
  manager uses the name only when attestation metadata names no device; `SuccessfulRegistrationResult.passkeyProvider`
  carries name and icon to the registration page, `SuccessfulAuthenticationResult.passkeyProvider` (the credential just
  used) to the login page. The account profile gets the icon as the device detail `icon` holding the path of
  `GET /webauthn/passkey-providers/{aaguid}/icon` (permitAll, sandboxed CSP, `nosniff`), never the data URI: devices
  live in flow scope and webflow execution state is client-side, so data URIs would bloat every request. Chrome's
  virtual authenticators send unlisted or zero AAGUIDs, so scenarios cannot show a provider name.
- `#device-icon` on the registration and login pages only has an image when attestation metadata supplies `imageUrl`
  (the bundled Yubico list, remote PNGs) or the passkey provider has an icon; `showDeviceInfo` hides it otherwise or
  when the image fails to load.
- Passkey upgrade (conditional create): states exist only when `passkey-upgrade-enabled`, `allow-primary-authentication`
  and `allow-untrusted-attestation` are all on (no warning otherwise, by design); `startConditionalRegistration` re-checks
  them. The check action reads the login flow's own `flowScope.credential` (MFA subflows write request/conversation scope,
  which `WebUtils.getCredential` would return) and requires a `UsernamePasswordCredential` in the authentication.
  `user.name` is the typed username but the stored `userIdentity.name` is always the principal id; the typed name is kept
  in `CredentialRegistration.userEntityName` (null when equal) so `signalCurrentUserDetails` sends the name of the
  credential just used instead of renaming the passkey. `excludeCredentials` is rebuilt from the principal id.
  Headless Chrome reports `conditionalCreate`; with a CDP virtual authenticator the conditional `create()` never settles,
  so the page continues on its timeout (5 s); with none it rejects at once.
