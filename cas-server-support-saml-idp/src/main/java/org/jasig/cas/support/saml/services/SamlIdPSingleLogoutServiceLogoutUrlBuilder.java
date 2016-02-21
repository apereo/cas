package org.jasig.cas.support.saml.services;

import org.jasig.cas.client.util.URIBuilder;
import org.jasig.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.impl.SingleLogoutServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class SamlIdPSingleLogoutServiceLogoutUrlBuilder extends DefaultSingleLogoutServiceLogoutUrlBuilder {

    /**
     * The Services manager.
     */
    @Autowired
    @Qualifier("servicesManager")
    protected ReloadableServicesManager servicesManager;

    /**
     * The Saml registered service caching metadata resolver.
     */
    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @Override
    public URL determineLogoutUrl(final RegisteredService registeredService,
                                  final org.jasig.cas.logout.SingleLogoutService singleLogoutService) {

        try {
            if (registeredService instanceof SamlRegisteredService) {
                final URIBuilder builder = new URIBuilder(singleLogoutService.getOriginalUrl());
                for (final URIBuilder.BasicNameValuePair basicNameValuePair : builder.getQueryParams()) {
                    if (basicNameValuePair.getName().equalsIgnoreCase(SamlProtocolConstants.PARAMETER_ENTITY_ID)) {
                        final String entityID = basicNameValuePair.getValue();

                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                                SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
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

    private static SingleLogoutService getSingleLogoutService(final String endpoint) {
        final SingleLogoutService acs = new SingleLogoutServiceBuilder().buildObject();
        acs.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        acs.setLocation(endpoint);
        return acs;
    }
}
