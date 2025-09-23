package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.syncope.SyncopeUtils;
import org.apereo.cas.syncope.TenantSyncopePersonAttributeDaoBuilder;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
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
    public BeanContainer<PersonAttributeDao> syncopePersonAttributeDaos(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val repositories = SyncopeUtils.newPersonAttributeDaos(
                    casProperties.getAuthn().getAttributeRepository().getSyncope());
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

    @Configuration(value = "SyncopePersonDirectoryMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SyncopePersonDirectoryMultitenancyConfiguration {
        @ConditionalOnMissingBean(name = "syncopeTenantPersonAttributeDaoBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TenantPersonAttributeDaoBuilder syncopeTenantPersonAttributeDaoBuilder(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(TenantPersonAttributeDaoBuilder.class)
                .when(BeanCondition.on("cas.multitenancy.core.enabled").isTrue().given(applicationContext))
                .supply(TenantSyncopePersonAttributeDaoBuilder::new)
                .otherwise(TenantPersonAttributeDaoBuilder::noOp)
                .get();
        }
    }
}
