package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.PersonAttributeUtils;
import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.attribute.TenantStubPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
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
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryStubConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Configuration(value = "CasPersonDirectoryStubConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "stub")
class CasPersonDirectoryStubConfiguration {

    @Configuration(value = "StubAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class StubAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "stubAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<PersonAttributeDao> stubAttributeRepositories(final CasConfigurationProperties casProperties) {
            val list = new ArrayList<PersonAttributeDao>();
            val stub = casProperties.getAuthn().getAttributeRepository().getStub();
            val attrs = stub.getAttributes();
            if (!attrs.isEmpty()) {
                val dao = PersonAttributeUtils.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository().getStub());
                list.add(dao);
            }
            return BeanContainer.of(list);
        }
    }

    @Configuration(value = "StubAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class StubAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "stubPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer stubPersonDirectoryAttributeRepositoryPlanConfigurer(
            @Qualifier("stubAttributeRepositories")
            final BeanContainer<PersonAttributeDao> stubAttributeRepositories) {
            return plan -> {
                val results = stubAttributeRepositories.toList()
                    .stream()
                    .filter(PersonAttributeDao::isEnabled)
                    .collect(Collectors.toList());
                plan.registerAttributeRepositories(results);
            };
        }
    }

    @Configuration(value = "StubAttributeRepositoryMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class StubAttributeRepositoryMultitenancyConfiguration {
        @ConditionalOnMissingBean(name = "stubTenantPersonAttributeDaoBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TenantPersonAttributeDaoBuilder stubTenantPersonAttributeDaoBuilder(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(TenantPersonAttributeDaoBuilder.class)
                .when(BeanCondition.on("cas.multitenancy.core.enabled").isTrue().given(applicationContext))
                .supply(TenantStubPersonAttributeDaoBuilder::new)
                .otherwise(TenantPersonAttributeDaoBuilder::noOp)
                .get();
        }
    }
}
