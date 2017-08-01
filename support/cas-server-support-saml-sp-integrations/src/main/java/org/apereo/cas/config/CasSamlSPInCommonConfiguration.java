package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * This is {@link CasSamlSPInCommonConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSamlSPInCommonConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSamlSPInCommonConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasSamlSPInCommonConfiguration.class);

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
                casProperties.getSamlSp().getInCommon(),
                samlRegisteredServiceCachingMetadataResolver);
        if (service != null) {
            SamlSPUtils.saveService(service, servicesManager);

            LOGGER.info("Launching background thread to load the InCommon metadata. Depending on bandwidth, this might take a while...");
            new Thread(() -> {
                LOGGER.debug("Loading InCommon metadata at [{}]...", service.getMetadataLocation());
                samlRegisteredServiceCachingMetadataResolver.resolve(service);
            }).start();
        }
    }
}
