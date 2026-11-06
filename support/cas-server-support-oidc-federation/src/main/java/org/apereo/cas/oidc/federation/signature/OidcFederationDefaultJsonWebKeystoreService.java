package org.apereo.cas.oidc.federation.signature;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.support.CronExpression;

/**
 * The Federation Entity Keys of CAS, kept in a JSON web keystore file that is generated when missing, with a current
 * and a future key. Keys carry the lifecycle {@code state} of the OpenID Connect keystore; a key without one is current,
 * so a keystore that predates rotation keeps working. Entity statements are signed with the current key, and the entity
 * configuration publishes the current, future and previous keys, so that a key is known to the federation before it
 * signs and after it stopped signing (OpenID Federation 1.0, section 11.2). Keys that are retired are kept in the same
 * file, public only, with {@code exp} set to the time they were retired, and are published by the historical keys
 * endpoint (section 8.7). New keys record {@code iat}, and keys record when they became previous. The keystore file is
 * read again when it changes, so that nodes sharing it sign with the same keys; a scheduled rotation or revocation runs
 * only when it is due by the age of the keys, so that it does not run again on a restart or on another node.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class OidcFederationDefaultJsonWebKeystoreService implements OidcFederationJsonWebKeystoreService {
    private static final int MAX_SYMBOLIC_LINKS = 10;

    private static final String ISSUED_AT = "iat";

    private static final String EXPIRATION = "exp";

    private static final String REVOKED = "revoked";

    private static final String REVOKED_AT = "revoked_at";

    private static final String REASON = "reason";

    private static final String PREVIOUS_SINCE = "previous_since";

    private final CasReentrantLock lock = new CasReentrantLock();

    private final OidcProperties oidcProperties;

    private final @Nullable Resource resource;

    private volatile List<JsonWebKey> keys;

    private volatile long keysLastModified;

    public OidcFederationDefaultJsonWebKeystoreService(final OidcProperties oidcProperties) throws Exception {
        this.oidcProperties = oidcProperties;
        val jwksFile = SpringExpressionLanguageValueResolver.getInstance().resolve(oidcProperties.getFederation().getJwksFile());
        this.resource = ResourceUtils.getRawResourceFrom(jwksFile);
        this.keys = loadOrGenerate();
        this.keysLastModified = getKeystoreLastModified();
    }

    /**
     * How old keys must be for a scheduled rotation or revocation to change them: half the time between two runs of the
     * schedule, so that a run soon after another one, on a restart or on another node sharing the keystore, leaves the
     * keys as they are, while a schedule that restarts more often than it runs still rotates.
     *
     * @param schedule the schedule
     * @return the age
     */
    public static Duration getScheduledKeyAge(final SchedulingProperties schedule) {
        if (StringUtils.isNotBlank(schedule.getCronExpression())) {
            val cron = CronExpression.parse(schedule.getCronExpression());
            val next = cron.next(ZonedDateTime.now(ZoneOffset.UTC));
            val following = next == null ? null : cron.next(next);
            return next == null || following == null ? Duration.ZERO : Duration.between(next, following).dividedBy(2);
        }
        return Beans.newDuration(schedule.getRepeatInterval()).dividedBy(2);
    }

    /**
     * The keys of the keystore file, or a current and a future key, which are then written to it, when the file is
     * missing, empty or holds no keys. A keystore that exists but cannot be read, for want of permissions or because of an
     * error of the file system, or cannot be parsed, is refused rather than replaced, and its keys are not made up in
     * memory, which would leave every node and restart signing with keys of its own.
     *
     * @return the keys
     * @throws Exception when the keystore cannot be read, or the generated keys cannot be written
     */
    private List<JsonWebKey> loadOrGenerate() throws Exception {
        if (hasKeystoreContent()) {
            LOGGER.debug("Loading OpenID Connect Federation JSON web keys from [{}]", resource);
            val loaded = readKeystore();
            if (!loaded.isEmpty()) {
                return new ArrayList<>(loaded);
            }
        }
        LOGGER.debug("JSON web keys for OpenID Connect Federation are not found. Generating a current and a future key");
        val generated = new ArrayList<JsonWebKey>();
        generated.add(generateKey(JsonWebKeyLifecycleStates.CURRENT));
        generated.add(generateKey(JsonWebKeyLifecycleStates.FUTURE));
        store(generated);
        return generated;
    }

    private boolean hasKeystoreContent() throws IOException {
        if (resource == null || !resource.exists()) {
            return false;
        }
        return !resource.isFile() || resource.getFile().length() > 0;
    }

    private List<JsonWebKey> readKeystore() {
        try (val input = Objects.requireNonNull(resource).getInputStream()) {
            return new JsonWebKeySet(IOUtils.toString(input, StandardCharsets.UTF_8)).getJsonWebKeys();
        } catch (final Exception e) {
            throw new IllegalStateException("OpenID Connect Federation keystore %s cannot be read: %s".formatted(resource, e.getMessage()), e);
        }
    }

    /**
     * The published keys carry the algorithm they sign with, which verifiers match against the {@code alg} of a
     * statement, whatever {@code alg} the keystore records for them.
     *
     * @return the key set
     * @throws Exception the exception
     */
    @Override
    public JWKSet toJWKSet() throws Exception {
        val published = new ArrayList<JWK>();
        for (val key : getPublishedKeys()) {
            val jwk = toJwk(key);
            val parameters = jwk.toPublicJWK().toJSONObject();
            parameters.put(JsonWebKey.ALGORITHM_PARAMETER, resolveSigningAlgorithm(jwk).getName());
            published.add(JWK.parse(parameters));
        }
        return new JWKSet(published);
    }

    @Override
    public EntityStatement signEntityStatement(final EntityStatementClaimsSet claims) throws Exception {
        val signingKey = toJwk(getCurrentKey());
        return EntityStatement.sign(claims, signingKey, resolveSigningAlgorithm(signingKey));
    }

    @Override
    public SignedJWT sign(final JWTClaimsSet claims, final JOSEObjectType type) throws Exception {
        val signingKey = toJwk(getCurrentKey());
        val algorithm = resolveSigningAlgorithm(signingKey);
        val jwt = new SignedJWT(new JWSHeader.Builder(algorithm).type(type).keyID(signingKey.getKeyID()).build(), claims);
        jwt.sign(new DefaultJWSSignerFactory().createJWSSigner(signingKey, algorithm));
        return jwt;
    }

    /**
     * The algorithm a key signs with, from its type and curve: the {@code alg} a key may carry is not trusted, since
     * keys generated for elliptic curves have carried one that does not match their curve.
     *
     * @param key the key
     * @return the algorithm
     */
    static JWSAlgorithm resolveSigningAlgorithm(final JWK key) {
        return switch (key) {
            case final ECKey ecKey when Curve.P_384.equals(ecKey.getCurve()) -> JWSAlgorithm.ES384;
            case final ECKey ecKey when Curve.P_521.equals(ecKey.getCurve()) -> JWSAlgorithm.ES512;
            case ECKey _ -> JWSAlgorithm.ES256;
            case OctetKeyPair _ -> JWSAlgorithm.EdDSA;
            default -> JWSAlgorithm.RS256;
        };
    }

    @Override
    public List<JWK> getHistoricalKeys() {
        return getKeys()
            .stream()
            .filter(OidcFederationDefaultJsonWebKeystoreService::isRetired)
            .map(OidcFederationDefaultJsonWebKeystoreService::toPublicJwk)
            .toList();
    }

    @Override
    public JWKSet rotate() throws Exception {
        return lock.executeAndThrow(() -> {
            reloadKeystoreIfModified();
            val now = Instant.now(Clock.systemUTC()).getEpochSecond();
            val updated = copyOf(keys);
            val futureKeys = getActiveKeys(updated, JsonWebKeyLifecycleStates.FUTURE);
            if (futureKeys.isEmpty()) {
                LOGGER.info("No future OpenID Connect Federation key is published yet; adding one, which the next rotation makes current");
            } else {
                getActiveKeys(updated, JsonWebKeyLifecycleStates.CURRENT).forEach(key -> {
                    JsonWebKeyLifecycleStates.setJsonWebKeyState(key, JsonWebKeyLifecycleStates.PREVIOUS);
                    key.setOtherParameter(PREVIOUS_SINCE, now);
                });
                futureKeys.forEach(key -> JsonWebKeyLifecycleStates.setJsonWebKeyState(key, JsonWebKeyLifecycleStates.CURRENT));
            }
            updated.add(generateKey(JsonWebKeyLifecycleStates.FUTURE));
            store(updated);
            keys = updated;
            return toJWKSet();
        });
    }

    @Override
    public JWKSet rotateWhenDue(final Duration published) throws Exception {
        return lock.executeAndThrow(() -> {
            reloadKeystoreIfModified();
            val since = Instant.now(Clock.systemUTC()).minus(published).getEpochSecond();
            val notDue = getActiveKeys(keys, JsonWebKeyLifecycleStates.FUTURE)
                .stream()
                .filter(key -> readEpochSecond(key, ISSUED_AT).orElse(Long.MIN_VALUE) > since)
                .map(JsonWebKey::getKeyId)
                .toList();
            if (!notDue.isEmpty()) {
                LOGGER.debug("OpenID Connect Federation keys are not rotated: future keys [{}] are published for less than [{}]", notDue, published);
                return toJWKSet();
            }
            return rotate();
        });
    }

    @Override
    public JWKSet revoke() throws Exception {
        return retirePreviousKeys(null);
    }

    @Override
    public JWKSet revokeWhenDue(final Duration previous) throws Exception {
        return retirePreviousKeys(previous);
    }

    /**
     * Retire the previous keys, all of them or those previous for at least the given time. A previous key that does not
     * record since when it is previous, rotated before keys recorded it, records now.
     *
     * @param previous how long keys must have been previous, or null to retire all of them
     * @return the keys published after the revocation
     * @throws Exception the exception
     */
    private JWKSet retirePreviousKeys(@Nullable final Duration previous) throws Exception {
        return lock.executeAndThrow(() -> {
            reloadKeystoreIfModified();
            val now = Instant.now(Clock.systemUTC()).getEpochSecond();
            val updated = new ArrayList<JsonWebKey>();
            var modified = false;
            for (val key : copyOf(keys)) {
                val previousSince = readEpochSecond(key, PREVIOUS_SINCE);
                if (isRetired(key) || !JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isPrevious()) {
                    updated.add(key);
                } else if (previous != null && previousSince.isEmpty()) {
                    key.setOtherParameter(PREVIOUS_SINCE, now);
                    updated.add(key);
                    modified = true;
                } else if (previous == null || previousSince.orElseThrow() + previous.toSeconds() <= now) {
                    val retired = JsonWebKey.Factory.newJwk(key.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
                    retired.setOtherParameter(EXPIRATION, now);
                    updated.add(retired);
                    modified = true;
                    LOGGER.info("Retired OpenID Connect Federation key [{}]", key.getKeyId());
                } else {
                    updated.add(key);
                }
            }
            if (modified) {
                store(updated);
                keys = updated;
            }
            return toJWKSet();
        });
    }

    @Override
    public Optional<JWKSet> revokeKey(final String keyId, @Nullable final String reason) throws Exception {
        return lock.executeAndThrow(() -> {
            reloadKeystoreIfModified();
            if (keys.stream().noneMatch(key -> keyId.equals(key.getKeyId()))) {
                return Optional.<JWKSet>empty();
            }
            val revokedAt = Instant.now(Clock.systemUTC()).getEpochSecond();
            val revocation = new LinkedHashMap<String, Object>();
            revocation.put(REVOKED_AT, revokedAt);
            if (StringUtils.isNotBlank(reason)) {
                revocation.put(REASON, reason);
            }
            val updated = new ArrayList<JsonWebKey>();
            for (val key : copyOf(keys)) {
                if (keyId.equals(key.getKeyId())) {
                    val revoked = isRetired(key) ? key : JsonWebKey.Factory.newJwk(key.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
                    if (!isRetired(key)) {
                        revoked.setOtherParameter(EXPIRATION, revokedAt);
                    }
                    revoked.setOtherParameter(REVOKED, revocation);
                    updated.add(revoked);
                } else {
                    updated.add(key);
                }
            }
            if (getActiveKeys(updated, JsonWebKeyLifecycleStates.CURRENT).isEmpty()) {
                val futureKeys = getActiveKeys(updated, JsonWebKeyLifecycleStates.FUTURE);
                if (futureKeys.isEmpty()) {
                    updated.add(generateKey(JsonWebKeyLifecycleStates.CURRENT));
                } else {
                    JsonWebKeyLifecycleStates.setJsonWebKeyState(futureKeys.getFirst(), JsonWebKeyLifecycleStates.CURRENT);
                }
            }
            if (getActiveKeys(updated, JsonWebKeyLifecycleStates.FUTURE).isEmpty()) {
                updated.add(generateKey(JsonWebKeyLifecycleStates.FUTURE));
            }
            store(updated);
            keys = updated;
            LOGGER.warn("Revoked OpenID Connect Federation key [{}] for reason [{}]", keyId, StringUtils.defaultIfBlank(reason, "unspecified"));
            return Optional.of(toJWKSet());
        });
    }

    private List<JsonWebKey> getPublishedKeys() {
        return getKeys()
            .stream()
            .filter(key -> !isRetired(key))
            .sorted(Comparator.comparingLong(key -> JsonWebKeyLifecycleStates.getJsonWebKeyState(key).getState()))
            .toList();
    }

    private JsonWebKey getCurrentKey() {
        return getActiveKeys(getKeys(), JsonWebKeyLifecycleStates.CURRENT)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No current OpenID Connect Federation key is defined"));
    }

    private static List<JsonWebKey> getActiveKeys(final List<JsonWebKey> keys, final JsonWebKeyLifecycleStates state) {
        return keys
            .stream()
            .filter(key -> !isRetired(key) && JsonWebKeyLifecycleStates.getJsonWebKeyState(key).getState() == state.getState())
            .toList();
    }

    /**
     * The keys, read again from the keystore file when it changed since they were read, such as by another node. A
     * keystore file that is missing, or is not a file, leaves the keys as they are.
     *
     * @return the keys
     */
    private List<JsonWebKey> getKeys() {
        val lastModified = getKeystoreLastModified();
        if (lastModified != 0L && lastModified != keysLastModified) {
            lock.execute(this::reloadKeystoreIfModified);
        }
        return keys;
    }

    private void reloadKeystoreIfModified() {
        val lastModified = getKeystoreLastModified();
        if (resource != null && lastModified != 0L && lastModified != keysLastModified) {
            try (val input = resource.getInputStream()) {
                val loaded = new JsonWebKeySet(IOUtils.toString(input, StandardCharsets.UTF_8)).getJsonWebKeys();
                if (!loaded.isEmpty()) {
                    keys = new ArrayList<>(loaded);
                    keysLastModified = lastModified;
                    LOGGER.info("Reloaded OpenID Connect Federation JSON web keys from [{}]", resource);
                }
            } catch (final Exception e) {
                LOGGER.warn("OpenID Connect Federation JSON web keys cannot be reloaded from [{}]: [{}]", resource, e.getMessage());
            }
        }
    }

    private long getKeystoreLastModified() {
        try {
            return resource != null && resource.isFile() && resource.exists() ? resource.getFile().lastModified() : 0L;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Copies of the keys, private keys included, which a change works on, so that the keys in use stay as they are
     * until the changed keys are saved, and stay so when they cannot be.
     *
     * @param keys the keys
     * @return the copies
     */
    private static List<JsonWebKey> copyOf(final List<JsonWebKey> keys) {
        return keys
            .stream()
            .map(key -> FunctionUtils.doUnchecked(() -> JsonWebKey.Factory.newJwk(key.toParams(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE))))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Optional<Long> readEpochSecond(final JsonWebKey key, final String name) {
        val value = key.getOtherParameterValue(name, Object.class);
        return value instanceof final Number number ? Optional.of(number.longValue()) : Optional.empty();
    }

    private static boolean isRetired(final JsonWebKey key) {
        return key.getOtherParameterValue(EXPIRATION, Long.class) != null;
    }

    private JsonWebKey generateKey(final JsonWebKeyLifecycleStates state) {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey(
            oidcProperties.getJwks().getCore().getJwksType(),
            oidcProperties.getJwks().getCore().getJwksKeySize(),
            OidcJsonWebKeyUsage.SIGNING);
        JsonWebKeyLifecycleStates.setJsonWebKeyState(key, state);
        key.setOtherParameter(ISSUED_AT, Instant.now(Clock.systemUTC()).getEpochSecond());
        key.setAlgorithm(FunctionUtils.doUnchecked(() -> resolveSigningAlgorithm(toJwk(key))).getName());
        LOGGER.debug("Generated OpenID Connect Federation key [{}] as [{}]", key.getKeyId(), state);
        return key;
    }

    /**
     * Write the keys to the keystore file, through a file of its own in the same directory that then replaces the
     * keystore at once, so that the keystore is never read half written, nor left empty when the keys cannot be written.
     * A keystore that is a symbolic link, even one whose target does not exist yet, is written where the link points. On a
     * file system with POSIX permissions, the new file takes the permissions, owner and group of the keystore, as far as
     * the file system allows it, and is not opened to a group other than the keystore's; a new keystore is readable by its
     * owner only. On other file systems, the new file takes what its directory gives new files. When the file system
     * refuses to replace the keystore, as Windows may while it is open, the keys are not saved and stay as they were.
     *
     * @param updated the keys
     * @throws Exception when the keys cannot be written
     */
    private void store(final List<JsonWebKey> updated) throws Exception {
        if (resource == null) {
            throw new IllegalStateException("No keystore file is defined for OpenID Connect Federation keys");
        }
        val json = new JsonWebKeySet(updated).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        LOGGER.debug("Writing OpenID Connect Federation JSON web keys to [{}]", resource);
        val keystore = resolveLinks(resource.getFile().toPath().toAbsolutePath());
        Files.createDirectories(keystore.getParent());
        val written = Files.createTempFile(keystore.getParent(), keystore.getFileName().toString(), ".tmp");
        try {
            Files.writeString(written, json, StandardCharsets.UTF_8);
            if (Files.exists(keystore)) {
                copyAttributes(keystore, written);
            }
            try {
                Files.move(written, keystore, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(written, keystore, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(written);
        }
        keysLastModified = getKeystoreLastModified();
    }

    private static Path resolveLinks(final Path location) throws IOException {
        var path = location;
        for (var hops = 0; Files.isSymbolicLink(path) && hops < MAX_SYMBOLIC_LINKS; hops++) {
            path = path.resolveSibling(Files.readSymbolicLink(path)).normalize();
        }
        return path;
    }

    private static void copyAttributes(final Path keystore, final Path written) {
        val view = Files.getFileAttributeView(keystore, PosixFileAttributeView.class);
        if (view == null) {
            return;
        }
        try {
            val attributes = view.readAttributes();
            val writtenView = Files.getFileAttributeView(written, PosixFileAttributeView.class);
            val permissions = EnumSet.noneOf(PosixFilePermission.class);
            permissions.addAll(attributes.permissions());
            if (!attributes.owner().equals(writtenView.readAttributes().owner())) {
                keepAttribute(keystore, "owner", () -> writtenView.setOwner(attributes.owner()));
            }
            if (!attributes.group().equals(writtenView.readAttributes().group())
                && !keepAttribute(keystore, "group", () -> writtenView.setGroup(attributes.group()))) {
                permissions.removeAll(EnumSet.of(PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE));
            }
            Files.setPosixFilePermissions(written, permissions);
        } catch (final IOException e) {
            LOGGER.warn("Permissions of OpenID Connect Federation keystore [{}] cannot be kept: [{}]", keystore, e.getMessage());
        }
    }

    private static boolean keepAttribute(final Path keystore, final String attribute, final AttributeChange change) {
        try {
            change.apply();
            return true;
        } catch (final IOException e) {
            LOGGER.warn("The [{}] of OpenID Connect Federation keystore [{}] cannot be kept: [{}]", attribute, keystore, e.getMessage());
            return false;
        }
    }

    @FunctionalInterface
    private interface AttributeChange {
        void apply() throws IOException;
    }

    private static JWK toJwk(final JsonWebKey key) throws Exception {
        return JWK.parse(key.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
    }

    private static JWK toPublicJwk(final JsonWebKey key) {
        try {
            val publicKey = key instanceof final PublicJsonWebKey publicJsonWebKey
                ? JsonWebKey.Factory.newJwk(publicJsonWebKey.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY))
                : key;
            return JWK.parse(publicKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
