package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.SamlSPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * This is {@link CasSamlSPSAManageConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSamlSPSAManageConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPSAManageConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @PostConstruct
    public void init() {
        final SamlRegisteredService service = SamlSPUtils.newSamlServiceProviderService(
                casProperties.getSamlSP().getSaManage().getName(),
                casProperties.getSamlSP().getSaManage().getDescription(),
                casProperties.getSamlSP().getSaManage().getMetadata(),
                casProperties.getSamlSP().getSaManage().getNameIdAttribute()
        );
        if (service != null) {
            service.setSignResponses(true);
            servicesManager.save(service);
            servicesManager.load();
        }
    }
}
