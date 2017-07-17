package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreAuthenticationConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public AuthenticationTransactionManager authenticationTransactionManager(@Qualifier("casAuthenticationManager")
                                                                             final AuthenticationManager authenticationManager) {
        return new DefaultAuthenticationTransactionManager(authenticationManager);
    }

    @ConditionalOnMissingBean(name = "casAuthenticationManager")
    @Autowired
    @Bean
    public AuthenticationManager casAuthenticationManager(@Qualifier("authenticationPolicy")
                                                          final Collection<AuthenticationPolicy> authenticationPolicy,
                                                          @Qualifier("registeredServiceAuthenticationHandlerResolver")
                                                          final AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver,
                                                          @Qualifier("authenticationEventExecutionPlan")
                                                          final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
        return new PolicyBasedAuthenticationManager(
                authenticationEventExecutionPlan,
                registeredServiceAuthenticationHandlerResolver,
                authenticationPolicy,
                casProperties.getPersonDirectory().isPrincipalResolutionFailureFatal()
        );
    }

    @ConditionalOnMissingBean(name = "authenticationEventExecutionPlan")
    @Autowired
    @Bean
    public AuthenticationEventExecutionPlan authenticationEventExecutionPlan(final List<AuthenticationEventExecutionPlanConfigurer> configurers) {
        final DefaultAuthenticationEventExecutionPlan plan = new DefaultAuthenticationEventExecutionPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring authentication execution plan [{}]", name);
            c.configureAuthenticationExecutionPlan(plan);
        });
        return plan;
    }
}
