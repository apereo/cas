package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
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
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

/**
 * This is {@link SamlIdPMongoDbIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPMongoDbIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.mongo", name = "idp-metadata-collection")
@Slf4j
public class SamlIdPMongoDbIdPMetadataConfiguration {

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;
    
    @Autowired
    @Qualifier("samlIdPMetadataCache")
    private ObjectProvider<Cache<String, SamlIdPMetadataDocument>> samlIdPMetadataCache;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private ObjectProvider<SamlIdPCertificateAndKeyWriter> samlSelfSignedCertificateWriter;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbSamlIdPMetadataCipherExecutor")
    @RefreshScope
    public CipherExecutor mongoDbSamlIdPMetadataCipherExecutor() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getMongo().getCrypto();

        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, MongoDbSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("MongoDb SAML IdP metadata encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "metadata artifacts");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "mongoDbSamlIdPMetadataTemplate")
    @Bean
    @RefreshScope
    public MongoTemplate mongoDbSamlIdPMetadataTemplate() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val mongo = idp.getMetadata().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getIdpMetadataCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    @SneakyThrows
    @RefreshScope
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter.getObject())
            .applicationContext(applicationContext)
            .casProperties(casProperties)
            .openSamlConfigBean(openSamlConfigBean.getObject())
            .metadataCipherExecutor(mongoDbSamlIdPMetadataCipherExecutor())
            .build();
        return new MongoDbSamlIdPMetadataGenerator(context, mongoDbSamlIdPMetadataTemplate(),
            idp.getMetadata().getMongo().getIdpMetadataCollection());
    }

    @Bean
    @SneakyThrows
    @RefreshScope
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new MongoDbSamlIdPMetadataLocator(
            mongoDbSamlIdPMetadataCipherExecutor(),
            samlIdPMetadataCache.getObject(),
            mongoDbSamlIdPMetadataTemplate(),
            idp.getMetadata().getMongo().getIdpMetadataCollection());
    }
}
