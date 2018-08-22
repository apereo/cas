package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CasJdbcThrottlingConfiguration {

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("throttledRequestResponseHandler")
    private ThrottledRequestResponseHandler throttledRequestResponseHandler;

    @Bean
    public DataSource inspektrThrottleDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }


    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        val throttle = casProperties.getAuthn().getThrottle();
        val failure = throttle.getFailure();
        return new JdbcThrottledSubmissionHandlerInterceptorAdapter(
            failure.getThreshold(),
            failure.getRangeSeconds(),
            throttle.getUsernameParameter(),
            auditTrailManager.getIfAvailable(),
            inspektrThrottleDataSource(),
            throttle.getAppcode(),
            throttle.getJdbc().getAuditQuery(),
            failure.getCode(),
            throttledRequestResponseHandler);
    }
}
