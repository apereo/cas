package org.apereo.cas.validation.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.validation.Cas10ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * This is {@link CasCoreValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreValidationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreValidationConfiguration {
    
    @Bean
    @Scope(value = "prototype")
    public CasProtocolValidationSpecification cas10ProtocolValidationSpecification() {
        return new Cas10ProtocolValidationSpecification();
    }

    @Bean
    @Scope(value = "prototype")
    public CasProtocolValidationSpecification cas20ProtocolValidationSpecification() {
        return new Cas20ProtocolValidationSpecification();
    }

    @Bean
    @Scope(value = "prototype")
    public CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification() {
        return new Cas20WithoutProxyingValidationSpecification();
    }
}
