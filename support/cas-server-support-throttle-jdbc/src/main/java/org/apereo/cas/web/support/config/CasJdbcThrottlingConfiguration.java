package org.apereo.cas.web.support.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.web.support.InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.apereo.inspektr.audit.AuditTrailManager;
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
    private CasConfigurationProperties casProperties;

    @Bean
    public DataSource inspektrAuditTrailDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }

    @Autowired
    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(@Qualifier("auditTrailManager") final AuditTrailManager auditTrailManager) {
        final ThrottleProperties throttle = casProperties.getAuthn().getThrottle();
        final String appcode = throttle.getAppcode();
        final String sqlQueryAudit = throttle.getJdbc().getAuditQuery();
        final ThrottleProperties.Failure failure = throttle.getFailure();
        return new InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(failure.getThreshold(), failure.getRangeSeconds(),
                throttle.getUsernameParameter(), auditTrailManager, inspektrAuditTrailDataSource(), appcode, sqlQueryAudit, failure.getCode());
    }
}
