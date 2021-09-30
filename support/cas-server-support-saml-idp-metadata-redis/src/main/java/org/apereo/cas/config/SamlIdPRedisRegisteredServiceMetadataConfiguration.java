package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.RedisSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link SamlIdPRedisRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "SamlIdPRedisRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPRedisRegisteredServiceMetadataConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SamlRegisteredServiceMetadataResolver redisSamlRegisteredServiceMetadataResolver(
        final CasConfigurationProperties casProperties,
        @Qualifier("redisSamlRegisteredServiceMetadataResolverTemplate")
        final RedisTemplate<String, SamlMetadataDocument> redisSamlRegisteredServiceMetadataResolverTemplate,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RedisSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, redisSamlRegisteredServiceMetadataResolverTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisSamlRegisteredServiceMetadataConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAuthn().getSamlIdp().getMetadata().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataResolverTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisTemplate<String, SamlMetadataDocument> redisSamlRegisteredServiceMetadataResolverTemplate(
        @Qualifier("redisSamlRegisteredServiceMetadataConnectionFactory")
        final RedisConnectionFactory redisSamlRegisteredServiceMetadataConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisSamlRegisteredServiceMetadataConnectionFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer redisSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("redisSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver redisSamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(redisSamlRegisteredServiceMetadataResolver);
    }
}
