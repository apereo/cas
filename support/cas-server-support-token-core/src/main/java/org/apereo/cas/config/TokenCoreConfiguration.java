package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.token.JwtTokenCipherSigningPublicKeyEndpoint;
import org.apereo.cas.token.JwtTokenTicketBuilder;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
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
@Slf4j
public class TokenCoreConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casClientTicketValidator")
    private ObjectProvider<AbstractUrlBasedTicketValidator> casClientTicketValidator;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> grantingTicketExpirationPolicy;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "tokenCipherExecutor")
    public CipherExecutor tokenCipherExecutor() {
        val crypto = casProperties.getAuthn().getToken().getCrypto();

        val enabled = FunctionUtils.doIf(
            !crypto.isEnabled() && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey()),
            () -> {
                LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                    + "are defined for operations. CAS will proceed to enable the token encryption/signing functionality.");
                return Boolean.TRUE;
            },
            crypto::isEnabled)
            .get();

        if (enabled) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, JwtTicketCipherExecutor.class);
        }
        LOGGER.info("Token cookie encryption/signing is turned off. This "
            + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
            + "signing and verification of generated tokens.");
        return CipherExecutor.noOp();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "tokenTicketBuilder")
    public TokenTicketBuilder tokenTicketBuilder() {
        return new JwtTokenTicketBuilder(casClientTicketValidator.getObject(),
            grantingTicketExpirationPolicy.getObject(),
            tokenTicketJwtBuilder(),
            servicesManager.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "tokenTicketJwtBuilder")
    public JwtBuilder tokenTicketJwtBuilder() {
        return new JwtBuilder(
            casProperties.getServer().getPrefix(),
            tokenCipherExecutor(),
            servicesManager.getObject(),
            new RegisteredServiceJwtTicketCipherExecutor());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public JwtTokenCipherSigningPublicKeyEndpoint jwtTokenCipherSigningPublicKeyEndpoint() {
        return new JwtTokenCipherSigningPublicKeyEndpoint(casProperties, tokenCipherExecutor(), this.servicesManager.getObject());
    }
}
