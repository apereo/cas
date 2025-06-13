package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.jpa.JpaSamlIdPMetadataDocumentFactory;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link SamlIdPJpaIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProviderMetadata, module = "jpa")
@Configuration(value = "SamlIdPJpaIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPJpaIdPMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.jpa.idp-metadata-enabled").isTrue();

    @Configuration(value = "SamlIdPJpaIdPMetadataEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPJpaIdPMetadataEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "jpaSamlMetadataIdPVendorAdapter")
        public JpaVendorAdapter jpaSamlMetadataIdPVendorAdapter(
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
        public BeanContainer<String> jpaSamlMetadataIdPPackagesToScan(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
                    val type = new JpaSamlIdPMetadataDocumentFactory(idp.getJpa().getDialect()).getType();
                    return BeanContainer.of(CollectionUtils.wrapSet(type.getPackage().getName()));
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public EntityManagerFactory samlMetadataIdPEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaSamlMetadataIdPVendorAdapter")
            final JpaVendorAdapter jpaSamlMetadataIdPVendorAdapter,
            @Qualifier("dataSourceSamlMetadataIdP")
            final DataSource dataSourceSamlMetadataIdP,
            @Qualifier("jpaSamlMetadataIdPPackagesToScan")
            final BeanContainer<String> jpaSamlMetadataIdPPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(EntityManagerFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaSamlMetadataIdPVendorAdapter)
                        .persistenceUnitName("jpaSamlMetadataIdPContext")
                        .dataSource(dataSourceSamlMetadataIdP)
                        .packagesToScan(jpaSamlMetadataIdPPackagesToScan.toSet()).build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx, idp.getJpa()).getObject();
                }))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "SamlIdPJpaIdPMetadataTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPJpaIdPMetadataTransactionConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerSamlMetadataIdP(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("samlMetadataIdPEntityManagerFactory")
            final EntityManagerFactory emf) {
            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val mgmr = new JpaTransactionManager();
                    mgmr.setEntityManagerFactory(emf);
                    return mgmr;
                }))
                .otherwise(PseudoTransactionManager::new)
                .get();
        }
    }

    @Configuration(value = "SamlIdPJpaIdPMetadataGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPJpaIdPMetadataGeneratorConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("transactionManagerSamlMetadataIdP")
            final PlatformTransactionManager mgr,
            @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
            final SamlIdPMetadataGeneratorConfigurationContext ctx) {
            return BeanSupplier.of(SamlIdPMetadataGenerator.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val transactionTemplate = new TransactionTemplate(mgr);
                    return new JpaSamlIdPMetadataGenerator(ctx, transactionTemplate);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "SamlIdPJpaIdPMetadataBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPJpaIdPMetadataBaseConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CipherExecutor.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val idp = casProperties.getAuthn().getSamlIdp();
                    val crypto = idp.getMetadata().getJpa().getCrypto();
                    if (crypto.isEnabled()) {
                        return CipherExecutorUtils.newStringCipherExecutor(crypto, JpaSamlIdPMetadataCipherExecutor.class);
                    }
                    LOGGER.info("JPA SAML IdP metadata encryption/signing is turned off and "
                                + "MAY NOT be safe in a production environment. "
                                + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
                    return CipherExecutor.noOp();
                })
                .otherwise(CipherExecutor::noOp)
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public SamlIdPMetadataLocator samlIdPMetadataLocator(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("samlIdPMetadataCache")
            final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
            @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
            final CipherExecutor samlIdPMetadataGeneratorCipherExecutor) {
            return BeanSupplier.of(SamlIdPMetadataLocator.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new JpaSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor, samlIdPMetadataCache, applicationContext))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "SamlIdPJpaIdPMetadataDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPJpaIdPMetadataDataConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceSamlMetadataIdP")
        public DataSource dataSourceSamlMetadataIdP(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
                    return JpaBeans.newDataSource(idp.getJpa());
                })
                .otherwiseProxy()
                .get();
        }

    }
}
