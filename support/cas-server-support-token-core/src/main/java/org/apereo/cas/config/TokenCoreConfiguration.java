package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.token.JwtTokenCipherSigningPublicKeyEndpoint;
import org.apereo.cas.token.JwtTokenTicketBuilder;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link TokenCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@Configuration(value = "tokenCoreConfiguration", proxyBeanMethods = false)
public class TokenCoreConfiguration {

    @Configuration(value = "TokenCoreValidatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TokenCoreValidatorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "tokenTicketValidator")
        public TicketValidator tokenTicketValidator(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new InternalTicketValidator(centralAuthenticationService,
                webApplicationServiceFactory, authenticationAttributeReleasePolicy, servicesManager);
        }

    }
    
    @Configuration(value = "TokenCoreJwtConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TokenCoreJwtConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "tokenCipherExecutor")
        @Autowired
        public CipherExecutor tokenCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getToken().getCrypto();
            val enabled = FunctionUtils.doIf(!crypto.isEnabled() && StringUtils.isNotBlank(crypto.getEncryption().getKey())
                                             && StringUtils.isNotBlank(crypto.getSigning().getKey()),
                () -> {
                    LOGGER.warn("Token encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                                + "are defined for operations. CAS will proceed to enable the token encryption/signing functionality.");
                    return Boolean.TRUE;
                }, crypto::isEnabled).get();
            if (enabled) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, JwtTicketCipherExecutor.class);
            }
            LOGGER.info("Token cookie encryption/signing is turned off. This "
                        + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
                        + "signing and verification of generated tokens.");
            return CipherExecutor.noOp();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "tokenTicketJwtBuilder")
        public JwtBuilder tokenTicketJwtBuilder(
            @Qualifier("tokenCipherExecutor")
            final CipherExecutor tokenCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new JwtBuilder(tokenCipherExecutor, servicesManager, new RegisteredServiceJwtTicketCipherExecutor());
        }
    }

    @Configuration(value = "TokenCoreBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TokenCoreBuilderConfiguration {


        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "tokenTicketBuilder")
        @Autowired
        public TokenTicketBuilder tokenTicketBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("tokenTicketValidator")
            final TicketValidator tokenTicketValidator,
            @Qualifier("tokenTicketJwtBuilder")
            final JwtBuilder tokenTicketJwtBuilder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY)
            final ExpirationPolicyBuilder grantingTicketExpirationPolicy) {
            return new JwtTokenTicketBuilder(tokenTicketValidator,
                grantingTicketExpirationPolicy, tokenTicketJwtBuilder, servicesManager, casProperties);
        }
    }
    
    @Configuration(value = "TokenCoreWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class TokenCoreWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public JwtTokenCipherSigningPublicKeyEndpoint jwtTokenCipherSigningPublicKeyEndpoint(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties,
            @Qualifier("tokenCipherExecutor")
            final CipherExecutor tokenCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new JwtTokenCipherSigningPublicKeyEndpoint(casProperties,
                tokenCipherExecutor, servicesManager, webApplicationServiceFactory);
        }
    }
}
