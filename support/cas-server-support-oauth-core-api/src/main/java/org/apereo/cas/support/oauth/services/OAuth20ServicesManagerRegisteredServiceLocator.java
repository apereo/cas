package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.BaseServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.query.BasicRegisteredServiceQueryIndex;
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
import org.springframework.core.Ordered;
import java.util.List;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class OAuth20ServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {
    protected final CasConfigurationProperties casProperties;

    public OAuth20ServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> supports(registeredService, service)
            && doesClientIdBelongToRegisteredService((OAuthRegisteredService) registeredService, service));
    }

    protected boolean doesClientIdBelongToRegisteredService(final OAuthRegisteredService registeredService, final Service service) {
        LOGGER.trace("Attempting to locate service [{}] via [{}]", service, registeredService);
        val clientIdAttribute = service.getAttributes().get(OAuth20Constants.CLIENT_ID);
        val clientId = CollectionUtils.firstElement(clientIdAttribute).map(Object::toString).orElse(StringUtils.EMPTY);
        return StringUtils.isNotBlank(clientId) && Strings.CI.equals(registeredService.getClientId(), clientId);
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
            return StringUtils.isBlank(source) || Strings.CI.startsWith(source, callbackService)
                || OAuth20Utils.checkCallbackValid(registeredService, source);
        }
        return false;
    }
}

