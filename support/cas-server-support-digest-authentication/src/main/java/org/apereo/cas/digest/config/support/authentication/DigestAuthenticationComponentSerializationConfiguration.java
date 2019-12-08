package org.apereo.cas.digest.config.support.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.digest.DigestCredential;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DigestAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "digestAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DigestAuthenticationComponentSerializationConfiguration implements ComponentSerializationPlanConfigurer {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(DigestCredential.class);
    }
}
