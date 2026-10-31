---
name: cas-attribute-consent
description: "Review and change attribute consent: ConsentDecision, the consent webflow gate, ConsentQueryResult, repositories and their lookups and deletes, decision decryption, dates and the consent scenarios. Use when touching support/cas-server-support-consent*."
---

# Attribute consent

## Attribute consent review discipline

- Start at `ConsentDecision`, not at a repository. `id` is a primitive `long` with
  `@GeneratedValue`, so only JPA generates one. That single field is the key in four
  backends: LDAP merges by it, Redis keys on `ConsentDecision:<principal>:<id>`, Mongo maps
  it to `_id` by naming convention, and DynamoDB uses it as the table HASH key. It used to
  stay `0` on every one of them, which collapsed each user (LDAP, Redis) or the whole
  deployment (Mongo, DynamoDB) onto a single decision. `DefaultConsentDecisionBuilder.build`
  now assigns a random positive id, JPA still replaces it with a generated one, and LDAP's
  `mergeDecision` assigns one when `id <= 0`. Anything that creates a `ConsentDecision`
  outside the builder -- the actuator's `/import`, say -- still arrives with id `0`.
- `BaseConsentRepositoryTests` used to call `decision.setId(1/100/200)` before every store,
  which is why the id defect never failed a test. `verifyMultipleDecisionsForPrincipal` now
  stores builder-produced decisions for two users across two services and then deletes by id
  and by principal, against every backend; `getOtherUser()` is overridden where the second
  user must already exist (LDAP). The class holds a `@ResourceLock` because
  `verifyDeleteRecordsForPrincipal` calls `deleteAll()` on the shared repository.
- Consent is a gate in the login webflow, not a filter at attribute release.
  `ConsentWebflowConfigurer` prepends `CheckConsentRequiredAction` to the login flow's
  `generateServiceTicket` state; the only other consumer is
  `SamlIdPConsentSingleSignOnParticipationStrategy`. Nothing in
  `RegisteredServiceAttributeReleasePolicy` reads a stored decision, so the consented set
  never constrains what is released, and any path that issues without the login flow --
  REST protocol tickets, proxy tickets, OIDC refresh/userinfo, the OpenID4VCI
  pre-authorized code grant -- releases attributes with no consent check at all.
- Follow `ConsentQueryResult`. It carries the decision, the service and now the resolved
  consentable attributes, and it used to be thrown away: `ConsentActivationStrategy` returned
  a bare boolean and `CheckConsentRequiredAction` reduced it to an event, so
  `prepareConsentForRequestContext` repeated the full `getConsentableAttributes` evaluation
  (person directory included) and the repository read in the same request. The strategy now
  returns the result and the action hands it to `prepareConsentForRequestContext`, which
  resolves nothing when the result carries attributes -- a result built by a custom strategy
  carries none and still falls back to resolving. What remains per consent interaction is one
  evaluation and read for the prompt, one of each inside `storeConsentDecision` on confirm,
  and one more from the `generateServiceTicketAfterConsent` clone that re-runs the check;
  `getRegisteredServiceForConsent`, and with it the access strategy, still runs twice per
  prompt. Count these by request before claiming a number.
- `DefaultConsentEngine.executeRepositoryOperation` calls `toConsentRepository(tenant)` on
  every operation, and `TenantJdbcConsentRepositoryBuilder` builds a DataSource plus a
  Hibernate `EntityManagerFactory` inside it, then destroys them. Check the multitenant
  path before judging consent performance; the single-tenant path is not representative.
- Repository lookups must be checked for operator and pattern correctness, not just for
  the right column names. `DynamoDbConsentFacilitator` matched SERVICE and ID with
  `ComparisonOperator.GE` (correct for the date ranges other CAS DynamoDB facilitators
  use it for, wrong for an equality lookup); it now uses `EQ`, but every read is still a
  full `scanPaginator` because `id` is the table's only key. `RedisConsentRepository`
  interpolated the raw principal into a SCAN glob; it now escapes glob characters and keeps
  only keys whose remainder is a numeric id, because the `:` delimiter alone lets principal
  `alice` reach the decisions of `alice:x`. Deleting one decision there is a `DEL` of the
  exact key rather than a scan.
