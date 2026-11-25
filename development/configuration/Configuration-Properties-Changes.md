# Configuration Properties Changes

Source: https://apereo.github.io/cas/development/configuration/Configuration-Properties-Changes.html

This page compares the CAS configuration catalog of this version with the catalog of the version before it, and lists the CAS settings that were added, removed, replaced by others or given a new default value. It is generated when the documentation is built, so it complements the [release notes](https://apereo.github.io/cas/development/release_notes/Overview.html), which explain why things changed. Select a setting to see its description, type and module without leaving the page. The same list is published as JSON for scripts and tools, next to the [configuration catalog](https://apereo.github.io/cas/development/configuration/Configuration-Properties.html).

- [**140** added](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Changes.html#added-settings)
- [**53** removed](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Changes.html#removed-settings)
- [**0** replaced](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Changes.html#replaced-settings)
- [**82** new defaults](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Changes.html#changed-defaults)

Compared with **8.0.x**. As JSON: [settings-changes.json](https://apereo.github.io/cas/assets/data/development/settings-changes.json).

## Added Settings

### cas.audit

- `cas.audit.redis.max-age`default `P180D` Indicates how long audit records should be kept in Redis.

### cas.authn.adaptive

- `cas.authn.adaptive.ip-intel.vpn-detection.ban-relays`default `false` Ban IP addresses of relays such as iCloud Private Relay, in addition to VPNs and Tor exit nodes.
- `cas.authn.adaptive.ip-intel.vpn-detection.enabled`default `false` Whether client IP addresses should be examined by this service.
- `cas.authn.adaptive.ip-intel.vpn-detection.url`default `https://api.vpndetection.io/%s` URL endpoint of the service to make API calls.

### cas.authn.attribute-repository.mapped

- `cas.authn.attribute-repository.mapped.id`A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.mapped.order`The order of this attribute repository in the chain of repositories.
- `cas.authn.attribute-repository.mapped.people`Map of people and their attributes.
- `cas.authn.attribute-repository.mapped.state`default `active` Whether attribute resolution based on this source is enabled.

### cas.authn.mfa.web-authn

- `cas.authn.mfa.web-authn.core.hints`User-agent hints, in descending order of preference, sent with registration and authentication requests to guide the browser towards the kind of authenticator to offer first.
- `cas.authn.mfa.web-authn.core.passkey-enroll-url`URL of the page where a user creates a passkey for the account, published as enroll in the passkey endpoints document at /.well-known/passkey-endpoints, so that password managers can send users straight to it.
- `cas.authn.mfa.web-authn.core.passkey-manage-url`URL of the page where a user manages the account's passkeys, published as manage in the passkey endpoints document at /.well-known/passkey-endpoints.
- `cas.authn.mfa.web-authn.core.passkey-upgrade-enabled`default `false` Offer a passkey right after a password login: CAS shows a short page that asks the browser's password manager to create a passkey for the account on its own (WebAuthn conditional create), then continues as usual.

### cas.authn.oauth

- `cas.authn.oauth.core.allowed-resources`Resources, as regular expressions, that every OAuth client may request with the RFC 8707 resource parameter, in addition to those allowed by its registered service.

### cas.authn.oidc.client-attestation

- `cas.authn.oidc.client-attestation.challenge.enabled`default `false` Whether CAS hands out challenges at its challenge endpoint, advertised as challenge_endpoint, and requires the challenge claim of every client attestation proof of possession to carry one.
- `cas.authn.oidc.client-attestation.challenge.time-to-live`default `PT5M` How long a challenge may be used.
- `cas.authn.oidc.client-attestation.signing-alg-values-supported`Signing algorithms accepted for client attestations and their proofs of possession, advertised as client_attestation_signing_alg_values_supported and client_attestation_pop_signing_alg_values_supported.
- `cas.authn.oidc.client-attestation.trust-anchors`Locations of PEM-encoded certificates of the trust anchors that client attestations must chain to, such as those of wallet providers.

### cas.authn.oidc.core

- `cas.authn.oidc.core.pairwise-subject-salt`Salt used to calculate the pairwise subject identifiers of clients whose definition carries no salt of its own, such as clients that CAS registers itself, dynamically or through OpenID Federation, when they ask for pairwise subject ident...

### cas.authn.oidc.discovery

- `cas.authn.oidc.discovery.token-endpoint-auth-signing-alg-values-supported`Signing algorithms supported by the token endpoint for the JWT that authenticates the client under the private_key_jwt and client_secret_jwt authentication methods.

### cas.authn.oidc.dpop

- `cas.authn.oidc.dpop.nonce.enabled`default `false` Whether DPoP proofs must carry a nonce that CAS handed out.
- `cas.authn.oidc.dpop.nonce.time-to-live`default `PT5M` How long a nonce may be used.

### cas.authn.oidc.federation

- `cas.authn.oidc.federation.issued-trust-marks`Trust marks this entity issues as a trust mark issuer, each type with the entities it is issued to.
- `cas.authn.oidc.federation.issued-trust-marks[].delegation`The delegation that allows this entity to issue trust marks of a type it does not own, a signed JWT obtained from the owner of the type, given as a resource such as a file or a URL, or the JWT itself.
- `cas.authn.oidc.federation.issued-trust-marks[].expiration`default `P30D` How long an issued trust mark is valid.
- `cas.authn.oidc.federation.issued-trust-marks[].logo-uri`URL of a logo for the trust mark, published as logo_uri.
- `cas.authn.oidc.federation.issued-trust-marks[].ref`URL of human-readable information about the issuance of the trust mark, published as ref.
- `cas.authn.oidc.federation.issued-trust-marks[].subjects`Entity identifiers of the entities this trust mark type is issued to.
- `cas.authn.oidc.federation.issued-trust-marks[].trust-mark-type`The trust mark type identifier of the trust marks issued.
- `cas.authn.oidc.federation.max-authority-hints`default `5` The maximum number of authority hints followed from any entity configuration while resolving the trust chains of a relying party that registers automatically.
- `cas.authn.oidc.federation.max-concurrent-resolutions`default `10` The maximum number of resolutions that run at the same time, for automatic registration and the resolve endpoint together.
- `cas.authn.oidc.federation.max-path-length`default `3` The maximum number of intermediate entities between a relying party that registers automatically and a trust anchor.
- `cas.authn.oidc.federation.max-requests-per-resolution`default `50` The maximum number of requests a single resolution may send: entity configurations, subordinate statements, historical keys and trust mark status requests, counted together across the trust chains of the entity and those of the issuers o...
- `cas.authn.oidc.federation.required-trust-marks`Trust mark types that a relying party must hold to be registered automatically by this OpenID provider.
- `cas.authn.oidc.federation.resolution-timeout`default `PT10S` The longest time a single resolution may take, all of its requests together; every response is also limited in size.
- `cas.authn.oidc.federation.resolve-endpoint-authentication`default `none` Client authentication at the resolve endpoint of this entity as a trust anchor or intermediate.
- `cas.authn.oidc.federation.revocation.schedule.cron-expression`A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week.
- `cas.authn.oidc.federation.revocation.schedule.cron-time-zone`A time zone for which the cron expression will be resolved.
- `cas.authn.oidc.federation.revocation.schedule.enabled`default `true` Whether scheduler should be enabled to schedule the job to run.
- `cas.authn.oidc.federation.revocation.schedule.enabled-on-host`default `.*` Overrides SchedulingProperties#enabled property value of true if this property does not match hostname of CAS server.
- `cas.authn.oidc.federation.revocation.schedule.repeat-interval`default `PT2M` String representation of a repeat interval of re-loading data for a data store implementation.
- `cas.authn.oidc.federation.revocation.schedule.start-delay`default `PT15S` String representation of a start delay of loading data for a data store implementation.
- `cas.authn.oidc.federation.rotation.schedule.cron-expression`A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week.
- `cas.authn.oidc.federation.rotation.schedule.cron-time-zone`A time zone for which the cron expression will be resolved.
- `cas.authn.oidc.federation.rotation.schedule.enabled`default `true` Whether scheduler should be enabled to schedule the job to run.
- `cas.authn.oidc.federation.rotation.schedule.enabled-on-host`default `.*` Overrides SchedulingProperties#enabled property value of true if this property does not match hostname of CAS server.
- `cas.authn.oidc.federation.rotation.schedule.repeat-interval`default `PT2M` String representation of a repeat interval of re-loading data for a data store implementation.
- `cas.authn.oidc.federation.rotation.schedule.start-delay`default `PT15S` String representation of a start delay of loading data for a data store implementation.
- `cas.authn.oidc.federation.trust-anchors`The trust anchors whose trust chains an OpenID provider accepts when relying parties register automatically, and that an intermediate resolves entities to at its resolve endpoint, with their keys obtained out of band.
- `cas.authn.oidc.federation.trust-anchors[].entity-id`The entity identifier of the trust anchor.
- `cas.authn.oidc.federation.trust-anchors[].jwks`The public keys of the trust anchor, obtained out of band, as a JSON web key set: a resource such as a file or a URL, or the JSON document itself.
- `cas.authn.oidc.federation.trust-mark-endpoint-authentication`default `none` Client authentication at the trust mark endpoint of this entity as a trust mark issuer.
- `cas.authn.oidc.federation.trust-mark-issuers`For a trust anchor, the trust mark types of the federation and the entities accepted as their issuers, published as trust_mark_issuers in its entity configuration.
- `cas.authn.oidc.federation.trust-mark-issuers[].issuers`Entity identifiers of the entities accepted as issuers of trust marks of this type.
- `cas.authn.oidc.federation.trust-mark-issuers[].trust-mark-type`The trust mark type identifier.
- `cas.authn.oidc.federation.trust-mark-owners`For a trust anchor, the owners of trust mark types whose issuance is delegated to trust mark issuers, published as trust_mark_owners in its entity configuration.
- `cas.authn.oidc.federation.trust-mark-owners[].jwks`The keys of the owner, with which delegations are verified, as a JSON web key set or a resource such as a file or a URL that holds one.
- `cas.authn.oidc.federation.trust-mark-owners[].owner`Entity identifier of the owner of the trust mark type, who delegates its issuance to trust mark issuers.
- `cas.authn.oidc.federation.trust-mark-owners[].trust-mark-type`The trust mark type identifier.
- `cas.authn.oidc.federation.trust-marks`Trust marks issued to this entity, published in its entity configuration.
- `cas.authn.oidc.federation.unresolved-entity-retention`default `PT5M` How long an entity identifier whose trust chains could not be resolved is remembered.

### cas.authn.oidc.id-token

- `cas.authn.oidc.id-token.collector-script.location`The location of the resource.

### cas.authn.oidc.registration

- `cas.authn.oidc.registration.client-id-metadata-document.enabled`default `false` Whether clients may use client ID metadata documents.
- `cas.authn.oidc.registration.client-id-metadata-document.maximum-cache-duration`default `PT24H` The longest time a fetched document is used before CAS fetches it again, whatever its cache headers allow.
- `cas.authn.oidc.registration.client-id-metadata-document.maximum-clients`default `1000` The maximum number of clients registered from client ID metadata documents.
- `cas.authn.oidc.registration.client-id-metadata-document.minimum-cache-duration`default `PT5M` The shortest time a fetched document is used before CAS fetches it again, also used when the response carries no cache headers or forbids caching.
- `cas.authn.oidc.registration.client-id-metadata-document.retention`default `P30D` How long a client stays registered after its document was last fetched successfully.
- `cas.authn.oidc.registration.client-id-metadata-document.timeout`default `PT5S` Maximum time to wait for the document.

### cas.authn.oidc.vc

- `cas.authn.oidc.vc.issuer.batch-size`default `10` Maximum number of credential requests accepted in a single batch.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].disclosable`default `true` A claim that is candidate for Selective Disclosure.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].display`Control display settings for this claim.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].display[].locale`default `en-US` Locale that controls this configuration's display.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].display[].name`Display name for this configuration.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].mandatory`Whether this claim is mandatory.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].namespace`Name space of this claim in an ISO/IEC 18013-5 mdoc, such as org.iso.18013.5.1.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].credential-validity`default `P30D` Length of time for which a credential issued by this configuration remains valid.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].deferred-issuance`Whether credentials of this configuration are issued in a deferred manner.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display`default `org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties$CredentialConfigurationDisplay@4d847d32` Control display settings of this credential configuration used in metadata generation.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[].background-color`Background color for this configuration.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[].description`default `My verifiable credential that belongs to my organization` Description for this configuration.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[].locale`default `en-US` Locale that controls this configuration's display.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[].logo`default `https://apereo.github.io/cas/images/cas_logo.png` Logo url.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[].name`default `My Verifiable Credential` Display name for this configuration.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[].text-color`Text color for this configuration.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].doctype`Document type of an ISO/IEC 18013-5 mdoc, required by the mso_mdoc format and ignored by the others, for example org.iso.18013.5.1.mDL for a mobile driving licence.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].key-attestations.key-storage`Attack potential resistance levels of the key storage this credential configuration accepts, such as iso_18045_high or iso_18045_moderate.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].key-attestations.required`Whether every proof for this credential configuration must come with a key attestation, either in the key_attestation header of a jwt proof or as an attestation proof.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].key-attestations.user-authentication`Attack potential resistance levels of user authentication this credential configuration accepts, such as iso_18045_high.
- `cas.authn.oidc.vc.issuer.deferred-issuance.interval`default `PT5M` How long a wallet should wait before asking for the credentials of a deferred transaction again, advertised as the interval of the credential response.
- `cas.authn.oidc.vc.issuer.deferred-issuance.time-to-live`default `P7D` How long a deferred transaction is kept.
- `cas.authn.oidc.vc.issuer.display`How wallets present this credential issuer, one entry per language.
- `cas.authn.oidc.vc.issuer.display[].locale`Language of this display, as a BCP 47 language tag such as en-US.
- `cas.authn.oidc.vc.issuer.display[].logo`URL of the credential issuer's logo.
- `cas.authn.oidc.vc.issuer.display[].logo-alt-text`Alternative text for the logo; defaults to the display name.
- `cas.authn.oidc.vc.issuer.display[].name`Display name of the credential issuer.
- `cas.authn.oidc.vc.issuer.encryption.alg-values-supported`JWE key management algorithms (alg) the issuer accepts in the key a wallet supplies for response encryption.
- `cas.authn.oidc.vc.issuer.encryption.enabled`default `false` Whether the issuer metadata advertises credential_request_encryption and credential_response_encryption, and the credential endpoint accepts encrypted requests and encrypts responses when asked to.
- `cas.authn.oidc.vc.issuer.encryption.enc-values-supported`JWE content encryption algorithms (enc) accepted for encrypted requests and offered for encrypted responses.
- `cas.authn.oidc.vc.issuer.encryption.request-encryption-required`default `false` Whether every credential request must be encrypted.
- `cas.authn.oidc.vc.issuer.encryption.response-encryption-required`default `false` Whether every credential response must be encrypted, so that a wallet has to supply credential_response_encryption with each request.
- `cas.authn.oidc.vc.issuer.key-attestation.trust-anchors`Locations of PEM-encoded certificates of the trust anchors that key attestations must chain to, such as those of wallet providers.
- `cas.authn.oidc.vc.issuer.status-list.enabled`default `false` Whether issued SD-JWT VC credentials carry a status claim that points into a status list published by CAS.
- `cas.authn.oidc.vc.issuer.status-list.expiration`default `PT24H` How long a status list token is valid after it is issued, advertised as its exp claim.
- `cas.authn.oidc.vc.issuer.status-list.size`default `131072` Number of entries in each status list.
- `cas.authn.oidc.vc.issuer.status-list.time-to-live`default `PT10M` How long a verifier may cache a status list token before fetching it again, advertised as its ttl claim.
- `cas.authn.oidc.vc.metadata.include-claims`default `true` Whether claims should be included in the metadata.
- `cas.authn.oidc.vc.metadata.signed-metadata-expiration`default `P1D` How long signed credential issuer metadata, returned to wallets that ask for application/jwt, remains valid, advertised as its exp claim.
- `cas.authn.oidc.vc.offer.required-principal-attribute`The principal attribute that must be present in the authenticated principal for the offer to be valid.
- `cas.authn.oidc.vc.offer.required-principal-attribute-value`The value of the principal attribute that must be present in the authenticated principal for the offer to be valid.
- `cas.authn.oidc.vc.offer.transaction-code-enabled`default `true` Whether a transaction code should be attached to credential offers that use the pre-authorized code flow.
- `cas.authn.oidc.vc.presentation.client-identifier-prefix`default `REDIRECT_URI` How the wallet is expected to identify and authenticate CAS as the verifier, expressed as an OpenID4VP client identifier prefix.
- `cas.authn.oidc.vc.presentation.response-mode`default `DIRECT_POST` How the wallet delivers its authorization response, expressed as an OpenID4VP response mode.

### cas.authn.saml-idp.metadata

- `cas.authn.saml-idp.metadata.core.failure-cache-expiration`default `PT1M` How long a failure to load metadata for a service provider is remembered before the metadata source is consulted again.
- `cas.authn.saml-idp.metadata.core.maximum-cache-duration`default `P1D` The longest time service provider metadata is cached when the metadata itself asks for a longer time with its cacheDuration.
- `cas.authn.saml-idp.metadata.core.minimum-cache-duration`default `PT1M` The shortest time service provider metadata is cached when the metadata itself asks for a shorter time with its cacheDuration, so that metadata asking to be cached for no time at all is not loaded again on every request.
- `cas.authn.saml-idp.metadata.file-system.change-detection-interval`default `PT5S` How often CAS may check whether service provider metadata kept in local files has changed.
- `cas.authn.saml-idp.metadata.http.metadata-backup-max-age`default `P7D` How long a metadata backup file may go without being confirmed by its source before it is reported as stale.

### cas.authn.throttle

- `cas.authn.throttle.hazelcast.cluster.core.cluster-name`default `cas-hazelcast-cluster` Name of the Hazelcast cluster.

### cas.heimdall

- `cas.heimdall.jwt-assertion-max-lifetime`default `PT5M` Maximum lifetime of a JWT bearer assertion presented to Heimdall, measured between its iat and exp claims.

### cas.mcp

- `cas.mcp.oauth.operator-required-attributes`Principal attributes that the person behind an access token must have to be granted the MCP_OPERATOR role, which the tools that change CAS require, keyed by attribute name, with a regular expression as the value.
- `cas.mcp.oauth.required-attributes`Principal attributes that the person behind an access token must have to be granted the MCP_ADMIN role, keyed by attribute name, with a regular expression as the value.

### cas.palantir

- `cas.palantir.templates-directory`Directory that holds application definition templates, as JSON files, that are offered when an application definition is written by hand in the dashboard, next to the templates that ship with CAS.

### cas.service-registry

- `cas.service-registry.amazon-s3.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.core.maximum-service-id-length`default `4096` Maximum length of a service identifier, such as the service parameter, that CAS will try to match against registered service definitions.
- `cas.service-registry.dynamo-db.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.git.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.jpa.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.json.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.ldap.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.mongo.change-stream.enabled`default `true` Watch the service registry collection through a MongoDb change stream and reload service definitions as soon as they change, on every CAS node.
- `cas.service-registry.mongo.change-stream.quiet-period`default `PT2S` How long to wait after the last observed change before reloading service definitions, so that a burst of changes, such as a bulk import, results in a single reload.
- `cas.service-registry.mongo.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.redis.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.rest.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.
- `cas.service-registry.stream.hazelcast.config.cluster.core.cluster-name`default `cas-hazelcast-cluster` Name of the Hazelcast cluster.
- `cas.service-registry.yaml.order`The execution order of this registry which will determine its position in a chain in case multiple registries are defined.

### cas.tgc

- `cas.tgc.crypto.encryption-enabled`default `true` Whether crypto encryption operations are enabled.
- `cas.tgc.crypto.signing-enabled`default `true` Whether crypto signing operations are enabled.

### cas.ticket

- `cas.ticket.registry.hazelcast.cluster.core.cluster-name`default `cas-hazelcast-cluster` Name of the Hazelcast cluster.
- `cas.ticket.registry.in-memory.properties`Free-form key-value pairs to configure the specific registry with custom settings.

### cas.webflow

- `cas.webflow.session.server.hazelcast.cluster.core.cluster-name`default `cas-hazelcast-cluster` Name of the Hazelcast cluster.

## Removed Settings

These settings are no longer recognized; remove them from your configuration, and check the release notes for what replaces them.

### cas.authn.oauth

- `cas.authn.oauth.csrf-cookie.domain`
- `cas.authn.oauth.csrf-cookie.http-only`
- `cas.authn.oauth.csrf-cookie.max-age`
- `cas.authn.oauth.csrf-cookie.name`
- `cas.authn.oauth.csrf-cookie.path`
- `cas.authn.oauth.csrf-cookie.same-site-policy`
- `cas.authn.oauth.csrf-cookie.secure`

### cas.authn.throttle

- `cas.authn.throttle.hazelcast.cluster.core.async-fillup`
- `cas.authn.throttle.hazelcast.cluster.core.replicated`
- `cas.authn.throttle.hazelcast.core.enable-jet`

### cas.service-registry

- `cas.service-registry.cosmos-db.consistency-level`
- `cas.service-registry.cosmos-db.container`
- `cas.service-registry.cosmos-db.create-container`
- `cas.service-registry.cosmos-db.database`
- `cas.service-registry.cosmos-db.database-throughput`
- `cas.service-registry.cosmos-db.endpoint-discovery-enabled`
- `cas.service-registry.cosmos-db.indexing-mode`
- `cas.service-registry.cosmos-db.key`
- `cas.service-registry.cosmos-db.max-retry-attempts-on-throttled-requests`
- `cas.service-registry.cosmos-db.max-retry-wait-time`
- `cas.service-registry.cosmos-db.mode`
- `cas.service-registry.cosmos-db.preferred-regions`
- `cas.service-registry.cosmos-db.uri`
- `cas.service-registry.cosmos-db.user-agent-suffix`
- `cas.service-registry.stream.hazelcast.config.cluster.core.async-fillup`
- `cas.service-registry.stream.hazelcast.config.cluster.core.replicated`
- `cas.service-registry.stream.hazelcast.config.core.enable-jet`

### cas.ticket

- `cas.ticket.registry.cosmos-db.consistency-level`
- `cas.ticket.registry.cosmos-db.crypto.alg`
- `cas.ticket.registry.cosmos-db.crypto.enabled`
- `cas.ticket.registry.cosmos-db.crypto.encryption.key`
- `cas.ticket.registry.cosmos-db.crypto.encryption.key-size`
- `cas.ticket.registry.cosmos-db.crypto.signing-enabled`
- `cas.ticket.registry.cosmos-db.crypto.signing.key`
- `cas.ticket.registry.cosmos-db.crypto.signing.key-size`
- `cas.ticket.registry.cosmos-db.database`
- `cas.ticket.registry.cosmos-db.database-throughput`
- `cas.ticket.registry.cosmos-db.endpoint-discovery-enabled`
- `cas.ticket.registry.cosmos-db.indexing-mode`
- `cas.ticket.registry.cosmos-db.key`
- `cas.ticket.registry.cosmos-db.max-retry-attempts-on-throttled-requests`
- `cas.ticket.registry.cosmos-db.max-retry-wait-time`
- `cas.ticket.registry.cosmos-db.mode`
- `cas.ticket.registry.cosmos-db.preferred-regions`
- `cas.ticket.registry.cosmos-db.uri`
- `cas.ticket.registry.cosmos-db.user-agent-suffix`
- `cas.ticket.registry.hazelcast.cluster.core.async-fillup`
- `cas.ticket.registry.hazelcast.cluster.core.replicated`
- `cas.ticket.registry.hazelcast.core.enable-jet`
- `cas.ticket.registry.stateless.storage-type`

### cas.webflow

- `cas.webflow.session.server.hazelcast.cluster.core.async-fillup`
- `cas.webflow.session.server.hazelcast.cluster.core.replicated`
- `cas.webflow.session.server.hazelcast.core.enable-jet`

## Replaced Settings

These settings are deprecated in favor of another one; move to the replacement before the old name is removed.

No settings were replaced.

## Changed Defaults

The default value of these settings changed. If you relied on the previous default, set it explicitly.

| Setting | Before | Now |
| --- | --- | --- |
| `cas.acceptable-usage-policy.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.acceptable-usage-policy.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.acceptable-usage-policy.mongo.pool.max-size` | `10` | `100` |
| `cas.acceptable-usage-policy.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.audit.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.audit.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.audit.mongo.pool.max-size` | `10` | `100` |
| `cas.audit.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.mfa.gauth.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.mfa.gauth.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.mfa.gauth.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.mfa.gauth.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.mfa.trusted.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.mfa.trusted.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.mfa.trusted.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.mfa.trusted.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.mfa.web-authn.core.trust-source.fido.metadata-blob-url` | `https://mds.fidoalliance.org/` | `https://mds3.fidoalliance.org/` |
| `cas.authn.mfa.web-authn.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.mfa.web-authn.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.mfa.web-authn.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.mfa.web-authn.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.mfa.yubikey.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.mfa.yubikey.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.mfa.yubikey.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.mfa.yubikey.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.oidc.jwks.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.oidc.jwks.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.oidc.jwks.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.oidc.jwks.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.pac4j.saml[].metadata.service-provider.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.pac4j.saml[].metadata.service-provider.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.pac4j.saml[].metadata.service-provider.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.pac4j.saml[].metadata.service-provider.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.passwordless.accounts.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.passwordless.accounts.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.passwordless.accounts.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.passwordless.accounts.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.passwordless.tokens.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.passwordless.tokens.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.passwordless.tokens.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.passwordless.tokens.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.saml-idp.metadata.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.authn.saml-idp.metadata.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.authn.saml-idp.metadata.mongo.pool.max-size` | `10` | `100` |
| `cas.authn.saml-idp.metadata.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.authn.throttle.hazelcast.cluster.core.eviction-policy` | `LRU` | `NONE` |
| `cas.authn.throttle.hazelcast.cluster.core.map-merge-policy` | `PUT_IF_ABSENT` | `LATEST_UPDATE` |
| `cas.consent.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.consent.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.consent.mongo.pool.max-size` | `10` | `100` |
| `cas.consent.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.events.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.events.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.events.mongo.pool.max-size` | `10` | `100` |
| `cas.events.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.javers.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.javers.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.javers.mongo.pool.max-size` | `10` | `100` |
| `cas.javers.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.monitor.mongo[].pool.idle-time` | `PT30S` | `PT5M` |
| `cas.monitor.mongo[].pool.life-time` | `PT60S` | `PT30M` |
| `cas.monitor.mongo[].pool.max-size` | `10` | `100` |
| `cas.monitor.mongo[].pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.service-registry.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.service-registry.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.service-registry.mongo.pool.max-size` | `10` | `100` |
| `cas.service-registry.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.service-registry.stream.hazelcast.config.cluster.core.eviction-policy` | `LRU` | `NONE` |
| `cas.service-registry.stream.hazelcast.config.cluster.core.map-merge-policy` | `PUT_IF_ABSENT` | `LATEST_UPDATE` |
| `cas.ticket.registry.hazelcast.cluster.core.eviction-policy` | `LRU` | `NONE` |
| `cas.ticket.registry.hazelcast.cluster.core.map-merge-policy` | `PUT_IF_ABSENT` | `LATEST_UPDATE` |
| `cas.ticket.registry.mongo.pool.idle-time` | `PT30S` | `PT5M` |
| `cas.ticket.registry.mongo.pool.life-time` | `PT60S` | `PT30M` |
| `cas.ticket.registry.mongo.pool.max-size` | `10` | `100` |
| `cas.ticket.registry.mongo.pool.max-wait-time` | `PT60S` | `PT10S` |
| `cas.ticket.tst.time-to-kill-in-seconds` | `PT15M` | `PT5M` |
| `cas.webflow.session.server.hazelcast.cluster.core.eviction-policy` | `LRU` | `NONE` |
| `cas.webflow.session.server.hazelcast.cluster.core.map-merge-policy` | `PUT_IF_ABSENT` | `LATEST_UPDATE` |
