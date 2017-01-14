package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CasCoreAuthenticationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreAuthenticationConfiguration.class);
    
    private Set<AuthenticationEventExecutionPlanConfigurer> configurers = new LinkedHashSet<>();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport(@Qualifier("principalElectionStrategy")
                                                                          final PrincipalElectionStrategy principalElectionStrategy,
                                                                          @Qualifier("authenticationManager")
                                                                          final AuthenticationManager authenticationManager) {
        return new DefaultAuthenticationSystemSupport(
                defaultAuthenticationTransactionManager(authenticationManager), principalElectionStrategy);
    }

    @Bean(name = {"defaultAuthenticationTransactionManager", "authenticationTransactionManager"})
    public AuthenticationTransactionManager defaultAuthenticationTransactionManager(@Qualifier("authenticationManager")
                                                                                    final AuthenticationManager authenticationManager) {
        return new DefaultAuthenticationTransactionManager(authenticationManager);
    }

    @ConditionalOnMissingBean(name = "authenticationManager")
    @Autowired
    @Bean
    public AuthenticationManager authenticationManager(@Qualifier("authenticationPolicy")
                                                       final AuthenticationPolicy authenticationPolicy,
                                                       @Qualifier("authenticationMetadataPopulators")
                                                       final List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators,
                                                       @Qualifier("registeredServiceAuthenticationHandlerResolver")
                                                       final AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver,
                                                       @Qualifier("authenticationEventExecutionPlan")
                                                       final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
        return new PolicyBasedAuthenticationManager(
                authenticationEventExecutionPlan,
                registeredServiceAuthenticationHandlerResolver,
                authenticationMetadataPopulators,
                authenticationPolicy,
                casProperties.getPersonDirectory().isPrincipalResolutionFailureFatal()
        );
    }

    @Autowired(required = false)
    public void setConfigurers(final List<AuthenticationEventExecutionPlanConfigurer> cfg) {
        if (cfg != null) {
            LOGGER.debug("Found {} authentication event execution plans", cfg.size());
            this.configurers.addAll(cfg);
        }
    }

    @ConditionalOnMissingBean(name = "authenticationEventExecutionPlan")
    @Bean
    public AuthenticationEventExecutionPlan authenticationEventExecutionPlan() {
        final DefaultAuthenticationEventExecutionPlan plan = new DefaultAuthenticationEventExecutionPlan();
        configurers.forEach(c -> {
            LOGGER.debug("Configuring authentication execution plan via {}", c);
            c.configureAuthenticationExecutionPlan(plan);
        });
        return plan;
    }
}
