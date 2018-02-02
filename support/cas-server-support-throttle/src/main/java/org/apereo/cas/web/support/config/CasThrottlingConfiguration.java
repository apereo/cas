package org.apereo.cas.web.support.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
@Conditional(AuthenticationThrottlingCondition.class)
public class CasThrottlingConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottle")
    @Bean
    @Autowired
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(@Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailExecutionPlan) {
        final ThrottleProperties throttle = casProperties.getAuthn().getThrottle();
        if (StringUtils.isNotBlank(throttle.getUsernameParameter())) {
            LOGGER.debug("Activating authentication throttling based on IP address and username...");
            return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(throttle.getFailure().getThreshold(),
                throttle.getFailure().getRangeSeconds(),
                throttle.getUsernameParameter(),
                throttle.getFailure().getCode(),
                auditTrailExecutionPlan,
                throttle.getAppcode());
        }
        LOGGER.debug("Activating authentication throttling based on IP address...");
        return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(throttle.getFailure().getThreshold(),
            throttle.getFailure().getRangeSeconds(),
            throttle.getUsernameParameter(),
            throttle.getFailure().getCode(),
            auditTrailExecutionPlan,
            throttle.getAppcode());
    }

    @Lazy
    @Bean
    @Conditional(AuthenticationThrottlingCondition.class)
    public Runnable throttleSubmissionCleaner(@Qualifier("authenticationThrottle") final ThrottledSubmissionHandlerInterceptor adapter) {
        return new InMemoryThrottledSubmissionCleaner(adapter);
    }

}
