package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationAttributeReleaseProperties;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
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
@Slf4j
public class CasCoreAuthenticationConfiguration {


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

    @ConditionalOnMissingBean(name = "authenticationAttributeReleasePolicy")
    @RefreshScope
    @Bean
    public AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy() {
        final AuthenticationAttributeReleaseProperties authenticationAttributeRelease =
            casProperties.getAuthn().getAuthenticationAttributeRelease();
        final DefaultAuthenticationAttributeReleasePolicy policy = new DefaultAuthenticationAttributeReleasePolicy();
        policy.setAttributesToRelease(authenticationAttributeRelease.getOnlyRelease());
        final Set<String> attributesToNeverRelease = CollectionUtils.wrapSet(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
            RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        attributesToNeverRelease.addAll(authenticationAttributeRelease.getNeverRelease());
        policy.setAttributesToNeverRelease(attributesToNeverRelease);
        return policy;
    }
}
