package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link X509AuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "x509AuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "x509ComponentSerializationPlanConfigurer")
    @RefreshScope
    public ComponentSerializationPlanConfigurer x509ComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(X509CertificateCredential.class);
    }
}
