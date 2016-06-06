package org.apereo.cas.web.support.config;

import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.NeverThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.sql.DataSource;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThrottlingConfiguration")
@EnableConfigurationProperties(ThrottleProperties.class)
public class CasThrottlingConfiguration {

    @Autowired
    ThrottleProperties throttleProperties;

    @Autowired
    @Qualifier("auditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired(required=false)
    @Qualifier("inspektrAuditTrailDataSource")
    private DataSource dataSource;

    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter inMemoryIpAddressUsernameThrottle() {
        return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter();
    }

    @Bean
    public HandlerInterceptorAdapter inMemoryIpAddressThrottle() {
        return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter();
    }

    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter inspektrIpAddressUsernameThrottle() {
        final InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter bean =
                new InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(this.auditTrailManager, this.dataSource);
        bean.setApplicationCode(this.throttleProperties.getAppcode());
        bean.setAuthenticationFailureCode(this.throttleProperties.getFailure().getCode());
        bean.setUsernameParameter(this.throttleProperties.getUsernameParameter());
        bean.setFailureThreshold(this.throttleProperties.getFailure().getThreshold());
        bean.setFailureRangeInSeconds(this.throttleProperties.getFailure().getRangeSeconds());
        bean.setSqlQueryAudit(this.throttleProperties.getAuditQuery());
        return bean;
    }

    @Bean
    public HandlerInterceptorAdapter neverThrottle() {
        return new NeverThrottledSubmissionHandlerInterceptorAdapter();
    }
}
