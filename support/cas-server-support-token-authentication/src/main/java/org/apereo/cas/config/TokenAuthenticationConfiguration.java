package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.authentication.TokenAuthenticationHandler;

import lombok.val;
import org.pac4j.core.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link TokenAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "tokenAuthenticationConfiguration", proxyBeanMethods = false)
public class TokenAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "tokenPrincipalFactory")
    @Bean
    public PrincipalFactory tokenPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "tokenAuthenticationHandler")
    @Bean
    @Autowired
    public AuthenticationHandler tokenAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                            @Qualifier("tokenPrincipalFactory")
                                                            final PrincipalFactory tokenPrincipalFactory,
                                                            @Qualifier(ServicesManager.BEAN_NAME)
                                                            final ServicesManager servicesManager) {
        val token = casProperties.getAuthn().getToken();
        val principalNameTransformer = PrincipalNameTransformerUtils.newPrincipalNameTransformer(token.getPrincipalTransformation());
        val handler = new TokenAuthenticationHandler(token.getName(), servicesManager, tokenPrincipalFactory, principalNameTransformer, JEESessionStore.INSTANCE);
        handler.setState(token.getState());
        return handler;
    }

    @ConditionalOnMissingBean(name = "tokenAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer tokenAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("tokenAuthenticationHandler")
        final AuthenticationHandler tokenAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(tokenAuthenticationHandler, defaultPrincipalResolver);
    }
}
