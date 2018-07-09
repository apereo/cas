package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.SamlIdPMetadataCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * This is {@link CouchDbSamlIdPMetadataConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("ouchDbSamlIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "cas.authn.samlIdp.metadata.couchDb", name = "idpMetadataEnabled", havingValue = "true")
public class CouchDbSamlIdPMetadataConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("samlIdPCouchDbFactory")
    private CouchDbConnectorFactory samlIdPCouchDbFactory;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @RefreshScope
    @Bean
    public CouchDbConnectorFactory samlIdPCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb(), objectMapperFactory);
    }

    @RefreshScope
    @Bean
    public CouchDbInstance samlIdPMetadataCouchDbInstance() {
        return samlIdPCouchDbFactory.createInstance();
    }

    @RefreshScope
    @Bean
    public CouchDbConnector samlIdPMetadataCouchDbConnector() {
        return samlIdPCouchDbFactory.createConnector();
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataCouchDbRepository samlIdPMetadataCouchDbRepository() {
        val repository = new SamlIdPMetadataCouchDbRepository(samlIdPCouchDbFactory.create(),
            casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "couchDbSamlIdPMetadataCipherExecutor")
    public CipherExecutor couchDbSamlIdPMetadataCipherExecutor() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getCouchDb().getCrypto();

        if (crypto.isEnabled()) {
            return new CouchDbSamlIdPMetadataCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        }
        LOGGER.info("CouchDb SAML IdP metadata encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Autowired
    @Bean(initMethod = "generate")
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val idp = casProperties.getAuthn().getSamlIdp();

        return new CouchDbSamlIdPMetadataGenerator(
            samlIdPMetadataLocator(),
            samlSelfSignedCertificateWriter,
            idp.getEntityId(),
            resourceLoader,
            casProperties.getServer().getPrefix(),
            idp.getScope(),
            couchDbSamlIdPMetadataCipherExecutor(),
            samlIdPMetadataCouchDbRepository());
    }

    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        return new CouchDbSamlIdPMetadataLocator(couchDbSamlIdPMetadataCipherExecutor(), samlIdPMetadataCouchDbRepository());
    }
}
