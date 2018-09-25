package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.UrlValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URL;
import java.util.Collection;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlIdPSingleLogoutServiceLogoutUrlBuilder extends DefaultSingleLogoutServiceLogoutUrlBuilder {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The Saml registered service caching metadata resolver.
     */
    protected final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    public SamlIdPSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                      final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                      final UrlValidator urlValidator) {
        super(urlValidator);
        this.servicesManager = servicesManager;
        this.samlRegisteredServiceCachingMetadataResolver = resolver;
    }

    @Override
    public Collection<URL> determineLogoutUrl(final RegisteredService registeredService,
                                              final WebApplicationService singleLogoutService) {

        try {
            if (registeredService instanceof SamlRegisteredService) {
                val location = buildLogoutUrl(registeredService, singleLogoutService);
                if (location != null) {
                    LOGGER.info("Final logout URL built for [{}] is [{}]", registeredService, location);
                    return CollectionUtils.wrap(location);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("Service [{}] is not a SAML service, or its logout url could not be determined", registeredService);
        return super.determineLogoutUrl(registeredService, singleLogoutService);
    }

    private URL buildLogoutUrl(final RegisteredService registeredService, final WebApplicationService singleLogoutService) throws Exception {
        LOGGER.debug("Building logout url for SAML service [{}]", registeredService);
        val entityID = singleLogoutService.getId();
        LOGGER.debug("Located entity id [{}]", entityID);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver, (SamlRegisteredService) registeredService, entityID);
        if (!adaptor.isPresent()) {
            LOGGER.warn("Cannot find metadata linked to [{}]", entityID);
            return null;
        }
        val location = adaptor.get().getSingleLogoutService().getLocation();
        return new URL(location);
    }
}
