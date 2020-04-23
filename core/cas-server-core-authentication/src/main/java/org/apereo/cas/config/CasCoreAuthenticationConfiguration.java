package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration(value = "casCoreAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @Bean
    @RefreshScope
    public AuthenticationTransactionManager authenticationTransactionManager() {
        return new DefaultAuthenticationTransactionManager(applicationContext, casAuthenticationManager());
    }

    @ConditionalOnMissingBean(name = "casAuthenticationManager")
    @Bean
    @RefreshScope
    public AuthenticationManager casAuthenticationManager() {
        return new PolicyBasedAuthenticationManager(
            authenticationEventExecutionPlan.getObject(),
            casProperties.getPersonDirectory().isPrincipalResolutionFailureFatal(),
            applicationContext
        );
    }

    @ConditionalOnMissingBean(name = "authenticationEventExecutionPlan")
    @Autowired
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlan authenticationEventExecutionPlan(final List<AuthenticationEventExecutionPlanConfigurer> configurers) {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        val sortedConfigurers = new ArrayList<AuthenticationEventExecutionPlanConfigurer>(configurers);
        AnnotationAwareOrderComparator.sortIfNecessary(sortedConfigurers);

        sortedConfigurers.forEach(c -> {
            LOGGER.trace("Configuring authentication execution plan [{}]", c.getName());
            c.configureAuthenticationExecutionPlan(plan);
        });
        return plan;
    }

    @ConditionalOnMissingBean(name = "authenticationAttributeReleasePolicy")
    @RefreshScope
    @Bean
    public AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy() {
        val release = casProperties.getAuthn().getAuthenticationAttributeRelease();
        if (!release.isEnabled()) {
            LOGGER.debug("CAS is configured to not release protocol-level authentication attributes.");
            return AuthenticationAttributeReleasePolicy.noOp();
        }
        return new DefaultAuthenticationAttributeReleasePolicy(release.getOnlyRelease(),
            release.getNeverRelease(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
    }
}
