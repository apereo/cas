package org.apereo.cas.oidc.federation.registration;

import module java.base;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
 * makes up cannot grow a lock per id; the serialization holds within one CAS node only.
 * <p>
 * An entity identifier whose trust chains could not be resolved is remembered for a while: requests that carry it do
 * not resolve again until then, whether it is unknown or a registration whose refresh failed, which is kept meanwhile.
 * <p>
 * A registration can also be reevaluated ahead of its expiration (section 12.3), when a relying party signs with keys
 * its registration does not hold after rolling its keys over. Each entity is reevaluated at most once in the same
 * period, so requests signed with keys a relying party never published cannot make CAS fetch its statements again and
 * again; a reevaluation that fails keeps the current registration.
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

    private static final int MAX_UNRESOLVED_ENTITIES = 10_000;

    private final List<CasReentrantLock> serviceLocks = Stream.generate(CasReentrantLock::new).limit(LOCK_STRIPES).toList();

    private final ServicesManager servicesManager;

    private final OidcFederationTrustChainResolver trustChainResolver;

    private final Cache<String, Boolean> unresolvedEntities;

    private final Cache<String, Boolean> reevaluatedEntities;

    public OidcFederationAutomaticClientRegistration(final ServicesManager servicesManager,
                                                     final OidcFederationTrustChainResolver trustChainResolver,
                                                     final Duration unresolvedEntityRetention) {
        this.servicesManager = servicesManager;
        this.trustChainResolver = trustChainResolver;
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
     */
    public @Nullable OAuthRegisteredService register(final String clientId) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        if (registeredService != null && !shouldRefresh(registeredService)) {
            return registeredService;
        }
        if ((registeredService == null && !isFederatedEntityId(clientId)) || isUnresolved(clientId)) {
            return registeredService;
        }
        return serviceLocks.get(Math.floorMod(clientId.hashCode(), LOCK_STRIPES)).execute(() -> {
            val currentService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
            if ((currentService != null && !shouldRefresh(currentService)) || isUnresolved(clientId)) {
                return currentService;
            }
            val resolvedService = resolveAndSave(clientId, currentService);
            if (resolvedService.isEmpty()) {
                LOGGER.debug("Trust chains of [{}] could not be resolved; they are not resolved again for a while", clientId);
                unresolvedEntities.put(clientId, Boolean.TRUE);
            }
            return resolvedService.orElse(currentService);
        });
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
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        if (!isReevaluationDue(registeredService, stale)) {
            return registeredService;
        }
        return serviceLocks.get(Math.floorMod(clientId.hashCode(), LOCK_STRIPES)).execute(() -> {
            val currentService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
            if (!isReevaluationDue(currentService, stale)) {
                return currentService;
            }
            reevaluatedEntities.put(clientId, Boolean.TRUE);
            LOGGER.info("Registration of [{}] does not hold the keys it signs with; resolving its trust chains again", clientId);
            return resolveAndSave(clientId, currentService).orElse(currentService);
        });
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

    private Optional<OAuthRegisteredService> resolveAndSave(final String clientId, @Nullable final OAuthRegisteredService currentService) {
        try {
            val resolvedService = trustChainResolver.resolveTrustChains(clientId);
            LOGGER.debug("Resolved service: [{}]", resolvedService);
            return resolvedService.map(service -> {
                if (currentService != null) {
                    service.setId(currentService.getId());
                }
                return (OAuthRegisteredService) servicesManager.save(service);
            });
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, "Unable to resolve federated service [" + clientId + "]", e);
            return Optional.empty();
        }
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
