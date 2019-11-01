package org.apereo.cas.config;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasCoreServicesAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casCoreServicesAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync
public class CasCoreServicesAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("cacheCredentialsCipherExecutor")
    private ObjectProvider<CipherExecutor> cacheCredentialsCipherExecutor;

    @Autowired
    @Qualifier("registeredServiceCipherExecutor")
    private ObjectProvider<RegisteredServiceCipherExecutor> registeredServiceCipherExecutor;

    @Bean
    public ProtocolAttributeEncoder noOpCasAttributeEncoder() {
        return new NoOpProtocolAttributeEncoder();
    }

    @ConditionalOnMissingBean(name = "casAttributeEncoder")
    @RefreshScope
    @Bean
    public ProtocolAttributeEncoder casAttributeEncoder() {
        return new DefaultCasProtocolAttributeEncoder(servicesManager.getObject(),
            registeredServiceCipherExecutor.getObject(),
            cacheCredentialsCipherExecutor.getObject());
    }
}
