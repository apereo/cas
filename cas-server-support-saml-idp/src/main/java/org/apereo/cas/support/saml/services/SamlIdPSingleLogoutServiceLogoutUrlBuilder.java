package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.client.util.URIBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import java.net.URL;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPSingleLogoutServiceLogoutUrlBuilder extends DefaultSingleLogoutServiceLogoutUrlBuilder {

    /**
     * The Services manager.
     */
    protected ServicesManager servicesManager;

    /**
     * The Saml registered service caching metadata resolver.
     */
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Override
    public URL determineLogoutUrl(final RegisteredService registeredService,
                                  final org.apereo.cas.logout.SingleLogoutService singleLogoutService) {

        try {
            if (registeredService instanceof SamlRegisteredService) {
                final URIBuilder builder = new URIBuilder(singleLogoutService.getOriginalUrl());
                for (final URIBuilder.BasicNameValuePair basicNameValuePair : builder.getQueryParams()) {
                    if (basicNameValuePair.getName().equalsIgnoreCase(SamlProtocolConstants.PARAMETER_ENTITY_ID)) {
                        final String entityID = basicNameValuePair.getValue();

                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                                SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
                                        SamlRegisteredService.class.cast(registeredService), entityID);

                        final String location = adaptor.getSingleLogoutService().getLocation();
                        return new URL(location);
                    }
                }
            }

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return super.determineLogoutUrl(registeredService, singleLogoutService);
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setSamlRegisteredServiceCachingMetadataResolver(
            final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver) {
        this.samlRegisteredServiceCachingMetadataResolver = samlRegisteredServiceCachingMetadataResolver;
    }
}
