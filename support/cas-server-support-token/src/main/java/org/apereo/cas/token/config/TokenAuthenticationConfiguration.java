package org.apereo.cas.token.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.token.authentication.TokenAuthenticationHandler;
import org.apereo.cas.token.authentication.principal.TokenWebApplicationServiceResponseBuilder;
import org.apereo.cas.token.cipher.TokenTicketCipherExecutor;
import org.apereo.cas.token.webflow.TokenAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link TokenAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("tokenAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TokenAuthenticationConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Bean
    public ResponseBuilder webApplicationServiceResponseBuilder() {
        return new TokenWebApplicationServiceResponseBuilder(servicesManager,
                tokenCipherExecutor(),
                grantingTicketExpirationPolicy);
    }

    @ConditionalOnMissingBean(name = "tokenPrincipalFactory")
    @Bean
    public PrincipalFactory tokenPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationHandler tokenAuthenticationHandler() {
        final TokenAuthenticationProperties token = casProperties.getAuthn().getToken();
        return new TokenAuthenticationHandler(token.getName(), servicesManager, tokenPrincipalFactory(),
                Beans.newPrincipalNameTransformer(token.getPrincipalTransformation()));
    }

    @Bean
    public Action tokenAuthenticationAction() {
        return new TokenAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy, servicesManager);
    }

    @Bean
    public CipherExecutor tokenCipherExecutor() {
        final CryptographyProperties crypto = casProperties.getAuthn().getToken().getCrypto();
        return new TokenTicketCipherExecutor(crypto.getEncryption().getKey(), crypto.getSigning().getKey());
    }

    /**
     * The type Token authentication event execution plan configuration.
     */
    @Configuration("tokenAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class TokenAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Autowired
        @Qualifier("personDirectoryPrincipalResolver")
        private PrincipalResolver personDirectoryPrincipalResolver;

        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(tokenAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }
}
