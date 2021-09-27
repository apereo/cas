package org.apereo.cas.config.authentication.support;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration(value = "samlAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "samlAuthenticationMetaDataPopulator")
    @Bean
    public AuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator() {
        return new SamlAuthenticationMetaDataPopulator();
    }

    @ConditionalOnMissingBean(name = "samlAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer samlAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("samlAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator) {
        return plan -> plan.registerAuthenticationMetadataPopulator(samlAuthenticationMetaDataPopulator);
    }
}
