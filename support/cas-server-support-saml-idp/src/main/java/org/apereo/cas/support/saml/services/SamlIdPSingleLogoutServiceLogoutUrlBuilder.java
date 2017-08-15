package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.web.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPSingleLogoutServiceLogoutUrlBuilder extends DefaultSingleLogoutServiceLogoutUrlBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlIdPSingleLogoutServiceLogoutUrlBuilder.class);

    /**
     * The Services manager.
     */
    protected ServicesManager servicesManager;

    /**
     * The Saml registered service caching metadata resolver.
     */
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    public SamlIdPSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                      final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                      final UrlValidator urlValidator) {
        super(urlValidator);
        this.servicesManager = servicesManager;
        this.samlRegisteredServiceCachingMetadataResolver = resolver;
    }

    @Override
    public URL determineLogoutUrl(final RegisteredService registeredService,
                                  final WebApplicationService singleLogoutService) {

        try {
            if (registeredService instanceof SamlRegisteredService) {
                final URL location = buildLogoutUrl(registeredService, singleLogoutService);
                if (location != null) {
                    LOGGER.info("Final logout URL built for [{}] is [{}]", registeredService, location);
                    return location;
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
        final String entityID = singleLogoutService.getId();
        LOGGER.debug("Located entity id [{}]", entityID);

        final Optional<SamlRegisteredServiceServiceProviderMetadataFacade> adaptor =
                SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
                        SamlRegisteredService.class.cast(registeredService), entityID);

        if (!adaptor.isPresent()) {
            LOGGER.warn("Cannot find metadata linked to [{}]", entityID);
            return null;
        }
        final String location = adaptor.get().getSingleLogoutService().getLocation();
        return new URL(location);
    }
}
