package org.apereo.cas.oidc.federation.registration;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionLimiter;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionUnavailableException;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * Automatic client registration of OpenID Federation 1.0, section 12.1: a relying party uses its Entity Identifier as
 * its client id, and the first request that carries it resolves the relying party's trust chains and saves the
 * resolved metadata as a temporary service, which is resolved again shortly before the trust chain expires and then
 * replaces the earlier registration under the same service id. Only {@code https} Entity Identifiers with a host are
 * considered (section 1.2).
 * <p>
 * Resolution fetches several entity statements and saves a service, so concurrent first requests of one relying party
 * are serialized: the first resolves and saves, the others wait and find the saved service instead of resolving again
 * and saving duplicates under new service ids. The locks are striped by client id, so that client ids an attacker
 * makes up cannot grow a lock per id; the serialization holds within one CAS node only. A request waits a few seconds at
 * most for the lock, after which the registration is busy, as it is when too many resolutions run. Using the trust
 * chain the relying party sent and, failing that, resolving its trust chains is a single resolution, within one
 * deadline and request budget.
 * <p>
 * A relying party that is not registered yet and whose registration is busy, or depends on a trust anchor or document
 * that cannot be reached now, is not registered for now and not remembered as unresolved: {@link #register} then fails
 * with {@link OidcFederationResolutionUnavailableException}, so that it is not answered as an unknown client.
 * <p>
 * An entity identifier whose trust chains could not be resolved is remembered for a while: requests that carry it do
 * not resolve again until then, whether it is unknown or a registration whose refresh failed, which is kept meanwhile.
 * <p>
 * A registration can also be reevaluated ahead of its expiration (section 12.3), when a relying party signs with keys
 * its registration does not hold after rolling its keys over. Each entity is reevaluated at most once in the same
 * period, so requests signed with keys a relying party never published cannot make CAS fetch its statements again and
 * again; a reevaluation that fails keeps the current registration.
 * <p>
 * A trust chain the relying party sends is a candidate only, since anyone can send the genuine statements of an entity:
 * it replaces a registration only when its entity configuration was issued after the one the registration was built
 * from, and when the request that carries it is signed with the keys it resolves to. Otherwise the trust chains are
 * resolved, so that a replayed chain cannot bring back keys the relying party has replaced.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcFederationAutomaticClientRegistration {
    /**
     * Bean name.
     */
    public static final String BEAN_NAME = "oidcFederationAutomaticClientRegistration";

    private static final Duration SERVICE_EXPIRATION_REFRESH_WINDOW = Duration.ofMinutes(5);

    private static final int LOCK_STRIPES = 64;

    private static final String JWKS = "jwks";

    private static final int MAX_UNRESOLVED_ENTITIES = 10_000;

    private final List<CasReentrantLock> serviceLocks = Stream.generate(CasReentrantLock::new).limit(LOCK_STRIPES).toList();

    private final ServicesManager servicesManager;

    private final OidcFederationTrustChainResolver trustChainResolver;

    private final Cache<String, Boolean> unresolvedEntities;

    private final Cache<String, Boolean> reevaluatedEntities;

    private final Supplier<String> pairwiseSubjectSalt;

    private final OidcFederationResolutionLimiter resolutionLimiter;

    public OidcFederationAutomaticClientRegistration(final ServicesManager servicesManager,
                                                     final OidcFederationTrustChainResolver trustChainResolver,
                                                     final Duration unresolvedEntityRetention) {
        this(servicesManager, trustChainResolver, unresolvedEntityRetention, null);
    }

    public OidcFederationAutomaticClientRegistration(final ServicesManager servicesManager,
                                                     final OidcFederationTrustChainResolver trustChainResolver,
                                                     final Duration unresolvedEntityRetention,
                                                     @Nullable final String pairwiseSubjectSalt) {
        this(servicesManager, trustChainResolver, unresolvedEntityRetention, () -> pairwiseSubjectSalt,
            new OidcFederationResolutionLimiter(new OidcFederationProperties()));
    }

    public OidcFederationAutomaticClientRegistration(final ServicesManager servicesManager,
                                                     final OidcFederationTrustChainResolver trustChainResolver,
                                                     final Duration unresolvedEntityRetention,
                                                     final Supplier<String> pairwiseSubjectSalt,
                                                     final OidcFederationResolutionLimiter resolutionLimiter) {
        this.resolutionLimiter = resolutionLimiter;
        this.servicesManager = servicesManager;
        this.trustChainResolver = trustChainResolver;
        this.pairwiseSubjectSalt = pairwiseSubjectSalt;
        this.unresolvedEntities = Caffeine.newBuilder()
            .expireAfterWrite(unresolvedEntityRetention)
            .maximumSize(MAX_UNRESOLVED_ENTITIES)
            .build();
        this.reevaluatedEntities = Caffeine.newBuilder()
            .expireAfterWrite(unresolvedEntityRetention)
            .maximumSize(MAX_UNRESOLVED_ENTITIES)
            .build();
    }

    /**
     * Find the registered service of a client, registering a relying party of the federation on first contact and
     * refreshing a temporary registration that is about to expire. A refresh that fails keeps the current registration.
     *
     * @param clientId the client id
     * @return the registered service, or null when the client is neither registered nor resolvable in the federation
     * @throws OidcFederationResolutionUnavailableException when the client is not registered and cannot be resolved now
     */
    public @Nullable OAuthRegisteredService register(final String clientId) {
        return register(clientId, List.of(), _ -> false);
    }

    /**
     * Find or register the relying party as {@link #register(String)} does, using the trust chain the relying party
     * sent when its trust chains would otherwise be resolved, provided the request is signed with the keys it carries.
     *
     * @param clientId       the client id
     * @param trustChain     the serialized statements of the trust chain the relying party sent, which may be empty
     * @param signedWithKeys whether the request that carries the trust chain is signed with the keys of a registration
     * @return the registered service, or null when the client is neither registered nor resolvable in the federation
     * @throws OidcFederationResolutionUnavailableException when the client is not registered and cannot be resolved now
     */
    public @Nullable OAuthRegisteredService register(final String clientId, final List<String> trustChain,
                                                     final Predicate<OAuthRegisteredService> signedWithKeys) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        if (registeredService != null && !shouldRefresh(registeredService)) {
            return registeredService;
        }
        if ((registeredService == null && !isFederatedEntityId(clientId)) || isUnresolved(clientId)) {
            return registeredService;
        }
        val result = withLock(clientId, () -> {
            val currentService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
            if ((currentService != null && !shouldRefresh(currentService)) || isUnresolved(clientId)) {
                return Optional.ofNullable(currentService);
            }
            try {
                val resolvedService = resolveAndSave(clientId, currentService, trustChain, signedWithKeys);
                if (resolvedService.isEmpty()) {
                    LOGGER.debug("Trust chains of [{}] could not be resolved; they are not resolved again for a while", clientId);
                    unresolvedEntities.put(clientId, Boolean.TRUE);
                }
                return Optional.ofNullable(resolvedService.orElse(currentService));
            } catch (final OidcFederationResolutionUnavailableException e) {
                LOGGER.warn("Trust chains of [{}] are not resolved now: [{}]", clientId, e.getMessage());
                if (currentService == null) {
                    throw e;
                }
                return Optional.of(currentService);
            }
        });
        if (result == null) {
            if (registeredService == null) {
                throw new OidcFederationResolutionUnavailableException("Registration of %s is busy; try again later".formatted(clientId));
            }
            return registeredService;
        }
        return result.orElse(null);
    }

    /**
     * Run the action under the lock of the client id, waiting a few seconds at most for it.
     *
     * @param <T>      the result type
     * @param clientId the client id
     * @param action   the action
     * @return the result, or null when the lock could not be had in time
     */
    private <T> @Nullable T withLock(final String clientId, final Supplier<T> action) {
        val lock = serviceLocks.get(Math.floorMod(clientId.hashCode(), LOCK_STRIPES));
        if (!lock.tryLock()) {
            LOGGER.warn("Registration of [{}] is busy", clientId);
            return null;
        }
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resolve the trust chains of an automatically registered relying party again before its registration expires,
     * when the registration is found stale, as when the relying party signs with a key its registration does not hold.
     * The condition is checked again under the lock, so that concurrent requests reevaluate once and the others find
     * the refreshed registration.
     *
     * @param clientId the client id
     * @param stale    whether the registration no longer holds what the request needs
     * @return the registered service, refreshed when it was reevaluated, or null when the client is not registered
     */
    public @Nullable OAuthRegisteredService reevaluate(final String clientId, final Predicate<OAuthRegisteredService> stale) {
        return reevaluate(clientId, stale, List.of());
    }

    /**
     * Reevaluate the registration as {@link #reevaluate(String, Predicate)} does, using the trust chain the relying
     * party sent when there is one, provided the registration it resolves to is no longer stale, that is, the request
     * is signed with the keys the chain carries. The reevaluation counts against the period before the request is
     * verified, but always ends with keys the relying party has published.
     *
     * @param clientId   the client id
     * @param stale      whether the registration no longer holds what the request needs
     * @param trustChain the serialized statements of the trust chain the relying party sent, which may be empty
     * @return the registered service, refreshed when it was reevaluated, or null when the client is not registered
     */
    public @Nullable OAuthRegisteredService reevaluate(final String clientId, final Predicate<OAuthRegisteredService> stale,
                                                       final List<String> trustChain) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        if (!isReevaluationDue(registeredService, stale)) {
            return registeredService;
        }
        val result = withLock(clientId, () -> {
            val currentService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
            if (!isReevaluationDue(currentService, stale)) {
                return currentService;
            }
            reevaluatedEntities.put(clientId, Boolean.TRUE);
            LOGGER.info("Registration of [{}] does not hold the keys it signs with; resolving its trust chains again", clientId);
            try {
                return resolveAndSave(clientId, currentService, trustChain, stale.negate()).orElse(currentService);
            } catch (final OidcFederationResolutionUnavailableException e) {
                LOGGER.warn("Trust chains of [{}] are not resolved again now: [{}]", clientId, e.getMessage());
                reevaluatedEntities.invalidate(clientId);
                return currentService;
            }
        });
        return result == null ? registeredService : result;
    }

    /**
     * Whether the client id can be the Entity Identifier of a relying party: an absolute {@code https} URL with a host.
     *
     * @param clientId the client id
     * @return true or false
     */
    public static boolean isFederatedEntityId(@Nullable final String clientId) {
        if (StringUtils.isBlank(clientId) || !Strings.CI.startsWith(clientId, "https://")) {
            return false;
        }
        try {
            return StringUtils.isNotBlank(new URI(clientId).getHost());
        } catch (final URISyntaxException e) {
            LOGGER.trace("Client id [{}] is not a valid URI", clientId);
            return false;
        }
    }

    /**
     * Whether the service was registered automatically from the federation.
     *
     * @param registeredService the registered service
     * @return true or false
     */
    public static boolean isAutomaticallyRegistered(@Nullable final RegisteredService registeredService) {
        return registeredService != null
            && registeredService.getProperties().containsKey(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE);
    }

    private boolean isReevaluationDue(@Nullable final OAuthRegisteredService registeredService,
                                      final Predicate<OAuthRegisteredService> stale) {
        return registeredService != null
            && isAutomaticallyRegistered(registeredService)
            && reevaluatedEntities.getIfPresent(registeredService.getClientId()) == null
            && stale.test(registeredService);
    }

    private boolean isUnresolved(final String clientId) {
        return unresolvedEntities.getIfPresent(clientId) != null;
    }

    /**
     * Resolve the relying party, from the trust chain it sent when that chain can be used, and save it under the id of
     * its current registration. Both are a single resolution, within one deadline and request budget.
     *
     * @param clientId       the client id
     * @param currentService the current registration
     * @param trustChain     the serialized statements of the trust chain the relying party sent, which may be empty
     * @param signedWithKeys whether the request that carries the trust chain is signed with the keys of a registration
     * @return the saved registration
     * @throws OidcFederationResolutionUnavailableException when too many resolutions are running, or a trust anchor cannot
     *                                                      be reached now, which is not a failure to resolve the relying party
     */
    private Optional<OAuthRegisteredService> resolveAndSave(final String clientId, @Nullable final OAuthRegisteredService currentService,
                                                            final List<String> trustChain,
                                                            final Predicate<OAuthRegisteredService> signedWithKeys) {
        try {
            val resolvedService = resolutionLimiter.execute(() -> {
                val sentService = resolveSentTrustChain(clientId, currentService, trustChain, signedWithKeys);
                return sentService.isPresent() ? sentService : trustChainResolver.resolveTrustChains(clientId);
            });
            LOGGER.debug("Resolved service: [{}]", resolvedService);
            return resolvedService
                .filter(service -> applyPairwiseSubjects(clientId, service))
                .map(service -> {
                    if (currentService != null) {
                        service.setId(currentService.getId());
                    }
                    return (OAuthRegisteredService) servicesManager.save(service);
                });
        } catch (final OidcFederationResolutionUnavailableException e) {
            throw e;
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, "Unable to resolve federated service [" + clientId + "]", e);
            return Optional.empty();
        }
    }

    /**
     * A relying party that asks for pairwise subject identifiers is given them, calculated with the pairwise subject salt
     * of CAS when they are used, so that they stay the same whenever the relying party is registered again and the salt
     * is never written into its registration; without that salt, it is not registered rather than given identifiers
     * that other relying parties could link.
     *
     * @param clientId the client id
     * @param service  the registration
     * @return whether the relying party can be registered
     */
    private boolean applyPairwiseSubjects(final String clientId, final OAuthRegisteredService service) {
        if (!(service instanceof final OidcRegisteredService oidcService)
            || !Strings.CI.equals(OidcSubjectTypes.PAIRWISE.getType(), oidcService.getSubjectType())) {
            return true;
        }
        if (StringUtils.isBlank(pairwiseSubjectSalt.get())) {
            LOGGER.warn("[{}] asks for pairwise subject identifiers, but no pairwise subject salt is defined; it is not registered", clientId);
            return false;
        }
        oidcService.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());
        return true;
    }

    /**
     * The registration built from the trust chain the relying party sent, when the chain verifies, its entity
     * configuration was not issued before the one the current registration was built from, and the request that carries
     * it is signed with the keys it resolves to. Statements of an entity are public, so a chain alone proves nothing about
     * who sends it: an older chain must not bring back keys the relying party has replaced, and any chain, the very chain
     * of the current registration included, is only used with a request signed with its keys, which is what stops a
     * replay. A chain as new as the registration, signed with its keys, is the relying party's own.
     *
     * @param clientId       the client id
     * @param currentService the current registration
     * @param trustChain     the serialized statements of the trust chain the relying party sent, which may be empty
     * @param signedWithKeys whether the request that carries the trust chain is signed with the keys of a registration
     * @return the registration from the sent trust chain, or empty when the trust chains must be resolved instead
     * @throws Exception the exception
     */
    private Optional<OidcRegisteredService> resolveSentTrustChain(final String clientId, @Nullable final OAuthRegisteredService currentService,
                                                                  final List<String> trustChain,
                                                                  final Predicate<OAuthRegisteredService> signedWithKeys)
            throws Exception {
        if (trustChain.isEmpty()) {
            return Optional.empty();
        }
        if (!isSignedWithDeclaredKeys(clientId, trustChain, signedWithKeys)) {
            LOGGER.warn("Request of [{}] is not signed with the keys its sent trust chain declares, which is not used", clientId);
            return Optional.empty();
        }
        val sentService = verifySentTrustChain(clientId, trustChain);
        if (sentService.isEmpty()) {
            LOGGER.debug("Trust chain sent by [{}] cannot be used; its trust chains are resolved instead", clientId);
            return Optional.empty();
        }
        if (!isNewerThan(sentService.get(), currentService)) {
            LOGGER.warn("Trust chain sent by [{}] is not newer than its registration and is not used", clientId);
            return Optional.empty();
        }
        if (!signedWithKeys.test(sentService.get())) {
            LOGGER.warn("Request of [{}] is not signed with the keys of the trust chain it sent, which is not used", clientId);
            return Optional.empty();
        }
        return sentService;
    }

    private Optional<OidcRegisteredService> verifySentTrustChain(final String clientId, final List<String> trustChain) throws Exception {
        try {
            return trustChainResolver.resolveSentTrustChain(clientId, trustChain);
        } catch (final OidcFederationResolutionUnavailableException e) {
            LOGGER.warn("Trust chain sent by [{}] cannot be verified now; its trust chains are resolved instead: [{}]", clientId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Whether the request is signed with the keys that the sent trust chain declares inline for the relying party, those
     * of the metadata its immediate superior asserts or else those of its entity configuration, checked before the chain
     * is verified, which fetches statements, historical keys, trust marks and documents: a chain anyone could replay is
     * not worth that unless the request is signed with its keys. Metadata policies of the chain may still change those
     * keys, which the resolved registration is checked against afterwards; a relying party that publishes its keys at a
     * URL only, or a chain that cannot be read, which the chain verification refuses, is not checked here.
     *
     * @param clientId       the client id
     * @param trustChain     the serialized statements of the trust chain the relying party sent
     * @param signedWithKeys whether the request that carries the trust chain is signed with the keys of a registration
     * @return true or false
     */
    private static boolean isSignedWithDeclaredKeys(final String clientId, final List<String> trustChain,
                                                    final Predicate<OAuthRegisteredService> signedWithKeys) {
        try {
            val chain = TrustChain.parseSerialized(trustChain);
            val declaredKeys = Stream.concat(chain.getSuperiorStatements().stream().limit(1), Stream.of(chain.getLeafConfiguration()))
                .map(statement -> statement.getClaimsSet().getMetadata(EntityType.OPENID_RELYING_PARTY))
                .filter(Objects::nonNull)
                .map(metadata -> FunctionUtils.doUnchecked(() -> JSONObjectUtils.getJSONObject(metadata, JWKS, null)))
                .filter(Objects::nonNull)
                .findFirst();
            if (declaredKeys.isEmpty()) {
                return true;
            }
            val declared = new OidcRegisteredService();
            declared.setClientId(clientId);
            declared.setJwks(declaredKeys.get().toJSONString());
            return signedWithKeys.test(declared);
        } catch (final Exception e) {
            LOGGER.debug("Trust chain sent by [{}] cannot be read: [{}]", clientId, e.getMessage());
            return true;
        }
    }

    private static boolean isNewerThan(final OAuthRegisteredService resolvedService, @Nullable final OAuthRegisteredService currentService) {
        if (currentService == null) {
            return true;
        }
        val resolved = readIssuedAt(resolvedService);
        val current = readIssuedAt(currentService);
        return resolved.isEmpty() || current.isEmpty() || resolved.get() >= current.get();
    }

    private static Optional<Long> readIssuedAt(final RegisteredService registeredService) {
        return Optional.ofNullable(registeredService.getProperties().get(OidcFederationDefaultTrustChainResolver.OPENIDFEDERATION_ENTITY_CONFIGURATION_ISSUED_AT))
            .map(property -> property.getValue(String.class))
            .filter(StringUtils::isNumeric)
            .map(Long::valueOf);
    }

    private static boolean shouldRefresh(final OAuthRegisteredService registeredService) {
        if (!isAutomaticallyRegistered(registeredService)) {
            return false;
        }
        val expirationDate = registeredService.getExpirationPolicy().getExpirationDate();
        val expiration = DateTimeUtils.zonedDateTimeOf(expirationDate);
        val refresh = expiration.minus(SERVICE_EXPIRATION_REFRESH_WINDOW).isBefore(ZonedDateTime.now(Clock.systemUTC()));
        LOGGER.debug("Should refresh: [{}] upfront: [{}]", registeredService, refresh);
        return refresh;
    }
}
