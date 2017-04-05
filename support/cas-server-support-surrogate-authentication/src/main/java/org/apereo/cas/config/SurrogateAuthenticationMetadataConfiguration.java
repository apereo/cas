package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.SurrogateAuthenticationMetadataPopulator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateAuthenticationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("SurrogateAuthenticationMetadataConfiguration")
public class SurrogateAuthenticationMetadataConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    @ConditionalOnMissingBean(name = "surrogateAuthenticationMetadataPopulator")
    @Bean
    public AuthenticationMetaDataPopulator surrogateAuthenticationMetadataPopulator() {
        return new SurrogateAuthenticationMetadataPopulator();
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerMetadataPopulator(surrogateAuthenticationMetadataPopulator());
    }
}
