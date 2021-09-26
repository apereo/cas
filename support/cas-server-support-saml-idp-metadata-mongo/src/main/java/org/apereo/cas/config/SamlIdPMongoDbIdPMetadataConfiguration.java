package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.mongo", name = "idp-metadata-collection")
@Slf4j
@Configuration(value = "samlIdPMongoDbIdPMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPMongoDbIdPMetadataConfiguration {

    @Autowired
    @Qualifier("samlIdPMetadataCache")
    private ObjectProvider<Cache<String, SamlIdPMetadataDocument>> samlIdPMetadataCache;

    @Bean
    @RefreshScope
    @Autowired
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getMongo().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, MongoDbSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("MongoDb SAML IdP metadata encryption/signing is turned off and " + "MAY NOT be safe in a production environment. " +
            "Consider using other choices to handle encryption, signing and verification of " + "metadata artifacts");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "mongoDbSamlIdPMetadataTemplate")
    @Bean
    @RefreshScope
    @Autowired
    public MongoTemplate mongoDbSamlIdPMetadataTemplate(final CasConfigurationProperties casProperties,
                                                        @Qualifier("sslContext")
                                                        final SSLContext sslContext) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val mongo = idp.getMetadata().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getIdpMetadataCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    @RefreshScope
    @Autowired
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(final CasConfigurationProperties casProperties,
                                                             @Qualifier("mongoDbSamlIdPMetadataTemplate")
                                                             final MongoTemplate mongoDbSamlIdPMetadataTemplate,
                                                             @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
                                                             final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new MongoDbSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, mongoDbSamlIdPMetadataTemplate, idp.getMetadata().getMongo().getIdpMetadataCollection());
    }

    @Bean
    @RefreshScope
    @Autowired
    public SamlIdPMetadataLocator samlIdPMetadataLocator(final CasConfigurationProperties casProperties,
                                                         @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
                                                         final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
                                                         @Qualifier("mongoDbSamlIdPMetadataTemplate")
                                                         final MongoTemplate mongoDbSamlIdPMetadataTemplate) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new MongoDbSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor, samlIdPMetadataCache.getObject(), mongoDbSamlIdPMetadataTemplate,
            idp.getMetadata().getMongo().getIdpMetadataCollection());
    }
}
