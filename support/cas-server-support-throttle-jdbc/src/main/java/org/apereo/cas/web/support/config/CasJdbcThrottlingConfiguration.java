package org.apereo.cas.web.support.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
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
@Slf4j
public class CasJdbcThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public DataSource inspektrAuditTrailDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }

    @Autowired
    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(@Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailManager) {
        final ThrottleProperties throttle = casProperties.getAuthn().getThrottle();
        final ThrottleProperties.Failure failure = throttle.getFailure();
        return new JdbcThrottledSubmissionHandlerInterceptorAdapter(failure.getThreshold(),
            failure.getRangeSeconds(),
            throttle.getUsernameParameter(),
            auditTrailManager,
            inspektrAuditTrailDataSource(),
            throttle.getAppcode(),
            throttle.getJdbc().getAuditQuery(),
            failure.getCode());
    }
}
