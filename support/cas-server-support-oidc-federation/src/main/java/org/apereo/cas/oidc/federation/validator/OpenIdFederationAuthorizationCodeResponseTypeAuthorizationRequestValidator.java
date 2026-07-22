package org.apereo.cas.oidc.federation.validator;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is {@link OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Jerome LELEU
 * @since 8.1.0
 */
@Slf4j
public class OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator
    extends OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator {
    private static final Duration SERVICE_EXPIRATION_REFRESH_WINDOW = Duration.ofMinutes(5);
    private static final Map<String, ReentrantLock> SERVICE_LOCKS = new ConcurrentHashMap<>();

    private final OidcFederationTrustChainResolver oidcFederationTrustChainResolver;

    public OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final OAuth20RequestParameterResolver requestParameterResolver,
        final OidcFederationTrustChainResolver oidcFederationTrustChainResolver) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer, requestParameterResolver);
        this.oidcFederationTrustChainResolver = oidcFederationTrustChainResolver;
    }

    @Override
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        val registeredService = super.getRegisteredServiceByClientId(clientId);
        if (registeredService != null) {
            return resolveWhenTemporaryServiceAlmostExpired(clientId, registeredService);
        }
        return resolveMissingFederatedService(clientId);
    }

    private OAuthRegisteredService resolveMissingFederatedService(final String clientId) {
        if (!isFederatedEntityId(clientId)) {
            return null;
        }
        val serviceLock = SERVICE_LOCKS.computeIfAbsent(clientId, key -> new ReentrantLock());
        serviceLock.lock();
        try {
            val existingService = super.getRegisteredServiceByClientId(clientId);
            if (existingService != null) {
                return existingService;
            }
            return resolveAndSaveFederatedService(clientId).orElse(null);
        } finally {
            serviceLock.unlock();
        }
    }

    private OAuthRegisteredService resolveWhenTemporaryServiceAlmostExpired(final String clientId,
                                                                            final OAuthRegisteredService registeredService) {
        if (!shouldRefreshTemporaryFederationService(registeredService)) {
            return registeredService;
        }
        val serviceLock = SERVICE_LOCKS.computeIfAbsent(clientId, key -> new ReentrantLock());
        serviceLock.lock();
        try {
            return resolveAndSaveFederatedService(clientId).orElse(registeredService);
        } finally {
            serviceLock.unlock();
        }
    }

    private Optional<OAuthRegisteredService> resolveAndSaveFederatedService(final String clientId) {
        try {
            val resolvedService = oidcFederationTrustChainResolver.resolveTrustChains(clientId);
            LOGGER.debug("Resolved service: [{}]", resolvedService);
            return resolvedService.map(service -> (OAuthRegisteredService) getServicesManager().save(service));
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, "Unable to resolve federated service [" + clientId + "]", e);
            return Optional.empty();
        }
    }

    private static boolean shouldRefreshTemporaryFederationService(final OAuthRegisteredService registeredService) {
        if (!registeredService.getProperties().containsKey(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE)) {
            return false;
        }
        val expirationDate = registeredService.getExpirationPolicy().getExpirationDate();
        val expiration = DateTimeUtils.zonedDateTimeOf(expirationDate);
        val refresh = expiration.minus(SERVICE_EXPIRATION_REFRESH_WINDOW).isBefore(ZonedDateTime.now(Clock.systemUTC()));
        LOGGER.debug("Should refresh: [{}] upfront: [{}]", registeredService, refresh);
        return refresh;
    }

    private static boolean isFederatedEntityId(final String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return false;
        }
        val normalized = StringUtils.lowerCase(clientId, Locale.ENGLISH);
        return StringUtils.startsWith(normalized, "http://") || StringUtils.startsWith(normalized, "https://");
    }
}
