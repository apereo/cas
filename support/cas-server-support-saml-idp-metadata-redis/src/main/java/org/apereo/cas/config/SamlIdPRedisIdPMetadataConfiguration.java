package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link SamlIdPRedisIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnExpression(value = "${cas.authn.saml-idp.metadata.redis.idp-metadata-enabled:false} and ${cas.authn.saml-idp.metadata.redis.enabled:true}")
@Slf4j
@Configuration(value = "SamlIdPRedisIdPMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPRedisIdPMetadataConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getRedis().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, RedisSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("Redis SAML IdP metadata encryption/signing is turned off and "
                    + "MAY NOT be safe in a production environment. "
                    + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisSamlIdPMetadataConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getSamlIdp().getMetadata().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate(
        @Qualifier("redisSamlIdPMetadataConnectionFactory")
        final RedisConnectionFactory redisSamlIdPMetadataConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisSamlIdPMetadataConnectionFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        @Qualifier("redisSamlIdPMetadataTemplate")
        final RedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return new RedisSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, redisSamlIdPMetadataTemplate);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("redisSamlIdPMetadataTemplate")
        final RedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate) {
        return new RedisSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
            samlIdPMetadataCache, redisSamlIdPMetadataTemplate);
    }
}
