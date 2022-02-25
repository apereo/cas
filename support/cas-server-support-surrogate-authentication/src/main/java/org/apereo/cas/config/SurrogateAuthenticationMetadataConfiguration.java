package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.SurrogateAuthenticationMetaDataPopulator;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateAuthenticationMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration(value = "SurrogateAuthenticationMetadataConfiguration", proxyBeanMethods = false)
public class SurrogateAuthenticationMetadataConfiguration {

    @Configuration(value = "SurrogateAuthenticationMetadataBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationMetadataBaseConfiguration {

        @ConditionalOnMissingBean(name = "surrogateAuthenticationMetadataPopulator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator surrogateAuthenticationMetadataPopulator() {
            return new SurrogateAuthenticationMetaDataPopulator();
        }

    }

    @Configuration(value = "SurrogateAuthenticationMetadataPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SurrogateAuthenticationMetadataPlanConfiguration {
        @ConditionalOnMissingBean(name = "surrogateAuthenticationMetadataConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationMetadataConfigurer(
            @Qualifier("surrogateAuthenticationMetadataPopulator")
            final AuthenticationMetaDataPopulator surrogateAuthenticationMetadataPopulator) {
            return plan -> plan.registerAuthenticationMetadataPopulator(surrogateAuthenticationMetadataPopulator);
        }
    }
}
