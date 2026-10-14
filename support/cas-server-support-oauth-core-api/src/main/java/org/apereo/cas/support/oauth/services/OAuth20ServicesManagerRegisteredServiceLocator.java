package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.BaseServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceIndexService;
import org.apereo.cas.services.query.BasicRegisteredServiceQueryIndex;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.services.query.RegisteredServiceQueryAttribute;
import org.apereo.cas.services.query.RegisteredServiceQueryIndex;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class OAuth20ServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {
    protected final CasConfigurationProperties casProperties;

    private final @Nullable ObjectProvider<RegisteredServiceIndexService> registeredServiceIndexService;

    public OAuth20ServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties) {
        this(casProperties, null);
    }

    public OAuth20ServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties,
                                                          final @Nullable ObjectProvider<RegisteredServiceIndexService> registeredServiceIndexService) {
        this.casProperties = casProperties;
        this.registeredServiceIndexService = registeredServiceIndexService;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> supports(registeredService, service)
            && doesClientIdBelongToRegisteredService((OAuthRegisteredService) registeredService, service));
    }

    /**
     * Locate the service through the {@code clientId} index when it has a match among the candidates,
     * and fall back to examining every candidate otherwise.
     */
    @Override
    public @Nullable RegisteredService locate(final Collection<? extends RegisteredService> candidates, final Service service) {
        val indexedServiceIds = findIndexedServiceIdsByClientId(service);
        if (!indexedServiceIds.isEmpty()) {
            val indexedService = candidates
                .stream()
                .filter(registeredService -> indexedServiceIds.contains(registeredService.getId()))
                .filter(registeredService -> getRegisteredServiceFilter().test(registeredService, service))
                .findFirst();
            if (indexedService.isPresent()) {
                return indexedService.get();
            }
        }
        return super.locate(candidates, service);
    }

    protected boolean doesClientIdBelongToRegisteredService(final OAuthRegisteredService registeredService, final Service service) {
        LOGGER.trace("Attempting to locate service [{}] via [{}]", service, registeredService);
        val clientId = getClientIdFrom(service);
        return StringUtils.isNotBlank(clientId) && Strings.CI.equals(registeredService.getClientId(), clientId);
    }

    private static String getClientIdFrom(final Service service) {
        val clientIdAttribute = service.getAttributes().get(OAuth20Constants.CLIENT_ID);
        return CollectionUtils.firstElement(clientIdAttribute).map(Object::toString).orElse(StringUtils.EMPTY);
    }

    private Set<Long> findIndexedServiceIdsByClientId(final Service service) {
        val clientId = getClientIdFrom(service);
        val indexService = registeredServiceIndexService != null ? registeredServiceIndexService.getIfAvailable() : null;
        if (StringUtils.isBlank(clientId) || indexService == null || !indexService.isEnabled()) {
            return Set.of();
        }
        val query = RegisteredServiceQuery.of(getRegisteredServiceIndexedType().getValue(), "clientId", clientId, true);
        return indexService.findServiceBy(query)
            .map(RegisteredService::getId)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return registeredService instanceof OAuthRegisteredService && supportsInternal(registeredService, service);
    }

    @Override
    public List<RegisteredServiceQueryIndex> getRegisteredServiceIndexes() {
        val indexes = super.getRegisteredServiceIndexes();
        val registeredServiceIndexedType = getRegisteredServiceIndexedType();
        indexes.add(BasicRegisteredServiceQueryIndex.hashIndex(
            new RegisteredServiceQueryAttribute(registeredServiceIndexedType.getValue(), String.class, "clientId")));
        return indexes;
    }

    @Override
    protected Pair<String, Class<? extends RegisteredService>> getRegisteredServiceIndexedType() {
        return Pair.of(OAuthRegisteredService.OAUTH_REGISTERED_SERVICE_FRIENDLY_NAME, OAuthRegisteredService.class);
    }

    protected boolean supportsInternal(final RegisteredService registeredService, final Service givenService) {
        val attributes = givenService.getAttributes();
        if (attributes.containsKey(OAuth20Constants.CLIENT_ID)) {
            val service = (WebApplicationService) givenService;
            val source = CollectionUtils.firstElement(attributes.get(service.getSource()))
                .map(String.class::cast)
                .orElse(StringUtils.EMPTY);
            val callbackService = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
            return StringUtils.isBlank(source)
                || Strings.CI.startsWith(source, callbackService)
                || OAuth20Utils.checkCallbackValid(registeredService, source);
        }
        return false;
    }
}

