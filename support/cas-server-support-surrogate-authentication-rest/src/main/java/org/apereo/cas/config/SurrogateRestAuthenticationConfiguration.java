package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateRestAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateRestAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("surrogateRestAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateRestAuthenticationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateRestAuthenticationConfiguration.class);

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
        LOGGER.debug("Using REST endpoint [{}] with method [{}] to locate surrogate accounts",
                su.getRest().getUrl(), su.getRest().getMethod());
        return new SurrogateRestAuthenticationService(su.getRest(), servicesManager);
    }
}
