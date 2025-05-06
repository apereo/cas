package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.persondir.PersonDirectoryUtils;
import org.apereo.cas.persondir.TenantRestfulPersonAttributeDaoBuilder;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */

@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory, module = "rest")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "CasPersonDirectoryRestConfiguration", proxyBeanMethods = false)
class CasPersonDirectoryRestConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.rest[0].url").isUrl();

    @Configuration(value = "RestfulAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RestfulAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "restfulAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<PersonAttributeDao> restfulAttributeRepositories(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val rest = casProperties.getAuthn().getAttributeRepository().getRest();
                    val list = PersonDirectoryUtils.newRestAttributeRepository(rest);
                    return BeanContainer.of(list);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }
    }

    @Configuration(value = "RestfulAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RestfulAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restfulPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer restfulPersonDirectoryAttributeRepositoryPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("restfulAttributeRepositories")
            final BeanContainer<PersonAttributeDao> restfulAttributeRepositories) {
            return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val results = restfulAttributeRepositories.toList()
                        .stream()
                        .filter(PersonAttributeDao::isEnabled)
                        .collect(Collectors.toList());
                    plan.registerAttributeRepositories(results);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "RestfulAttributeRepositoryMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RestfulAttributeRepositoryMultitenancyConfiguration {
        @ConditionalOnMissingBean(name = "restfulTenantPersonAttributeDaoBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TenantPersonAttributeDaoBuilder restfulTenantPersonAttributeDaoBuilder(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(TenantPersonAttributeDaoBuilder.class)
                .when(BeanCondition.on("cas.multitenancy.core.enabled").isTrue().given(applicationContext))
                .supply(TenantRestfulPersonAttributeDaoBuilder::new)
                .otherwise(TenantPersonAttributeDaoBuilder::noOp)
                .get();
        }
    }
}


