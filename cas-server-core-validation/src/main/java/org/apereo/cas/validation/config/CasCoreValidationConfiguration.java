package org.apereo.cas.validation.config;

import org.apereo.cas.validation.Cas10ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.ValidationSpecification;
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
public class CasCoreValidationConfiguration {
    
    @Bean
    @Scope(value = "prototype")
    public ValidationSpecification cas10ProtocolValidationSpecification() {
        return new Cas10ProtocolValidationSpecification();
    }

    @Bean
    @Scope(value = "prototype")
    public ValidationSpecification cas20ProtocolValidationSpecification() {
        return new Cas20ProtocolValidationSpecification();
    }

    @Bean
    @Scope(value = "prototype")
    public ValidationSpecification cas20WithoutProxyProtocolValidationSpecification() {
        return new Cas20WithoutProxyingValidationSpecification();
    }
}
