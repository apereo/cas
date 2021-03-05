package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationPolicyResolver;
import org.apereo.cas.authentication.AuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionFactory;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.GroovyAuthenticationPostProcessor;
import org.apereo.cas.authentication.GroovyAuthenticationPreProcessor;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.handler.ByCredentialSourceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.GroovyAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.policy.RegisteredServiceAuthenticationPolicyResolver;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.cache.DefaultPrincipalAttributesRepositoryCache;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuthenticationSupportConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationSupportConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationSupportConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("principalElectionStrategy")
    private ObjectProvider<PrincipalElectionStrategy> principalElectionStrategy;

    @Autowired
    @Qualifier("authenticationTransactionManager")
    private ObjectProvider<AuthenticationTransactionManager> authenticationTransactionManager;

    @Autowired
    @Qualifier("authenticationResultBuilderFactory")
    private ObjectProvider<AuthenticationResultBuilderFactory> authenticationResultBuilderFactory;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("authenticationTransactionFactory")
    private ObjectProvider<AuthenticationTransactionFactory> authenticationTransactionFactory;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "defaultAuthenticationSystemSupport")
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport() {
        return new DefaultAuthenticationSystemSupport(authenticationTransactionManager.getObject(),
            principalElectionStrategy.getObject(), authenticationResultBuilderFactory.getObject(),
            authenticationTransactionFactory.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationHandlerResolver")
    public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        val resolver = new RegisteredServiceAuthenticationHandlerResolver(servicesManager.getObject(),
            authenticationServiceSelectionPlan.getObject());
        resolver.setOrder(casProperties.getAuthn().getCore().getServiceAuthenticationResolution().getOrder());
        return resolver;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationPolicyResolver")
    public AuthenticationPolicyResolver registeredServiceAuthenticationPolicyResolver() {
        return new RegisteredServiceAuthenticationPolicyResolver(servicesManager.getObject(),
            authenticationServiceSelectionPlan.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "groovyAuthenticationHandlerResolver")
    @ConditionalOnProperty(name = "cas.authn.core.groovy-authentication-resolution.location")
    public AuthenticationHandlerResolver groovyAuthenticationHandlerResolver() {
        val groovy = casProperties.getAuthn().getCore().getGroovyAuthenticationResolution();
        return new GroovyAuthenticationHandlerResolver(groovy.getLocation(), servicesManager.getObject(), groovy.getOrder());
    }

    @Bean
    @ConditionalOnMissingBean(name = "byCredentialSourceAuthenticationHandlerResolver")
    @RefreshScope
    public AuthenticationHandlerResolver byCredentialSourceAuthenticationHandlerResolver() {
        return new ByCredentialSourceAuthenticationHandlerResolver();
    }

    @ConditionalOnMissingBean(name = "authenticationHandlerResolversExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer authenticationHandlerResolversExecutionPlanConfigurer() {
        return plan -> {
            if (casProperties.getAuthn().getPolicy().isSourceSelectionEnabled()) {
                plan.registerAuthenticationHandlerResolver(byCredentialSourceAuthenticationHandlerResolver());
            }
            plan.registerAuthenticationHandlerResolver(registeredServiceAuthenticationHandlerResolver());
            plan.registerAuthenticationPolicyResolver(registeredServiceAuthenticationPolicyResolver());
            val groovy = casProperties.getAuthn().getCore().getGroovyAuthenticationResolution();
            if (groovy.getLocation() != null) {
                plan.registerAuthenticationHandlerResolver(groovyAuthenticationHandlerResolver());
            }
        };
    }

    @ConditionalOnMissingBean(name = "groovyAuthenticationProcessorExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer groovyAuthenticationProcessorExecutionPlanConfigurer() {
        return plan -> {
            val engine = casProperties.getAuthn().getCore().getEngine();
            val preResource = engine.getGroovyPreProcessor().getLocation();
            if (preResource != null) {
                plan.registerAuthenticationPreProcessor(new GroovyAuthenticationPreProcessor(preResource));
            }
            val postResource = engine.getGroovyPostProcessor().getLocation();
            if (postResource != null) {
                plan.registerAuthenticationPostProcessor(new GroovyAuthenticationPostProcessor(postResource));
            }
        };
    }

    @ConditionalOnMissingBean(name = PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME)
    @Bean
    public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
        return new DefaultPrincipalAttributesRepositoryCache();
    }
}
