package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.GroovyAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link GroovyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "groovyAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class GroovyAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "groovyPrincipalFactory")
    @Bean
    public PrincipalFactory groovyPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AuthenticationHandler groovyResourceAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                                     @Qualifier("groovyPrincipalFactory")
                                                                     final PrincipalFactory groovyPrincipalFactory,
                                                                     @Qualifier(ServicesManager.BEAN_NAME)
                                                                     final ServicesManager servicesManager) {
        val groovy = casProperties.getAuthn().getGroovy();
        val handler = new GroovyAuthenticationHandler(groovy.getName(), servicesManager, groovyPrincipalFactory, groovy.getLocation(), groovy.getOrder());
        handler.setState(groovy.getState());
        return handler;
    }

    @ConditionalOnMissingBean(name = "groovyResourceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer groovyResourceAuthenticationEventExecutionPlanConfigurer(final CasConfigurationProperties casProperties,
                                                                                                               @Qualifier("groovyResourceAuthenticationHandler")
                                                                                                               final AuthenticationHandler groovyResourceAuthenticationHandler,
                                                                                                               @Qualifier("defaultPrincipalResolver")
                                                                                                               final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val file = casProperties.getAuthn().getGroovy().getLocation();
            if (file != null) {
                LOGGER.debug("Activating Groovy authentication handler via [{}]", file);
                plan.registerAuthenticationHandlerWithPrincipalResolver(groovyResourceAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }
}
