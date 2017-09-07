package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningJwtCryptographyProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.token.JWTTokenTicketBuilder;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.cipher.TokenTicketCipherExecutor;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link TokenCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("tokenCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class TokenCoreConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casClientTicketValidator")
    private AbstractUrlBasedTicketValidator casClientTicketValidator;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "tokenCipherExecutor")
    public CipherExecutor tokenCipherExecutor() {
        final EncryptionOptionalSigningJwtCryptographyProperties crypto = casProperties.getAuthn().getToken().getCrypto();
        return new TokenTicketCipherExecutor(crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg(), crypto.isEncryptionEnabled());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "tokenTicketBuilder")
    public TokenTicketBuilder tokenTicketBuilder() {
        return new JWTTokenTicketBuilder(casClientTicketValidator,
                casProperties.getServer().getPrefix(),
                tokenCipherExecutor(),
                grantingTicketExpirationPolicy.getTimeToLive());
    }
}
