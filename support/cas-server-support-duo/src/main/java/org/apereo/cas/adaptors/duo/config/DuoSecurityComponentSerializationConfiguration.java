package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoDirectCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DuoSecurityComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("duoSecurityComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(DuoCredential.class);
        plan.registerSerializableClass(DuoDirectCredential.class);
    }
}
