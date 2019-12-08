package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorTokenCredential;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link AccepttoMultifactorAuthenticationComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "accepttoMultifactorAuthenticationComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AccepttoMultifactorAuthenticationComponentSerializationConfiguration
    implements ComponentSerializationPlanConfigurer {

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(AccepttoMultifactorTokenCredential.class);
        plan.registerSerializableClass(AccepttoEmailCredential.class);
    }
}
