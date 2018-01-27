package org.apereo.cas.web.support.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.audit.AuditMongoDbProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.web.support.MongoDbThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link CasJdbcThrottlingConfiguration}.
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
        final ThrottleProperties throttle = casProperties.getAuthn().getThrottle();
        final ThrottleProperties.Failure failure = throttle.getFailure();

        final AuditMongoDbProperties mongo = casProperties.getAudit().getMongo();
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongo);
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
