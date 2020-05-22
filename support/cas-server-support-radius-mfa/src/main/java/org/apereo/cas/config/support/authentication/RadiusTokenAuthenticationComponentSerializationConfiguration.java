package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RadiusTokenAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "radiusTokenAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.authn.mfa.radius.client.inet-address")
public class RadiusTokenAuthenticationComponentSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "radiusTokenComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer radiusTokenComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(RadiusTokenCredential.class);
    }
}
