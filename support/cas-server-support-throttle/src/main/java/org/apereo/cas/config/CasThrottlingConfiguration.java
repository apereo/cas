package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlanConfigurer;
import org.apereo.cas.throttle.ConcurrentThrottledSubmissionsStore;
import org.apereo.cas.throttle.DefaultAuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.DefaultThrottledRequestResponseHandler;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestFilter;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerEndpoint;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Throttling)
@AutoConfiguration
public class CasThrottlingConfiguration {

    @Configuration(value = "CasThrottlingInterceptorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingInterceptorConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
        @Bean
        public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
            final CasConfigurationProperties casProperties,
            @Qualifier("authenticationThrottlingConfigurationContext")
            final ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext) {
            val throttle = casProperties.getAuthn().getThrottle();
            if (throttle.getFailure().getRangeSeconds() <= 0 && throttle.getFailure().getThreshold() <= 0) {
                LOGGER.trace("Authentication throttling is disabled since no range-seconds or failure-threshold is defined");
                return ThrottledSubmissionHandlerInterceptor.noOp();
            }
            if (StringUtils.isNotBlank(throttle.getCore().getUsernameParameter())) {
                LOGGER.trace("Activating authentication throttling based on IP address and username...");
                return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(
                    authenticationThrottlingConfigurationContext);
            }
            LOGGER.trace("Activating authentication throttling based on IP address...");
            return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(
                authenticationThrottlingConfigurationContext);
        }
    }

    @Configuration(value = "CasThrottlingContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingContextConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "authenticationThrottlingConfigurationContext")
        public ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuditTrailExecutionPlan.BEAN_NAME)
            final AuditTrailExecutionPlan auditTrailExecutionPlan,
            final CasConfigurationProperties casProperties,
            @Qualifier(ThrottledRequestResponseHandler.BEAN_NAME)
            final ThrottledRequestResponseHandler throttledRequestResponseHandler,
            @Qualifier(ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
            final ThrottledRequestExecutor throttledRequestExecutor,
            @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
            final ThrottledSubmissionsStore throttledSubmissionStore) {
            return ThrottledSubmissionHandlerConfigurationContext.builder()
                .casProperties(casProperties)
                .throttledSubmissionStore(throttledSubmissionStore)
                .auditTrailExecutionPlan(auditTrailExecutionPlan)
                .throttledRequestResponseHandler(throttledRequestResponseHandler)
                .throttledRequestExecutor(throttledRequestExecutor)
                .applicationContext(applicationContext)
                .build();
        }
    }

    @Configuration(value = "CasThrottlingCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingCoreConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "throttledRequestResponseHandler")
        public ThrottledRequestResponseHandler throttledRequestResponseHandler(final CasConfigurationProperties casProperties) {
            val throttle = casProperties.getAuthn().getThrottle();
            return new DefaultThrottledRequestResponseHandler(throttle.getCore().getUsernameParameter());
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
        public ThrottledRequestExecutor throttledRequestExecutor() {
            return ThrottledRequestExecutor.noOp();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = ThrottledSubmissionsStore.BEAN_NAME)
        @Bean
        public ThrottledSubmissionsStore throttleSubmissionMap(final CasConfigurationProperties casProperties) {
            return new ConcurrentThrottledSubmissionsStore(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "httpPostMethodThrottlingRequestFilter")
        public ThrottledRequestFilter httpPostMethodThrottlingRequestFilter() {
            return ThrottledRequestFilter.httpPost();
        }

    }

    @Configuration(value = "CasThrottlingPlanExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingPlanExecutionConfiguration {
        @ConditionalOnMissingBean(name = AuthenticationThrottlingExecutionPlan.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationThrottlingExecutionPlan authenticationThrottlingExecutionPlan(
            final List<AuthenticationThrottlingExecutionPlanConfigurer> configurers) {
            val plan = new DefaultAuthenticationThrottlingExecutionPlan();
            configurers.forEach(c -> {
                LOGGER.trace("Registering authentication throttler [{}]", c.getName());
                c.configureAuthenticationThrottlingExecutionPlan(plan);
            });
            return plan;
        }
    }

    @Configuration(value = "CasThrottlingPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingPlanConfiguration {
        @ConditionalOnMissingBean(name = "authenticationThrottlingExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationThrottlingExecutionPlanConfigurer authenticationThrottlingExecutionPlanConfigurer(
            @Qualifier("httpPostMethodThrottlingRequestFilter")
            final ThrottledRequestFilter httpPostMethodThrottlingRequestFilter,
            @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
            final ThrottledSubmissionHandlerInterceptor authenticationThrottle) {
            return plan -> {
                plan.registerAuthenticationThrottleFilter(httpPostMethodThrottlingRequestFilter);
                plan.registerAuthenticationThrottleInterceptor(authenticationThrottle);
            };
        }
    }

    @Configuration(value = "CasThrottlingWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ThrottledSubmissionHandlerEndpoint throttledSubmissionHandlerEndpoint(
            @Qualifier(AuthenticationThrottlingExecutionPlan.BEAN_NAME)
            final ObjectProvider<AuthenticationThrottlingExecutionPlan> plan, final CasConfigurationProperties casProperties) {
            return new ThrottledSubmissionHandlerEndpoint(casProperties, plan);
        }
    }

    @Configuration(value = "CasThrottlingSchedulerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasThrottlingSchedulerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Runnable throttleSubmissionCleaner(
            @Qualifier(AuthenticationThrottlingExecutionPlan.BEAN_NAME)
            final AuthenticationThrottlingExecutionPlan plan) {
            return new InMemoryThrottledSubmissionCleaner(plan);
        }
    }
}
