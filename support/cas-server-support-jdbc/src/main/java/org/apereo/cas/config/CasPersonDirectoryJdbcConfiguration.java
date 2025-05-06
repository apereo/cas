package org.apereo.cas.config;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jdbc.JdbcPersonAttributeUtils;
import org.apereo.cas.jdbc.TenantJdbcPersonAttributeDaoBuilder;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnClass(JpaBeans.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "CasPersonDirectoryJdbcConfiguration", proxyBeanMethods = false)
class CasPersonDirectoryJdbcConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.jdbc[0].sql");

    @Configuration(value = "JdbcAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "jdbcAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<PersonAttributeDao> jdbcAttributeRepositories(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val jdbc = casProperties.getAuthn().getAttributeRepository().getJdbc();
                    val list = JdbcPersonAttributeUtils.newJdbcAttributeRepositoryDao(jdbc);
                    return BeanContainer.of(list);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }
    }

    @Configuration(value = "JdbcAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jdbcPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer jdbcPersonDirectoryAttributeRepositoryPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcAttributeRepositories")
            final BeanContainer<PersonAttributeDao> jdbcAttributeRepositories) {
            return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val results = jdbcAttributeRepositories.toList()
                        .stream()
                        .filter(PersonAttributeDao::isEnabled)
                        .collect(Collectors.toList());
                    plan.registerAttributeRepositories(results);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "JdbcAttributeRepositoryMultitenancyConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class JdbcAttributeRepositoryMultitenancyConfiguration {
        @ConditionalOnMissingBean(name = "jdbcTenantPersonAttributeDaoBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TenantPersonAttributeDaoBuilder jdbcTenantPersonAttributeDaoBuilder(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(TenantPersonAttributeDaoBuilder.class)
                .when(BeanCondition.on("cas.multitenancy.core.enabled").isTrue().given(applicationContext))
                .supply(TenantJdbcPersonAttributeDaoBuilder::new)
                .otherwise(TenantPersonAttributeDaoBuilder::noOp)
                .get();
        }
    }
}
