package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DuoSecurityComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "duoSecurityComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityComponentSerializationConfiguration implements ComponentSerializationPlanConfigurer {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(DuoSecurityCredential.class);
        plan.registerSerializableClass(DuoSecurityDirectCredential.class);
    }
}
