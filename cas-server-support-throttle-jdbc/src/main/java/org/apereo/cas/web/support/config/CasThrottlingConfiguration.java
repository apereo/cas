package org.apereo.cas.web.support.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CasThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("auditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired(required = false)
    @Qualifier("inspektrAuditTrailDataSource")
    private DataSource dataSource;

    private AbstractThrottledSubmissionHandlerInterceptorAdapter
    configureThrottleHandlerInterceptorAdaptor(final AbstractThrottledSubmissionHandlerInterceptorAdapter interceptorAdapter) {
        interceptorAdapter.setUsernameParameter(casProperties.getAuthn().getThrottle().getUsernameParameter());
        interceptorAdapter.setFailureThreshold(casProperties.getAuthn().getThrottle().getFailure().getThreshold());
        interceptorAdapter.setFailureRangeInSeconds(casProperties.getAuthn().getThrottle().getFailure().getRangeSeconds());
        return interceptorAdapter;
    }

    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter inspektrIpAddressUsernameThrottle() {
        final InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter bean =
                new InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(this.auditTrailManager, this.dataSource);
        bean.setApplicationCode(casProperties.getAuthn().getThrottle().getAppcode());
        bean.setAuthenticationFailureCode(casProperties.getAuthn().getThrottle().getFailure().getCode());
        bean.setSqlQueryAudit(casProperties.getAuthn().getThrottle().getAuditQuery());
        return configureThrottleHandlerInterceptorAdaptor(bean);
    }

}
