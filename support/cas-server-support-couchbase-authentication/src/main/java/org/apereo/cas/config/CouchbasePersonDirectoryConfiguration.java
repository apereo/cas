package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchbase.core.DefaultCouchbaseClientFactory;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.persondir.support.CouchbasePersonAttributeDao;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchbasePersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "couchbase")
@AutoConfiguration
public class CouchbasePersonDirectoryConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.couchbase.username-attribute");

    @ConditionalOnMissingBean(name = "couchbasePersonAttributeDao")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public IPersonAttributeDao couchbasePersonAttributeDao(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(IPersonAttributeDao.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val couchbase = casProperties.getAuthn().getAttributeRepository().getCouchbase();
                val cb = new CouchbasePersonAttributeDao(couchbase, new DefaultCouchbaseClientFactory(couchbase));
                cb.setOrder(couchbase.getOrder());
                FunctionUtils.doIfNotNull(couchbase.getId(), cb::setId);
                return cb;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "couchbaseAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer couchbaseAttributeRepositoryPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("couchbasePersonAttributeDao")
        final IPersonAttributeDao couchbasePersonAttributeDao) throws Exception {
        return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerAttributeRepository(couchbasePersonAttributeDao))
            .otherwiseProxy()
            .get();
    }
}
