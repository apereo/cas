package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.syncope.SyncopePersonAttributeDao;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.common.base.Splitter;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SyncopePersonDirectoryConfiguration}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "syncope")
@Configuration(value = "SyncopePersonDirectoryConfiguration", proxyBeanMethods = false)
class SyncopePersonDirectoryConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.syncope.url").isUrl();

    @ConditionalOnMissingBean(name = "syncopePersonAttributeDaos")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @SuppressWarnings("unchecked")
    public BeanContainer<PersonAttributeDao> syncopePersonAttributeDaos(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val properties = casProperties.getAuthn().getAttributeRepository().getSyncope();

                val syncope = casProperties.getAuthn().getSyncope();
                val repositories = Splitter.on(",").splitToList(syncope.getDomain())
                    .stream()
                    .map(domain -> {
                        val dao = new SyncopePersonAttributeDao(properties);
                        dao.setOrder(properties.getOrder());
                        FunctionUtils.doIfNotNull(properties.getId(), id -> dao.setId(id));
                        return dao;
                    })
                    .toList();
                return BeanContainer.of(repositories);
            })
            .otherwise(BeanContainer::empty)
            .get();
    }

    @ConditionalOnMissingBean(name = "syncopeAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer syncopeAttributeRepositoryPlanConfigurer(
        @Qualifier("syncopePersonAttributeDaos")
        final BeanContainer<PersonAttributeDao> syncopePersonAttributeDaos) {
        return plan -> syncopePersonAttributeDaos.toList().stream()
            .filter(PersonAttributeDao::isEnabled).forEach(plan::registerAttributeRepository);
    }
}
