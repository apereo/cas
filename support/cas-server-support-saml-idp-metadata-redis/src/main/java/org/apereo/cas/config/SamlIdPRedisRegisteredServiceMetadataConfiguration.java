package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.RedisSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

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
 * This is {@link SamlIdPRedisRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLServiceProviderMetadata, module = "redis")
@Configuration(value = "SamlIdPRedisRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPRedisRegisteredServiceMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.redis.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolver redisSamlRegisteredServiceMetadataResolver(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("redisSamlRegisteredServiceMetadataResolverTemplate")
        final CasRedisTemplate<String, SamlMetadataDocument> redisSamlRegisteredServiceMetadataResolverTemplate,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        return BeanSupplier.of(SamlRegisteredServiceMetadataResolver.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                return new RedisSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean,
                    redisSamlRegisteredServiceMetadataResolverTemplate,
                    casProperties.getAuthn().getSamlIdp().getMetadata().getRedis().getScanCount());
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataConnectionFactory")
    public RedisConnectionFactory redisSamlRegisteredServiceMetadataConnectionFactory(
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

    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataResolverTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRedisTemplate<String, SamlMetadataDocument> redisSamlRegisteredServiceMetadataResolverTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisSamlRegisteredServiceMetadataConnectionFactory")
        final RedisConnectionFactory redisSamlRegisteredServiceMetadataConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisSamlRegisteredServiceMetadataConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer redisSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver redisSamlRegisteredServiceMetadataResolver) {
        return BeanSupplier.of(SamlRegisteredServiceMetadataResolutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerMetadataResolver(redisSamlRegisteredServiceMetadataResolver))
            .otherwiseProxy()
            .get();
    }
}
