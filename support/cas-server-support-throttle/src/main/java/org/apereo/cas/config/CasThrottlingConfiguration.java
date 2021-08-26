package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.throttle.DefaultAuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.DefaultThrottledRequestResponseHandler;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerEndpoint;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.ZonedDateTime;
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
    private ConfigurableApplicationContext applicationContext;

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
        return new DefaultThrottledRequestResponseHandler(throttle.getCore().getUsernameParameter());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
    public ThrottledRequestExecutor throttledRequestExecutor() {
        return ThrottledRequestExecutor.noOp();
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "throttleSubmissionMap")
    @Bean
    public ConcurrentMap throttleSubmissionMap() {
        return new ConcurrentHashMap<String, ZonedDateTime>();
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottle")
    @Bean
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        val throttle = casProperties.getAuthn().getThrottle();

        if (throttle.getFailure().getRangeSeconds() <= 0 && throttle.getFailure().getThreshold() <= 0) {
            LOGGER.trace("Authentication throttling is disabled since no range-seconds or failure-threshold is defined");
            return ThrottledSubmissionHandlerInterceptor.noOp();
        }

        if (StringUtils.isNotBlank(throttle.getCore().getUsernameParameter())) {
            LOGGER.trace("Activating authentication throttling based on IP address and username...");
            return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(
                authenticationThrottlingConfigurationContext(), throttleSubmissionMap());
        }
        LOGGER.trace("Activating authentication throttling based on IP address...");
        return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(
            authenticationThrottlingConfigurationContext(), throttleSubmissionMap());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottlingConfigurationContext")
    public ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext() {
        val throttle = casProperties.getAuthn().getThrottle();
        return ThrottledSubmissionHandlerConfigurationContext.builder()
            .failureThreshold(throttle.getFailure().getThreshold())
            .failureRangeInSeconds(throttle.getFailure().getRangeSeconds())
            .usernameParameter(throttle.getCore().getUsernameParameter())
            .authenticationFailureCode(throttle.getFailure().getCode())
            .auditTrailExecutionPlan(auditTrailExecutionPlan.getObject())
            .applicationCode(throttle.getCore().getAppCode())
            .throttledRequestResponseHandler(throttledRequestResponseHandler())
            .throttledRequestExecutor(throttledRequestExecutor())
            .authenticationThrottlingExecutionPlan(authenticationThrottlingExecutionPlan())
            .build();
    }

    @ConditionalOnMissingBean(name = "authenticationThrottlingExecutionPlan")
    @Bean
    public AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan() {
        val configurers = applicationContext.getBeansOfType(AuthenticationThrottlingExecutionPlanConfigurer.class, false, true).values();
        val plan = new DefaultAuthenticationThrottlingExecutionPlan();
        configurers.forEach(c -> {
            LOGGER.trace("Registering authentication throttler [{}]", c.getName());
            c.configureAuthenticationThrottlingExecutionPlan(plan);
        });
        return plan;
    }

    @Bean
    @Autowired
    public Runnable throttleSubmissionCleaner(
        @Qualifier("authenticationThrottlingExecutionPlan") final AuthenticationThrottlingExecutionPlan plan) {
        return new InMemoryThrottledSubmissionCleaner(plan);
    }

    @Bean
    @ConditionalOnMissingBean(name = "httpPostMethodThrottlingRequestFilter")
    public ThrottledRequestFilter httpPostMethodThrottlingRequestFilter() {
        return ThrottledRequestFilter.httpPost();
    }

    @ConditionalOnMissingBean(name = "authenticationThrottlingExecutionPlanConfigurer")
    @Bean
    @Order(0)
    public AuthenticationThrottlingExecutionPlanConfigurer authenticationThrottlingExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationThrottleFilter(httpPostMethodThrottlingRequestFilter());
            plan.registerAuthenticationThrottleInterceptor(authenticationThrottle());
        };
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public ThrottledSubmissionHandlerEndpoint throttledSubmissionHandlerEndpoint(
        @Qualifier("authenticationThrottlingExecutionPlan") final AuthenticationThrottlingExecutionPlan plan) {
        return new ThrottledSubmissionHandlerEndpoint(casProperties, plan);
    }
}
