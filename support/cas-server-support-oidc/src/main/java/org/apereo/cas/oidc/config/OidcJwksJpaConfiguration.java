package org.apereo.cas.oidc.config;

import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.jpa.OidcJpaJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * This is {@link OidcJwksJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "OidcJwksConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnClass(JpaBeanFactory.class)
public class OidcJwksJpaConfiguration {

    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.oidc.jwks.jpa.url");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "transactionManagerOidcJwks")
    public PlatformTransactionManager transactionManagerOidcJwks(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("oidcJwksEntityManagerFactory")
        final EntityManagerFactory emf) {

        return BeanSupplier.of(PlatformTransactionManager.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val mgmr = new JpaTransactionManager();
                mgmr.setEntityManagerFactory(emf);
                return mgmr;
            })
            .otherwise(PseudoPlatformTransactionManager::new)
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcJwksEntityManagerFactory")
    public EntityManagerFactory oidcJwksEntityManagerFactory(
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
        return BeanSupplier.of(EntityManagerFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val ctx = JpaConfigurationContext.builder()
                    .jpaVendorAdapter(jpaOidcJwksVendorAdapter)
                    .persistenceUnitName("jpaOidcJwksContext")
                    .dataSource(dataSourceOidcJwks)
                    .packagesToScan(jpaOidcJwksPackagesToScan.toSet())
                    .build();
                return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                    casProperties.getAuthn().getOidc().getJwks().getJpa()).getObject();
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
            .otherwiseProxy()
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
                return () -> new OidcJpaJsonWebKeystoreGeneratorService(oidc, transactionTemplate);
            })
            .otherwiseProxy()
            .get();
    }

}
