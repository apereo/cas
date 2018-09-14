package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.CouchDbAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.AuditActionContextCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportCouchDbAuditConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("casSupportCouchDbAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasSupportCouchDbAuditConfiguration {

    @Autowired
    @Qualifier("couchDbObjectMapperFactory")
    private ObjectMapperFactory couchDbObjectMapperFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("auditCouchDbFactory")
    private CouchDbConnectorFactory auditCouchDbFactory;

    @ConditionalOnMissingBean(name = "auditCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory auditCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAudit().getCouchDb(), couchDbObjectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "auditCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance auditCouchDbInstance() {
        return auditCouchDbFactory.createInstance();
    }

    @ConditionalOnMissingBean(name = "auditCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector auditCouchDbConnector() {
        return auditCouchDbFactory.createConnector();
    }

    @ConditionalOnMissingBean(name = "auditActionContextCouchDbRepository")
    @Bean
    @RefreshScope
    public AuditActionContextCouchDbRepository auditActionContextCouchDbRepository() {
        return new AuditActionContextCouchDbRepository(auditCouchDbConnector(), casProperties.getAudit().getCouchDb().isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbAuditTrailManager")
    @Bean
    @RefreshScope
    public AuditTrailManager couchDbAuditTrailManager() {
        val repository = auditActionContextCouchDbRepository();
        repository.initStandardDesignDocument();
        return new CouchDbAuditTrailManager(repository, casProperties.getAudit().getCouchDb().isAsyncronous());
    }

    @ConditionalOnMissingBean(name = "couchDbAuditTrailExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuditTrailExecutionPlanConfigurer couchDbAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(couchDbAuditTrailManager());
    }
}
