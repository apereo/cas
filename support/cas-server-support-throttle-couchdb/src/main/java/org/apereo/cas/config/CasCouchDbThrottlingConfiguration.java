package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.CouchDbThrottledSubmissionHandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;
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
@Configuration("casCouchDbThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCouchDbThrottlingConfiguration {

    @Autowired
    @Qualifier("auditTrailExecutionPlan")
    private ObjectProvider<AuditTrailExecutionPlan> auditTrailManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("auditActionContextCouchDbRepository")
    private AuditActionContextCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("throttledRequestResponseHandler")
    private ThrottledRequestResponseHandler throttledRequestResponseHandler;

    @ConditionalOnMissingBean(name = "couchDbAuthenticationThrottle")
    @Bean
    @RefreshScope
    public CouchDbThrottledSubmissionHandlerInterceptorAdapter authenticationThrottle() {
        val throttle = casProperties.getAuthn().getThrottle();
        val failure = throttle.getFailure();
        return new CouchDbThrottledSubmissionHandlerInterceptorAdapter(failure.getThreshold(),
            failure.getRangeSeconds(),
            throttle.getUsernameParameter(),
            failure.getCode(),
            auditTrailManager.getIfAvailable(),
            throttle.getAppcode(),
            couchDbRepository,
            throttledRequestResponseHandler);
    }
}
