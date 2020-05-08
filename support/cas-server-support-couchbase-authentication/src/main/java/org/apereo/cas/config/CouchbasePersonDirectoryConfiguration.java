package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.persondir.support.CouchbasePersonAttributeDao;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchbasePersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("couchbasePersonDirectoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbasePersonDirectoryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "couchbasePersonAttributeDao")
    @Bean
    @RefreshScope
    public IPersonAttributeDao couchbasePersonAttributeDao() {
        val couchbase = casProperties.getAuthn().getAttributeRepository().getCouchbase();
        val cb = new CouchbasePersonAttributeDao(couchbase, new CouchbaseClientFactory(couchbase));
        cb.setOrder(couchbase.getOrder());
        FunctionUtils.doIfNotNull(couchbase.getId(), cb::setId);
        return cb;
    }

    @ConditionalOnMissingBean(name = "couchbaseAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope
    public PersonDirectoryAttributeRepositoryPlanConfigurer couchbaseAttributeRepositoryPlanConfigurer() {
        val couchbase = casProperties.getAuthn().getAttributeRepository().getCouchbase();
        return plan -> {
            if (StringUtils.isNotBlank(couchbase.getUsernameAttribute())) {
                plan.registerAttributeRepository(couchbasePersonAttributeDao());
            }
        };
    }
}
