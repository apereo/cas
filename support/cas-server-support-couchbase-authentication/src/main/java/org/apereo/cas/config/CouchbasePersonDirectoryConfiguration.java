package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.persondir.support.CouchbasePersonAttributeDao;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchbasePersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@ConditionalOnProperty(prefix = "cas.authn.attribute-repository.couchbase", name = "username-attribute")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "couchbasePersonDirectoryConfiguration", proxyBeanMethods = false)
public class CouchbasePersonDirectoryConfiguration {

    @ConditionalOnMissingBean(name = "couchbasePersonAttributeDao")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public IPersonAttributeDao couchbasePersonAttributeDao(final CasConfigurationProperties casProperties) {
        val couchbase = casProperties.getAuthn().getAttributeRepository().getCouchbase();
        val cb = new CouchbasePersonAttributeDao(couchbase, new CouchbaseClientFactory(couchbase));
        cb.setOrder(couchbase.getOrder());
        FunctionUtils.doIfNotNull(couchbase.getId(), cb::setId);
        return cb;
    }

    @ConditionalOnMissingBean(name = "couchbaseAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer couchbaseAttributeRepositoryPlanConfigurer(
        @Qualifier("couchbasePersonAttributeDao")
        final IPersonAttributeDao couchbasePersonAttributeDao) {
        return plan -> plan.registerAttributeRepository(couchbasePersonAttributeDao);
    }
}
