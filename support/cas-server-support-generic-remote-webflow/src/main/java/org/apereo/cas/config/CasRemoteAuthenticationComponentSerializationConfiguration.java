package org.apereo.cas.config;

import org.apereo.cas.adaptors.generic.remote.RemoteAddressCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasRemoteAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "casRemoteAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasRemoteAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "remoteAuthnComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer remoteAuthnComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(RemoteAddressCredential.class);
    }
}
