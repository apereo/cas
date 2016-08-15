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
 * This is {@link CasSamlSPBoxConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSamlSPBoxConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPBoxConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @PostConstruct
    public void init() {
        final SamlRegisteredService service = SamlSPUtils.newSamlServiceProviderService(
                casProperties.getSamlSP().getBox().getName(),
                casProperties.getSamlSP().getBox().getDescription(),
                casProperties.getSamlSP().getBox().getMetadata(),
                casProperties.getSamlSP().getBox().getAttributes()
        );
        if (service != null) {
            service.setSignResponses(true);
            servicesManager.save(service);
            servicesManager.load();
        }
    }
}
