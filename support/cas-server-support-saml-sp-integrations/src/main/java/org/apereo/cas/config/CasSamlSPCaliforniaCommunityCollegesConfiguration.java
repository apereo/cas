package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.SamlSPUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSamlSPCaliforniaCommunityCollegesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casSamlSPCaliforniaCommunityCollegesConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasSamlSPCaliforniaCommunityCollegesConfiguration implements InitializingBean {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> samlRegisteredServiceCachingMetadataResolver;

    @Override
    public void afterPropertiesSet() {
        val resolver = samlRegisteredServiceCachingMetadataResolver.getObject();
        val service = SamlSPUtils.newSamlServiceProviderService(
            casProperties.getSamlSp().getCccco(),
            resolver);
        if (service != null) {
            SamlSPUtils.saveService(service, servicesManager.getObject());

            LOGGER.info("Launching background thread to load the CCC metadata. This might take a while...");
            new Thread(() -> {
                LOGGER.debug("Loading CCC metadata at [{}]...", service.getMetadataLocation());
                resolver.resolve(service, new CriteriaSet());
            }, getClass().getSimpleName()).start();
        }
    }
}
