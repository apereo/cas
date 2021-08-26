package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.RedisSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link SamlIdPRedisRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration("SamlIdPRedisRegisteredServiceMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SamlIdPRedisRegisteredServiceMetadataConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolver redisSamlRegisteredServiceMetadataResolver() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RedisSamlRegisteredServiceMetadataResolver(idp,
            openSamlConfigBean.getObject(), redisSamlRegisteredServiceMetadataResolverTemplate());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataConnectionFactory")
    public RedisConnectionFactory redisSamlRegisteredServiceMetadataConnectionFactory() {
        val redis = casProperties.getAuthn().getSamlIdp().getMetadata().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataResolverTemplate")
    @Bean
    @RefreshScope
    public RedisTemplate<String, SamlMetadataDocument> redisSamlRegisteredServiceMetadataResolverTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisSamlRegisteredServiceMetadataConnectionFactory());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "redisSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer redisSamlRegisteredServiceMetadataResolutionPlanConfigurer() {
        return plan -> plan.registerMetadataResolver(redisSamlRegisteredServiceMetadataResolver());
    }
}
