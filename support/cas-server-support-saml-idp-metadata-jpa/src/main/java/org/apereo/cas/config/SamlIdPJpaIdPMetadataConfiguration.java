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
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.List;

/**
 * This is {@link SamlIdPJpaIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("samlIdPJpaIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.jpa", name = "idp-metadata-enabled", havingValue = "true")
public class SamlIdPJpaIdPMetadataConfiguration {

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;
    
    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private ObjectProvider<SamlIdPCertificateAndKeyWriter> samlSelfSignedCertificateWriter;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaSamlMetadataIdPVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "dataSourceSamlMetadataIdP")
    public DataSource dataSourceSamlMetadataIdP() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        return JpaBeans.newDataSource(idp.getJpa());
    }

    @Bean
    public List<String> jpaSamlMetadataIdPPackagesToScan() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        val type = new JpaSamlIdPMetadataDocumentFactory(idp.getJpa().getDialect()).getType();
        return CollectionUtils.wrapList(type.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean samlMetadataIdPEntityManagerFactory() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaSamlMetadataIdPVendorAdapter(),
            "jpaSamlMetadataIdPContext",
            jpaSamlMetadataIdPPackagesToScan(),
            dataSourceSamlMetadataIdP());
        return factory.newEntityManagerFactoryBean(ctx, idp.getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerSamlMetadataIdP(
        @Qualifier("samlMetadataIdPEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "jpaSamlIdPMetadataCipherExecutor")
    public CipherExecutor jpaSamlIdPMetadataCipherExecutor() {
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

    @Autowired
    @Bean
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(@Qualifier("transactionManagerSamlMetadataIdP") final PlatformTransactionManager mgr) {
        val transactionTemplate = new TransactionTemplate(mgr);

        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter.getObject())
            .resourceLoader(applicationContext)
            .casProperties(casProperties)
            .metadataCipherExecutor(jpaSamlIdPMetadataCipherExecutor())
            .build();

        return new JpaSamlIdPMetadataGenerator(
            context,
            transactionTemplate);
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        return new JpaSamlIdPMetadataLocator(jpaSamlIdPMetadataCipherExecutor());
    }
}
