package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.MongoDbThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CasMongoDbThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "casMongoDbThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasMongoDbThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("throttledRequestResponseHandler")
    private ObjectProvider<ThrottledRequestResponseHandler> throttledRequestResponseHandler;

    @Autowired
    @Qualifier("throttledRequestExecutor")
    private ObjectProvider<ThrottledRequestExecutor> throttledRequestExecutor;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Autowired
    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        @Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailExecutionPlan) {
        val throttle = casProperties.getAuthn().getThrottle();
        val failure = throttle.getFailure();

        val mongo = casProperties.getAudit().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());

        val context = ThrottledSubmissionHandlerConfigurationContext.builder()
            .failureThreshold(failure.getThreshold())
            .failureRangeInSeconds(failure.getRangeSeconds())
            .usernameParameter(throttle.getUsernameParameter())
            .authenticationFailureCode(failure.getCode())
            .auditTrailExecutionPlan(auditTrailExecutionPlan)
            .applicationCode(throttle.getAppCode())
            .throttledRequestResponseHandler(throttledRequestResponseHandler.getObject())
            .throttledRequestExecutor(throttledRequestExecutor.getObject())
            .build();

        return new MongoDbThrottledSubmissionHandlerInterceptorAdapter(context, mongoTemplate, mongo.getCollection());
    }
}
