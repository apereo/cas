package org.apereo.cas.web.support.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.web.support.MongoDbThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasMongoDbThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casMongoDbThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasMongoDbThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(@Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailExecutionPlan) {
        final var throttle = casProperties.getAuthn().getThrottle();
        final var failure = throttle.getFailure();

        final var mongo = casProperties.getAudit().getMongo();
        final var factory = new MongoDbConnectionFactory();
        final var mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());

        return new MongoDbThrottledSubmissionHandlerInterceptorAdapter(failure.getThreshold(),
            failure.getRangeSeconds(),
            throttle.getUsernameParameter(),
            auditTrailExecutionPlan,
            mongoTemplate,
            failure.getCode(),
            throttle.getAppcode(),
            mongo.getCollection());
    }
}
