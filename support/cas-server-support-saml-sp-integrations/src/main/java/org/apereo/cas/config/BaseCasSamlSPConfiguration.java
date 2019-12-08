package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;

import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This is {@link BaseCasSamlSPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseCasSamlSPConfiguration implements InitializingBean {
    /**
     * CAS properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> samlRegisteredServiceCachingMetadataResolver;

    @Override
    public void afterPropertiesSet() {
        init();
    }

    public void init() {
        val service = SamlSPUtils.newSamlServiceProviderService(getServiceProvider(),
            samlRegisteredServiceCachingMetadataResolver.getObject());
        if (service != null) {
            finalizeRegisteredService(service);
            SamlSPUtils.saveService(service, servicesManager.getObject());
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
