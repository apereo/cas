package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.jpa.OidcJpaJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This is {@link CasOidcJwksJpaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect, module = "jpa")
@AutoConfiguration
public class CasOidcJwksJpaAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.oidc.jwks.jpa.url");
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "transactionManagerOidcJwks")
    public PlatformTransactionManager transactionManagerOidcJwks(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("oidcJwksEntityManagerFactory")
        final ObjectProvider<EntityManagerFactory> emf) {

        return BeanSupplier.of(PlatformTransactionManager.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .and(() -> Objects.nonNull(emf.getIfAvailable()))
            .supply(() -> {
                val mgmr = new JpaTransactionManager();
                mgmr.setEntityManagerFactory(emf.getObject());
                return mgmr;
            })
            .otherwise(PseudoTransactionManager::new)
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcJwksJpaPersistenceProviderConfigurer")
    public JpaPersistenceProviderConfigurer oidcJwksJpaPersistenceProviderConfigurer(
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(JpaPersistenceProviderConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> context -> {
                val entities = CollectionUtils.wrapList(OidcJsonWebKeystoreEntity.class.getName());
                context.getIncludeEntityClasses().addAll(entities);
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceProvider oidcJwksJpaPersistenceProvider(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
        return BeanSupplier.of(PersistenceProvider.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> jpaBeanFactory.newPersistenceProvider(casProperties.getAuthn().getOidc().getJwks().getJpa()))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcJwksEntityManagerFactory")
    public FactoryBean<EntityManagerFactory> oidcJwksEntityManagerFactory(
        @Qualifier("oidcJwksJpaPersistenceProvider")
        final PersistenceProvider oidcJwksPersistenceProvider,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("jpaOidcJwksVendorAdapter")
        final JpaVendorAdapter jpaOidcJwksVendorAdapter,
        @Qualifier("dataSourceOidcJwks")
        final DataSource dataSourceOidcJwks,
        @Qualifier("jpaOidcJwksPackagesToScan")
        final BeanContainer<String> jpaOidcJwksPackagesToScan,
        @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
        final JpaBeanFactory jpaBeanFactory,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(FactoryBean.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val ctx = JpaConfigurationContext.builder()
                    .jpaVendorAdapter(jpaOidcJwksVendorAdapter)
                    .persistenceUnitName(OidcJpaJsonWebKeystoreGeneratorService.PERSISTENCE_UNIT_NAME)
                    .dataSource(dataSourceOidcJwks)
                    .packagesToScan(jpaOidcJwksPackagesToScan.toSet())
                    .persistenceProvider(oidcJwksPersistenceProvider)
                    .build();
                return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                    casProperties.getAuthn().getOidc().getJwks().getJpa());
            }))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "jpaOidcJwksVendorAdapter")
    public JpaVendorAdapter jpaOidcJwksVendorAdapter(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
        final JpaBeanFactory jpaBeanFactory,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(JpaVendorAdapter.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<String> jpaOidcJwksPackagesToScan(final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> BeanContainer.of(CollectionUtils.wrapSet(OidcJsonWebKeystoreEntity.class.getPackage().getName())))
            .otherwise(BeanContainer::empty)
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceOidcJwks")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource dataSourceOidcJwks(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(DataSource.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> JpaBeans.newDataSource(casProperties.getAuthn().getOidc().getJwks().getJpa()))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Supplier<OidcJsonWebKeystoreGeneratorService> jpaJsonWebKeystoreGeneratorService(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("transactionManagerOidcJwks")
        final PlatformTransactionManager transactionManagerOidcJwks,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(Supplier.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val oidc = casProperties.getAuthn().getOidc();
                LOGGER.info("Managing JWKS via a relational database at [{}]", oidc.getJwks().getJpa().getUrl());
                val transactionTemplate = new TransactionTemplate(transactionManagerOidcJwks);
                return () -> new OidcJpaJsonWebKeystoreGeneratorService(oidc, transactionTemplate, applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

}
