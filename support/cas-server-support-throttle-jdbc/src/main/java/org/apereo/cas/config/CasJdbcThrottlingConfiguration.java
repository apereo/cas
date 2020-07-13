package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasThrottlingConfiguration.class)
public class CasJdbcThrottlingConfiguration {

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("throttledRequestResponseHandler")
    private ObjectProvider<ThrottledRequestResponseHandler> throttledRequestResponseHandler;

    @Autowired
    @Qualifier("throttledRequestExecutor")
    private ObjectProvider<ThrottledRequestExecutor> throttledRequestExecutor;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "inspektrThrottleDataSource")
    public DataSource inspektrThrottleDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }

    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        val throttle = casProperties.getAuthn().getThrottle();
        val failure = throttle.getFailure();

        val context = ThrottledSubmissionHandlerConfigurationContext.builder()
            .failureThreshold(failure.getThreshold())
            .failureRangeInSeconds(failure.getRangeSeconds())
            .usernameParameter(throttle.getUsernameParameter())
            .authenticationFailureCode(failure.getCode())
            .auditTrailExecutionPlan(auditTrailManager.getObject())
            .applicationCode(throttle.getAppCode())
            .throttledRequestResponseHandler(throttledRequestResponseHandler.getObject())
            .throttledRequestExecutor(throttledRequestExecutor.getObject())
            .build();

        return new JdbcThrottledSubmissionHandlerInterceptorAdapter(context, inspektrThrottleDataSource(),
            throttle.getJdbc().getAuditQuery());
    }
}
