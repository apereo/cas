package org.apereo.cas.config;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
@Slf4j
public class CasCoreAuthenticationConfiguration {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public AuthenticationTransactionManager authenticationTransactionManager(@Qualifier("casAuthenticationManager") final AuthenticationManager authenticationManager) {
        return new DefaultAuthenticationTransactionManager(applicationEventPublisher, authenticationManager);
    }

    @ConditionalOnMissingBean(name = "casAuthenticationManager")
    @Autowired
    @Bean
    public AuthenticationManager casAuthenticationManager(@Qualifier("authenticationEventExecutionPlan") final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
        return new PolicyBasedAuthenticationManager(
            authenticationEventExecutionPlan,
            casProperties.getPersonDirectory().isPrincipalResolutionFailureFatal(),
            applicationEventPublisher
        );
    }

    @ConditionalOnMissingBean(name = "authenticationEventExecutionPlan")
    @Autowired
    @Bean
    public AuthenticationEventExecutionPlan authenticationEventExecutionPlan(final List<AuthenticationEventExecutionPlanConfigurer> configurers) {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        configurers.forEach(c -> {
            val name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring authentication execution plan [{}]", name);
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

        val attributesToNeverRelease = CollectionUtils.wrapSet(
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET,
            RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        attributesToNeverRelease.addAll(release.getNeverRelease());

        return new DefaultAuthenticationAttributeReleasePolicy(release.getOnlyRelease(), attributesToNeverRelease,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
    }
}
