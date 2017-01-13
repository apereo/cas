package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                                                       @Qualifier("authenticationHandlersResolvers")
                                                       final Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers) {
        return new PolicyBasedAuthenticationManager(
                authenticationHandlersResolvers,
                registeredServiceAuthenticationHandlerResolver,
                authenticationMetadataPopulators,
                authenticationPolicy,
                casProperties.getPersonDirectory().isPrincipalResolutionFailureFatal()
        );
    }

    @ConditionalOnMissingBean(name = "authenticationHandlersResolvers")
    @Bean
    public Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers() {
        return new TreeMap<>(OrderComparator.INSTANCE);
    }
}
