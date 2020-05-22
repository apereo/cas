package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.CouchbaseAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportCouchbaseAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("casSupportCouchbaseAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportCouchbaseAuditConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public CouchbaseClientFactory auditsCouchbaseClientFactory() {
        val cb = casProperties.getAudit().getCouchbase();
        return new CouchbaseClientFactory(cb);
    }

    @Bean
    public AuditTrailManager couchbaseAuditTrailManager() {
        val cb = casProperties.getAudit().getCouchbase();
        return new CouchbaseAuditTrailManager(auditsCouchbaseClientFactory(),
            new AuditActionContextJsonSerializer(), cb.isAsynchronous());
    }

    @Bean
    public AuditTrailExecutionPlanConfigurer couchbaseAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(couchbaseAuditTrailManager());
    }
}
