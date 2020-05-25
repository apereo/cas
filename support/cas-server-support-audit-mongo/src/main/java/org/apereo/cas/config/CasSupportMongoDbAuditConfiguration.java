package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.MongoDbAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CasSupportMongoDbAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casSupportMongoDbAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportMongoDbAuditConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbAuditTrailManager")
    public AuditTrailManager mongoDbAuditTrailManager() {
        val mongo = casProperties.getAudit().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return new MongoDbAuditTrailManager(mongoTemplate, mongo.getCollection(), mongo.isAsynchronous());
    }

    @Bean
    public AuditTrailExecutionPlanConfigurer mongoDbAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(mongoDbAuditTrailManager());
    }
}
