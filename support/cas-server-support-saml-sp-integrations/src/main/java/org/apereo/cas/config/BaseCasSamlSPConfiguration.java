package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;

/**
 * This is {@link BaseCasSamlSPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseCasSamlSPConfiguration {
    /** CAS properties. */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    @PostConstruct
    public void init() {
        final SamlRegisteredService service = SamlSPUtils.newSamlServiceProviderService(
                getServiceProvider(),
                samlRegisteredServiceCachingMetadataResolver);
        if (service != null) {
            finalizeRegisteredService(service);
            SamlSPUtils.saveService(service, this.servicesManager);
        }
    }
    
    protected abstract AbstractSamlSPProperties getServiceProvider();

    /**
     * Finalize registered service.
     * Template method to customize the service behavior
     * and override default options.
     *
     * @param service the service
     */
    protected void finalizeRegisteredService(final SamlRegisteredService service) {
    } 
}
