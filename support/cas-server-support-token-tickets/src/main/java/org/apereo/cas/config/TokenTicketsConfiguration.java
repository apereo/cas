package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.authentication.principal.TokenWebApplicationServiceResponseBuilder;
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
@Configuration("tokenTicketsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TokenTicketsConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("tokenTicketBuilder")
    private TokenTicketBuilder tokenTicketBuilder;

    @Bean
    public ResponseBuilder webApplicationServiceResponseBuilder() {
        return new TokenWebApplicationServiceResponseBuilder(servicesManager, tokenTicketBuilder);
    }

}
