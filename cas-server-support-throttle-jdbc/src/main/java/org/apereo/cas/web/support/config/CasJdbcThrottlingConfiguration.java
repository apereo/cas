package org.apereo.cas.web.support.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;
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
        return Beans.newHickariDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }

    private AbstractThrottledSubmissionHandlerInterceptorAdapter
    configureThrottleHandlerInterceptorAdaptor(final AbstractThrottledSubmissionHandlerInterceptorAdapter interceptorAdapter) {
        interceptorAdapter.setUsernameParameter(casProperties.getAuthn().getThrottle().getUsernameParameter());
        interceptorAdapter.setFailureThreshold(casProperties.getAuthn().getThrottle().getFailure().getThreshold());
        interceptorAdapter.setFailureRangeInSeconds(casProperties.getAuthn().getThrottle().getFailure().getRangeSeconds());
        return interceptorAdapter;
    }

    @Autowired
    @Bean(name = {"inspektrIpAddressUsernameThrottle", "authenticationThrottle"})
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor inspektrIpAddressUsernameThrottle(@Qualifier("auditTrailManager")
                                                                                   final AuditTrailManager auditTrailManager) {
        final InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter bean =
                new InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(auditTrailManager,
                        inspektrAuditTrailDataSource());
        bean.setApplicationCode(casProperties.getAuthn().getThrottle().getAppcode());
        bean.setAuthenticationFailureCode(casProperties.getAuthn().getThrottle().getFailure().getCode());
        bean.setSqlQueryAudit(casProperties.getAuthn().getThrottle().getJdbc().getAuditQuery());
        return configureThrottleHandlerInterceptorAdaptor(bean);
    }

}
