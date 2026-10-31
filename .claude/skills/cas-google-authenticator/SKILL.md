---
name: cas-google-authenticator
description: "Review and change Google Authenticator (GAuth) MFA credential repositories across backends (JPA, Redis, Mongo, DynamoDB, LDAP, JSON, REST, in-memory): owner-scoped deletes, encoding, counts, verification flags, the Redis index, crypto. Use when touching support/cas-server-support-gauth*."
---

# Google Authenticator MFA repositories

## Google Authenticator credential repositories (all backends)

- Review every backend against the same contract, with crypto on: `cas.authn.mfa.gauth.crypto.enabled` defaults to `true`, but every puppeteer scenario and the repository tests turn it off, so undecoded returns and double encryption never show up in CI.
- A device delete must be scoped to its owner. WebAuthn, YubiKey and Duo device managers delete by username and id; `OneTimeTokenCredentialDeviceManager` and `GoogleAuthenticatorDeleteAccountAction` delete by id alone, and ids are guessable (JPA sequence; `currentTimeMillis` before 42bcde1).
- `BaseOneTimeTokenCredentialRepository.encode()` mutates its argument. Never pass an object you will keep or save again; JPA `get(id)` and InMemory `get(id)` return stored, undecoded instances, and updating them double-encrypts.
- `count()` means devices in Redis, JPA, Mongo and DynamoDB, but users in InMemory, JSON and LDAP; check which a caller assumes.
- A repository read that fails must not look like "no devices": `OneTimeTokenAccountCheckRegistrationAction` sends empty or null results to enrollment (REST returns null, JSON returns empty on errors).
- When testing deletes or updates, store devices for two users and two devices per user, and assert the others survive; single-user tests hid the DynamoDB `GE` filter and the LDAP update bug.
- To make a range-filter bug visible in a DynamoDB delete test, pick keys that a wrong comparison would sweep up: the kept user's name sorts after the deleted one ("z…" vs "a…"), and the kept device ids are larger than the deleted id. Random UUIDs alone pass by luck half the time.
- DynamoDB tables keyed by `id` should delete with `DeleteItem` on that key, not a scan followed by deletes.
- Webflow actions and device managers that take a device id from the request must load it with the owner-scoped `get(principal.getId(), id)` and act only on what that returns. Unscoped `get(id)` / `delete(id)` are for the actuator and internal paths.
- Every repository read returns a decoded copy (JPA detaches before decoding); `update()` must persist `properties`, which the verification flags of the delete and confirm actions live in.
- The delete/confirm verification flags are stored on the account as `<flag>:<epoch millis>` and expire (`GoogleAuthenticatorAccountVerificationUtils`). They cannot live in flow or conversation scope: the login webflow keeps its execution state client-side, and the AJAX check and the form submit carry different execution keys.
- `CasReentrantLock.tryLock(supplier)` returns null when the lock is not acquired; never let that null reach a caller as "no accounts".
- `BaseOneTimeTokenCredentialRepository.encode()` returns an encoded copy and leaves its argument alone; store and return the copy, and read normalized values (lowercased username) from it, not from the argument.
- Per-tenant ciphers must be built once and reused: a cipher built from settings with blank keys generates its own keys, so a new instance per call cannot decode what the previous one encoded. `BaseOneTimeTokenCredentialRepository` caches them by tenant and crypto settings.
- WebAuthn FIDO metadata: `FidoMetadataDownloader.refreshBlob()` always downloads; `loadCachedBlob()` uses the cache until the BLOB's `nextUpdate`. CAS refreshes at startup and falls back to the cache on `IOException` (the FIDO service rate-limits with HTTP 429). `CompositeAttestationTrustSourceTests` downloads from the real service; a 429 there with no cache from an earlier run is an outage, not a regression.
- Active Directory keeps a single value of `description` on user objects. The LDAP GAuth repository therefore writes all of an entry's accounts as one JSON array value; writing one value per account loses all but one device on AD.
- A webflow action that validates a GAuth token and then updates the device must read the device again after validation: the validator updates its own copy (used scratch code, last-used time), and updating an older copy undoes that.

## Google Authenticator Redis repository

- Accounts live twice: the record at `CAS_TOKEN_ACCOUNT:<id>` and a copy as a member of the set `CAS_TOKEN_PRINCIPAL:<username>`. `get(username)`, `count(username)` and OTP validation read the set, not the record, so a delete that removes only the record leaves a device that is still listed and still validates.
- Reads must not repair the index. A read that removes set members whose record "does not exist" acts on whatever the read saw; with `read-from` sending reads to replicas, and the record and the set on different cluster slots, a lagging replica makes a live device look orphaned and the removal on the primary is permanent. Repair belongs on the explicit `delete(id)`: when the record is missing the owner is unknown, so it scans `CAS_TOKEN_PRINCIPAL:*` in batches and removes members with that id.
- Remove set members by the raw bytes Redis returned (`executePipelined(..., RedisSerializer.byteArray())` then `sRem`), not by re-serializing a deserialized account; the value serializer is LZ4 over JDK serialization, and a removal only matches if the bytes are identical.
- Spring Data Redis pipelining works on Lettuce cluster connections (`LettuceClusterConnection` does not override `openPipeline`) and key `SCAN` spans all nodes, so pipelines and scans here are cluster-safe.
