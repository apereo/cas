package org.apereo.cas.web.support.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.web.support.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.web.support.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.web.support.DefaultAuthenticationThrottlingExecutionPlan;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.NoOpThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    @ConditionalOnMissingBean(name = "throttleSubmissionMap")
    @Bean
    @Lazy
    public ConcurrentMap throttleSubmissionMap() {
        return new ConcurrentHashMap<String, ZonedDateTime>();
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottle")
    @Bean
    @Lazy
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        final ThrottleProperties throttle = casProperties.getAuthn().getThrottle();

        if (throttle.getFailure().getRangeSeconds() <= 0 && throttle.getFailure().getThreshold() <= 0) {
            LOGGER.debug("Authentication throttling is disabled since no range-seconds or failure-threshold is defined");
            return new NoOpThrottledSubmissionHandlerInterceptor();
        }

        if (StringUtils.isNotBlank(throttle.getUsernameParameter())) {
            LOGGER.debug("Activating authentication throttling based on IP address and username...");
            return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(throttle.getFailure().getThreshold(),
                throttle.getFailure().getRangeSeconds(),
                throttle.getUsernameParameter(),
                throttle.getFailure().getCode(),
                auditTrailExecutionPlan.getIfAvailable(),
                throttle.getAppcode(),
                throttleSubmissionMap());
        }
        LOGGER.debug("Activating authentication throttling based on IP address...");
        return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(throttle.getFailure().getThreshold(),
            throttle.getFailure().getRangeSeconds(),
            throttle.getUsernameParameter(),
            throttle.getFailure().getCode(),
            auditTrailExecutionPlan.getIfAvailable(),
            throttle.getAppcode(),
            throttleSubmissionMap());
    }

    @Autowired
    @ConditionalOnMissingBean(name = "authenticationThrottlingExecutionPlan")
    @Bean
    public AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan(final List<AuthenticationThrottlingExecutionPlanConfigurer> configurers) {
        final DefaultAuthenticationThrottlingExecutionPlan plan = new DefaultAuthenticationThrottlingExecutionPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Registering authentication throttler [{}]", name);
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
