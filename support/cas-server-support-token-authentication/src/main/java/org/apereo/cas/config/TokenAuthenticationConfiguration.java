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
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("tokenAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TokenAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "tokenPrincipalFactory")
    @Bean
    public PrincipalFactory tokenPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "tokenAuthenticationHandler")
    @Bean
    public AuthenticationHandler tokenAuthenticationHandler() {
        val token = casProperties.getAuthn().getToken();
        return new TokenAuthenticationHandler(token.getName(), servicesManager.getObject(), tokenPrincipalFactory(),
            PrincipalNameTransformerUtils.newPrincipalNameTransformer(token.getPrincipalTransformation()));
    }

    @ConditionalOnMissingBean(name = "tokenAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer tokenAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(tokenAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }
}
