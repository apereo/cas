package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.uma.ticket.resource.repository.impl.JpaResourceSetRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasOAuthUmaJpaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.UMA, module = "jpa")
@AutoConfiguration(before = CasOAuthUmaAutoConfiguration.class)
public class CasOAuthUmaJpaAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.oauth.uma.resource-set.jpa.url");

    @Configuration(value = "CasOAuthUmaJpaEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaJpaEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaUmaVendorAdapter(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(JpaVendorAdapter.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaUmaPackagesToScan(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> BeanContainer.of(CollectionUtils.wrapSet(ResourceSet.class.getPackage().getName())))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public EntityManagerFactory umaEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaUmaVendorAdapter")
            final JpaVendorAdapter jpaUmaVendorAdapter,
            @Qualifier("dataSourceUma")
            final DataSource dataSourceUma,
            @Qualifier("jpaUmaPackagesToScan")
            final BeanContainer<String> jpaUmaPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(EntityManagerFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaUmaVendorAdapter)
                        .persistenceUnitName("umaResourceJpaContext")
                        .dataSource(dataSourceUma)
                        .packagesToScan(jpaUmaPackagesToScan.toSet())
                        .build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                        casProperties.getAuthn().getOauth().getUma().getResourceSet().getJpa()).getObject();
                }))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "CasOAuthUmaJpaTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaJpaTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager umaTransactionManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("umaEntityManagerFactory")
            final EntityManagerFactory emf) {
            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val mgmr = new JpaTransactionManager();
                    mgmr.setEntityManagerFactory(emf);
                    return mgmr;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasOAuthUmaJpaDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaJpaDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceUma")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceUma(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getAuthn().getOauth().getUma().getResourceSet().getJpa()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasOAuthUmaJpaRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuthUmaJpaRepositoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jpaUmaResourceSetRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResourceSetRepository umaResourceSetRepository(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(ResourceSetRepository.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(JpaResourceSetRepository::new)
                .otherwiseProxy()
                .get();
        }
    }
}
