package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.token.authentication.principal.TokenWebApplicationServiceResponseBuilder;
import org.apereo.cas.token.cipher.TokenTicketCipherExecutor;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
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
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casClientTicketValidator")
    private AbstractUrlBasedTicketValidator casClientTicketValidator;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public ResponseBuilder webApplicationServiceResponseBuilder() {
        return new TokenWebApplicationServiceResponseBuilder(servicesManager,
                tokenCipherExecutor(),
                grantingTicketExpirationPolicy,
                casClientTicketValidator);
    }

    @Bean
    public CipherExecutor tokenCipherExecutor() {
        final CryptographyProperties crypto = casProperties.getAuthn().getToken().getCrypto();
        return new TokenTicketCipherExecutor(crypto.getEncryption().getKey(), crypto.getSigning().getKey());
    }

}
