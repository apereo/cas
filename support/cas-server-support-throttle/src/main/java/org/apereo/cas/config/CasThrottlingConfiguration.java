package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.throttle.DefaultAuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.DefaultThrottledRequestResponseHandler;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.NoOpThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreUtilConfiguration.class)
@Slf4j
public class CasThrottlingConfiguration {

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailExecutionPlan;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "throttledRequestResponseHandler")
    public ThrottledRequestResponseHandler throttledRequestResponseHandler() {
        val throttle = casProperties.getAuthn().getThrottle();
        return new DefaultThrottledRequestResponseHandler(throttle.getUsernameParameter());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottle")
    @Bean
    @Lazy
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        val throttle = casProperties.getAuthn().getThrottle();

        if (throttle.getFailure().getRangeSeconds() <= 0 && throttle.getFailure().getThreshold() <= 0) {
            LOGGER.debug("Authentication throttling is disabled since no range-seconds or failure-threshold is defined");
            return new NoOpThrottledSubmissionHandlerInterceptor();
        }

        if (StringUtils.isNotBlank(throttle.getUsernameParameter())) {
            LOGGER.debug("Activating authentication throttling based on IP address and username...");
            return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(
                throttle.getFailure().getThreshold(),
                throttle.getFailure().getRangeSeconds(),
                throttle.getUsernameParameter(),
                throttle.getFailure().getCode(),
                auditTrailExecutionPlan.getIfAvailable(),
                throttle.getAppcode(),
                throttledRequestResponseHandler());
        }
        LOGGER.debug("Activating authentication throttling based on IP address...");
        return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(
            throttle.getFailure().getThreshold(),
            throttle.getFailure().getRangeSeconds(),
            throttle.getUsernameParameter(),
            throttle.getFailure().getCode(),
            auditTrailExecutionPlan.getIfAvailable(),
            throttle.getAppcode(),
            throttledRequestResponseHandler());
    }

    @Autowired
    @ConditionalOnMissingBean(name = "authenticationThrottlingExecutionPlan")
    @Bean
    public AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan(final List<AuthenticationThrottlingExecutionPlanConfigurer> configurers) {
        val plan = new DefaultAuthenticationThrottlingExecutionPlan();
        configurers.forEach(c -> {
            LOGGER.debug("Registering authentication throttler [{}]", c.getName());
            c.configureAuthenticationThrottlingExecutionPlan(plan);
        });
        return plan;
    }

    @Lazy
    @Bean
    @Autowired
    public Runnable throttleSubmissionCleaner(@Qualifier("authenticationThrottlingExecutionPlan") final AuthenticationThrottlingExecutionPlan plan) {
        return new InMemoryThrottledSubmissionCleaner(plan);
    }

    @ConditionalOnMissingBean(name = "authenticationThrottlingExecutionPlanConfigurer")
    @Bean
    @Order(0)
    public AuthenticationThrottlingExecutionPlanConfigurer authenticationThrottlingExecutionPlanConfigurer() {
        return new AuthenticationThrottlingExecutionPlanConfigurer() {
            @Override
            public void configureAuthenticationThrottlingExecutionPlan(final AuthenticationThrottlingExecutionPlan plan) {
                plan.registerAuthenticationThrottleInterceptor(authenticationThrottle());
            }
        };
    }
}
