package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.web.support.CouchDbThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

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
    @Qualifier("auditActionContextCouchDbRepository")
    private ObjectProvider<AuditActionContextCouchDbRepository> couchDbRepository;

    @Autowired
    @Qualifier("authenticationThrottlingConfigurationContext")
    private ObjectProvider<ThrottledSubmissionHandlerConfigurationContext> authenticationThrottlingConfigurationContext;

    @ConditionalOnMissingBean(name = "couchDbAuthenticationThrottle")
    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        return new CouchDbThrottledSubmissionHandlerInterceptorAdapter(
            authenticationThrottlingConfigurationContext.getObject(), couchDbRepository.getObject());
    }
}
