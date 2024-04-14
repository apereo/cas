package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
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
import org.apereo.cas.util.spring.beans.BeanCondition;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link SamlIdPRedisIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider, module = "redis")
@Configuration(value = "SamlIdPRedisIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPRedisIdPMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition
        .on("cas.authn.saml-idp.metadata.redis.idp-metadata-enabled").isTrue()
        .and("cas.authn.saml-idp.metadata.redis.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CipherExecutor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                val crypto = idp.getMetadata().getRedis().getCrypto();
                if (crypto.isEnabled()) {
                    return CipherExecutorUtils.newStringCipherExecutor(crypto, RedisSamlIdPMetadataCipherExecutor.class);
                }
                LOGGER.info("Redis SAML IdP metadata encryption/signing is turned off and "
                            + "MAY NOT be safe in a production environment. "
                            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
                return CipherExecutor.noOp();
            })
            .otherwise(CipherExecutor::noOp)
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisSamlIdPMetadataConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val redis = casProperties.getAuthn().getSamlIdp().getMetadata().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            }))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "redisSamlIdPMetadataTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisSamlIdPMetadataConnectionFactory")
        final RedisConnectionFactory redisSamlIdPMetadataConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisSamlIdPMetadataConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisSamlIdPMetadataTemplate")
        final CasRedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return BeanSupplier.of(SamlIdPMetadataGenerator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new RedisSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, redisSamlIdPMetadataTemplate))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("redisSamlIdPMetadataTemplate")
        final CasRedisTemplate<String, SamlIdPMetadataDocument> redisSamlIdPMetadataTemplate,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(SamlIdPMetadataLocator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new RedisSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
                samlIdPMetadataCache,
                redisSamlIdPMetadataTemplate,
                applicationContext,
                casProperties.getAuthn().getSamlIdp().getMetadata().getRedis().getScanCount()))
            .otherwiseProxy()
            .get();
    }
}
