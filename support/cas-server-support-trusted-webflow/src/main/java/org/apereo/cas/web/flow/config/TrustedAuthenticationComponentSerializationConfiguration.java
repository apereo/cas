package org.apereo.cas.web.flow.config;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link TrustedAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "trustedAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TrustedAuthenticationComponentSerializationConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "trustedAuthnComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer trustedAuthnComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(PrincipalBearingCredential.class);
    }
}
