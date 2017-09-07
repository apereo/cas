package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreServicesAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casCoreServicesAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnBean(value = AuthenticationManager.class)
public class CasCoreServicesAuthenticationConfiguration {
    @Bean
    public ProtocolAttributeEncoder noOpCasAttributeEncoder() {
        return new NoOpProtocolAttributeEncoder();
    }

    @ConditionalOnMissingBean(name = "casAttributeEncoder")
    @RefreshScope
    @Bean
    public ProtocolAttributeEncoder casAttributeEncoder(@Qualifier("servicesManager")
                                                        final ServicesManager servicesManager,
                                                        @Qualifier("cacheCredentialsCipherExecutor")
                                                        final CipherExecutor cacheCredentialsCipherExecutor,
                                                        @Qualifier("registeredServiceCipherExecutor")
                                                        final RegisteredServiceCipherExecutor registeredServiceCipherExecutor){
        return new DefaultCasProtocolAttributeEncoder(servicesManager, registeredServiceCipherExecutor, cacheCredentialsCipherExecutor);
    }
}
