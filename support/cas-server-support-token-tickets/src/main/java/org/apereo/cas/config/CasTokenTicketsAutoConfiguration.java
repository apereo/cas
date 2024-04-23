package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.authentication.principal.TokenWebApplicationServiceResponseBuilder;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.UrlValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasTokenTicketsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Tokens)
@AutoConfiguration
public class CasTokenTicketsAutoConfiguration {

    @Configuration(value = "TokenTicketsBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class TokenTicketsBuilderConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResponseBuilder webApplicationServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TokenTicketBuilder.BEAN_NAME)
            final TokenTicketBuilder tokenTicketBuilder,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator) {
            return new TokenWebApplicationServiceResponseBuilder(servicesManager, tokenTicketBuilder, urlValidator);
        }
    }

}
