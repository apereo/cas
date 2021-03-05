package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link SamlIdPRedisIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration("SamlIdPRedisIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnExpression(value = "${cas.authn.saml-idp.metadata.redis.idp-metadata-enabled:false} and ${cas.authn.saml-idp.metadata.redis.enabled:true}")
@Slf4j
public class SamlIdPRedisIdPMetadataConfiguration {

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlIdPMetadataCache")
    private ObjectProvider<Cache<String, SamlIdPMetadataDocument>> samlIdPMetadataCache;
    
    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private ObjectProvider<SamlIdPCertificateAndKeyWriter> samlSelfSignedCertificateWriter;

    @Bean
    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataCipherExecutor")
    @RefreshScope
    public CipherExecutor redisSamlIdPMetadataCipherExecutor() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getRedis().getCrypto();

        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, RedisSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("Redis SAML IdP metadata encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataConnectionFactory")
    public RedisConnectionFactory redisSamlIdPMetadataConnectionFactory() {
        val redis = casProperties.getAuthn().getSamlIdp().getMetadata().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataTemplate")
    @Bean
    @RefreshScope
    public RedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisSamlIdPMetadataConnectionFactory());
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter.getObject())
            .applicationContext(applicationContext)
            .casProperties(casProperties)
            .openSamlConfigBean(openSamlConfigBean.getObject())
            .metadataCipherExecutor(redisSamlIdPMetadataCipherExecutor())
            .build();
        return new RedisSamlIdPMetadataGenerator(context, redisSamlIdPMetadataTemplate());
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        return new RedisSamlIdPMetadataLocator(
            redisSamlIdPMetadataCipherExecutor(),
            samlIdPMetadataCache.getObject(),
            redisSamlIdPMetadataTemplate());
    }
}
