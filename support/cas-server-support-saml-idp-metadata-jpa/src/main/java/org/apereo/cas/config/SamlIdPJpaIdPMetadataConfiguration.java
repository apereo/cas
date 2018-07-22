package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
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
@ConditionalOnProperty(prefix = "cas.authn.samlIdp.metadata.jpa", name = "idpMetadataEnabled", havingValue = "true")
public class SamlIdPJpaIdPMetadataConfiguration {

    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaSamlMetadataIdPVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public DataSource dataSourceSamlMetadataIdP() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        return JpaBeans.newDataSource(idp.getJpa());
    }

    @Bean
    public List<String> jpaSamlMetadataIdPPackagesToScan() {
        return CollectionUtils.wrapList(SamlIdPMetadataDocument.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean samlMetadataIdPEntityManagerFactory() {
        val idp = casProperties.getAuthn().getSamlIdp().getMetadata();
        return JpaBeans.newHibernateEntityManagerFactoryBean(
            new JpaConfigDataHolder(
                jpaSamlMetadataIdPVendorAdapter(),
                "jpaSamlMetadataIdPContext",
                jpaSamlMetadataIdPPackagesToScan(),
                dataSourceSamlMetadataIdP()), idp.getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerSamlMetadataIdP(
        @Qualifier("samlMetadataIdPEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


    @Bean
    @ConditionalOnMissingBean(name = "jpaSamlIdPMetadataCipherExecutor")
    public CipherExecutor jpaSamlIdPMetadataCipherExecutor() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getJpa().getCrypto();

        if (crypto.isEnabled()) {
            return new JpaSamlIdPMetadataCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        }
        LOGGER.info("JPA SAML IdP metadata encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Autowired
    @Bean(initMethod = "generate")
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(@Qualifier("transactionManagerSamlMetadataIdP") final PlatformTransactionManager mgr) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val transactionTemplate = new TransactionTemplate(mgr);

        return new JpaSamlIdPMetadataGenerator(
            samlIdPMetadataLocator(),
            samlSelfSignedCertificateWriter,
            idp.getEntityId(),
            resourceLoader,
            casProperties.getServer().getPrefix(),
            idp.getScope(),
            jpaSamlIdPMetadataCipherExecutor(),
            transactionTemplate);
    }

    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        return new JpaSamlIdPMetadataLocator(jpaSamlIdPMetadataCipherExecutor());
    }
}