- Deletes deserve the same reading as reads. `JpaConsentRepository.deleteConsentDecisions`
  used `getSingleResult()` and so silently deleted nothing for any user with more than one
  decision; it is a bulk JPQL delete now. `ChainingConsentRepository` -- which is what the
  `consentRepository` bean always is, wrapping every configured store -- stored to every
  repository but deleted with `anyMatch`, stopping at the first success; it now asks every
  repository. `RestfulConsentRepository` sent `id` where the documented contract and the
  test's own mock server say `decisionId`, so a conforming server read a single revocation
  as "revoke everything". Revocation failing quietly is worse than consent failing loudly.
- A decision that will not decipher used to be fatal: `getConsentableAttributesFrom` turned any
  failure into `IllegalArgumentException`, which propagated out of `isConsentRequiredFor` and out
  of the login flow, so key rotation or a node without the shared `cas.consent.core.crypto` keys
  locked the user out. It now logs and returns no attributes, which reads as a mismatch and
  re-prompts; the new decision is written back with the current keys. Weigh every failure on this
  path the same way -- the login flow has no recovery for an exception thrown from the consent
  check, so the question is always whether re-prompting is safe, not whether the error is correct.
- `ConfirmConsentAction` parsed `option`, `reminder` and `reminderTimeUnit` straight off the request
  with `Integer.parseInt` / `Long.parseLong` / `ChronoUnit.valueOf` and persisted them unvalidated, so
  a missing parameter was a 500 on the confirm POST and `ChronoUnit.FOREVER` or `ERAS` parsed fine and
  then threw from `createdDate.plus(...)` on every later login. Each value now falls back on the
  configured default, and the pair is rejected unless it can actually be applied to a date -- checked by
  applying it, since `LocalDateTime.isSupported` accepts `ERAS` and only the ISO era range rejects it.
  `DefaultConsentEngine.calculateReminderExpirationDate` is the second half of that: a stored reminder
  that cannot produce a date means consent is required, never an exception, which is what protects
  decisions that were written before this validation existed or through the actuator's import.
- `ConsentDecision.createdDate` is a zone-less `LocalDateTime` that one node writes and another compares
  against its own clock, so it is produced with `ZoneOffset.UTC`, not `ZoneId.systemDefault()`. Before that,
  the backends disagreed with each other: JPA, Redis, LDAP, DynamoDB and REST keep the wall-clock value
  verbatim, so a node in another zone read a decision as hours older or newer, while MongoDB was accidentally
  correct because Spring Data converts `LocalDateTime` through the system zone in both directions and so
  normalizes to the instant. The reminder units offered in the consent view go down to seconds, so that skew
  is not academic. `OneTimeToken.issuedDateTime` and the Google Authenticator token repositories still carry
  the same pattern with second-scale windows.
- `cas.consent.core.active` defaults to `true`, so adding the module turns consent on for
  every service that releases attributes. Weigh findings about the consent path as if it
  is always on.
- Wallet/OID4VCI integration is the real gap, not a defect in the consent modules:
  `BaseOidcVerifiableCredentialEncoder` reads `principal.getAttributes()` directly against
  the issuer's configured claim list, so a credential handed to a wallet carries no
  consent record, no per-claim choice, and no revocation. Selective disclosure exists in
  the SD-JWT encoder but the holder never chooses what is disclosed at issuance time.
- Puppeteer coverage: `attribute-consent` (in-memory repository, one user, one service),
  `multitenancy-consent-jdbc`, `saml2-idp-login-consent`, `saml2-idp-login-consent-with-sso`.
  No scenario exercises LDAP, Redis, Mongo, DynamoDB or REST consent, and none stores two
  decisions for the same user, which is exactly the shape every storage defect above needs.
