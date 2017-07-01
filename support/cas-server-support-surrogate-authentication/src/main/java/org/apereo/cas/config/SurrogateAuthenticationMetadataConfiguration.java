package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.SurrogateAuthenticationMetaDataPopulator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateAuthenticationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("SurrogateAuthenticationMetadataConfiguration")
public class SurrogateAuthenticationMetadataConfiguration {

    @ConditionalOnMissingBean(name = "surrogateAuthenticationMetadataPopulator")
    @Bean
    public AuthenticationMetaDataPopulator surrogateAuthenticationMetadataPopulator() {
        return new SurrogateAuthenticationMetaDataPopulator();
    }

    @ConditionalOnMissingBean(name = "surrogateAuthenticationMetadataConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationMetadataConfigurer() {
        return plan -> plan.registerMetadataPopulator(surrogateAuthenticationMetadataPopulator());
    }
}
