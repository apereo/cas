package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.authentication.principal.TokenWebApplicationServiceResponseBuilder;
import org.apereo.cas.web.UrlValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link TokenTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "tokenTicketsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TokenTicketsConfiguration {

    @Configuration(value = "TokenTicketsBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TokenTicketsBuilderConfiguration {
        @Autowired
        @Bean
        public ResponseBuilder webApplicationServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("tokenTicketBuilder")
            final TokenTicketBuilder tokenTicketBuilder,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator) {
            return new TokenWebApplicationServiceResponseBuilder(servicesManager, tokenTicketBuilder, urlValidator);
        }
    }

}
