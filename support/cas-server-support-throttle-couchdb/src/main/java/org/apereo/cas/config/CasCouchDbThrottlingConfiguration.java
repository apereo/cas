package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.CouchDbThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCouchDbThrottlingConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "casCouchDbThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCouchDbThrottlingConfiguration {

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("auditActionContextCouchDbRepository")
    private ObjectProvider<AuditActionContextCouchDbRepository> couchDbRepository;

    @Autowired
    @Qualifier("throttledRequestExecutor")
    private ObjectProvider<ThrottledRequestExecutor> throttledRequestExecutor;

    @Autowired
    @Qualifier("throttledRequestResponseHandler")
    private ObjectProvider<ThrottledRequestResponseHandler> throttledRequestResponseHandler;

    @ConditionalOnMissingBean(name = "couchDbAuthenticationThrottle")
    @Bean
    @RefreshScope
    public CouchDbThrottledSubmissionHandlerInterceptorAdapter authenticationThrottle() {
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

        return new CouchDbThrottledSubmissionHandlerInterceptorAdapter(context, couchDbRepository.getObject());
    }
}
