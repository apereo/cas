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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link GroovyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("groovyAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class GroovyAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "groovyPrincipalFactory")
    @Bean
    public PrincipalFactory groovyPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler groovyResourceAuthenticationHandler() {
        val groovy = casProperties.getAuthn().getGroovy();
        return new GroovyAuthenticationHandler(groovy.getName(),
            servicesManager.getObject(), groovyPrincipalFactory(),
            groovy.getLocation(), groovy.getOrder());
    }

    @ConditionalOnMissingBean(name = "groovyResourceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer groovyResourceAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val file = casProperties.getAuthn().getGroovy().getLocation();
            if (file != null) {
                LOGGER.debug("Activating Groovy authentication handler via [{}]", file);
                plan.registerAuthenticationHandlerWithPrincipalResolver(groovyResourceAuthenticationHandler(),
                    defaultPrincipalResolver.getObject());
            }
        };
    }
}
