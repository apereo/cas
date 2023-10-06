package org.apereo.cas.oidc.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuth20ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class OidcServicesManagerRegisteredServiceLocator extends OAuth20ServicesManagerRegisteredServiceLocator {
    /**
     * Execution order of this locator.
     */
    static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 1;

    public OidcServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties) {
        super(casProperties);
        setOrder(DEFAULT_ORDER);
        setRegisteredServiceFilter((registeredService, service) -> supports(registeredService, service)
            && doesClientIdBelongToRegisteredService((OAuthRegisteredService) registeredService, service));
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return registeredService instanceof OidcRegisteredService && supportsInternal(registeredService, service);
    }

    @Override
    protected Class<? extends RegisteredService> getRegisteredServiceIndexedType() {
        return OidcRegisteredService.class;
    }
}

