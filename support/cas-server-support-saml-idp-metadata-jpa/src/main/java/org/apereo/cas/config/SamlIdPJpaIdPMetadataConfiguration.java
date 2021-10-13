package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
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
import org.apereo.cas.util.spring.BeanContainer;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link SamlIdPJpaIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Slf4j
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.jpa", name = "idp-metadata-enabled", havingValue = "true")
@Configuration(value = "samlIdPJpaIdPMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPJpaIdPMetadataConfiguration {

    @Configuration(value = "SamlIdPJpaIdPMetadataEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaIdPMetadataEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "jpaSamlMetadataIdPVendorAdapter")
        @Autowired
        public JpaVendorAdapter jpaSamlMetadataIdPVendorAdapter(final CasConfigurationProperties casProperties,
                                                                @Qualifier("jpaBeanFactory")
                                                                final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public BeanContainer<String> jpaSamlMetadataIdPPackagesToScan(final CasConfigurationProperties casProperties) {
            val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
            val type = new JpaSamlIdPMetadataDocumentFactory(idp.getJpa().getDialect()).getType();
            return BeanContainer.of(CollectionUtils.wrapSet(type.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean samlMetadataIdPEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaSamlMetadataIdPVendorAdapter")
            final JpaVendorAdapter jpaSamlMetadataIdPVendorAdapter,
            @Qualifier("dataSourceSamlMetadataIdP")
            final DataSource dataSourceSamlMetadataIdP,
            @Qualifier("jpaSamlMetadataIdPPackagesToScan")
            final BeanContainer<String> jpaSamlMetadataIdPPackagesToScan,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
            val ctx = JpaConfigurationContext.builder().jpaVendorAdapter(jpaSamlMetadataIdPVendorAdapter)
                .persistenceUnitName("jpaSamlMetadataIdPContext").dataSource(dataSourceSamlMetadataIdP)
                .packagesToScan(jpaSamlMetadataIdPPackagesToScan.toSet()).build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, idp.getJpa());
        }
    }

    @Configuration(value = "SamlIdPJpaIdPMetadataTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaIdPMetadataTransactionConfiguration {

        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerSamlMetadataIdP(
            @Qualifier("samlMetadataIdPEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

    }

    @Configuration(value = "SamlIdPJpaIdPMetadataGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaIdPMetadataGeneratorConfiguration {
        @Autowired
        @Bean
        public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
            @Qualifier("transactionManagerSamlMetadataIdP")
            final PlatformTransactionManager mgr,
            @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
            final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
            val transactionTemplate = new TransactionTemplate(mgr);
            return new JpaSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, transactionTemplate);
        }

    }

    @Configuration(value = "SamlIdPJpaIdPMetadataBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaIdPMetadataBaseConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(final CasConfigurationProperties casProperties) {
            val idp = casProperties.getAuthn().getSamlIdp();
            val crypto = idp.getMetadata().getJpa().getCrypto();
            if (crypto.isEnabled()) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, JpaSamlIdPMetadataCipherExecutor.class);
            }
            LOGGER.info("JPA SAML IdP metadata encryption/signing is turned off and "
                        + "MAY NOT be safe in a production environment. "
                        + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
            return CipherExecutor.noOp();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public SamlIdPMetadataLocator samlIdPMetadataLocator(
            @Qualifier("samlIdPMetadataCache")
            final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
            @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
            final CipherExecutor samlIdPMetadataGeneratorCipherExecutor) {
            return new JpaSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor, samlIdPMetadataCache);
        }

    }

    @Configuration(value = "SamlIdPJpaIdPMetadataDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPJpaIdPMetadataDataConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceSamlMetadataIdP")
        @Autowired
        public DataSource dataSourceSamlMetadataIdP(final CasConfigurationProperties casProperties) {
            val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
            return JpaBeans.newDataSource(idp.getJpa());
        }

    }
}
