package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * This is {@link CasSamlSPZoomConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casSamlSPZoomConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPZoomConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;
    
    @PostConstruct
    public void init() {
        final SamlRegisteredService service = SamlSPUtils.newSamlServiceProviderService(
                casProperties.getSamlSP().getZoom(), 
                samlRegisteredServiceCachingMetadataResolver);
        if (service != null) {
            service.setSignResponses(true);
            SamlSPUtils.saveService(service, this.servicesManager);
        }
    }
}
