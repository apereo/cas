package org.apereo.cas.config;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
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
@AutoConfigureAfter(CasCoreServicesConfiguration.class)
public class CasCoreServicesAuthenticationConfiguration {

    @Bean
    public ProtocolAttributeEncoder noOpCasAttributeEncoder() {
        return new NoOpProtocolAttributeEncoder();
    }

    @ConditionalOnMissingBean(name = "casAttributeEncoder")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public ProtocolAttributeEncoder casAttributeEncoder(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("cacheCredentialsCipherExecutor")
        final CipherExecutor cacheCredentialsCipherExecutor,
        @Qualifier(RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME)
        final RegisteredServiceCipherExecutor registeredServiceCipherExecutor) {
        return new DefaultCasProtocolAttributeEncoder(servicesManager, registeredServiceCipherExecutor, cacheCredentialsCipherExecutor);
    }
}
