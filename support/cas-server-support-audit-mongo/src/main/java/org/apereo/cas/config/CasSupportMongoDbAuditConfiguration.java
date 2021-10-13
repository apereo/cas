package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.MongoDbAuditTrailManager;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportMongoDbAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casSupportMongoDbAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportMongoDbAuditConfiguration {

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "mongoDbAuditTrailManager")
    public AuditTrailManager mongoDbAuditTrailManager(final CasConfigurationProperties casProperties,
                                                      @Qualifier("casSslContext")
                                                      final CasSSLContext casSslContext) {
        val mongo = casProperties.getAudit().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return new MongoDbAuditTrailManager(mongoTemplate, mongo.getCollection(), mongo.isAsynchronous());
    }

    @Bean
    @Autowired
    public AuditTrailExecutionPlanConfigurer mongoDbAuditTrailExecutionPlanConfigurer(
        @Qualifier("mongoDbAuditTrailManager")
        final AuditTrailManager mongoDbAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(mongoDbAuditTrailManager);
    }
}
