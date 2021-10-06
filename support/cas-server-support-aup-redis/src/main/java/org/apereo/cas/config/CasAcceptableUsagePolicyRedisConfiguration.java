package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.RedisAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

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
 * This is {@link CasAcceptableUsagePolicyRedisConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnExpression(value = "${cas.acceptable-usage-policy.core.enabled:true} and ${cas.acceptable-usage-policy.redis.enabled:true}")
@Configuration(value = "casAcceptableUsagePolicyRedisConfiguration", proxyBeanMethods = false)
public class CasAcceptableUsagePolicyRedisConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisAcceptableUsagePolicyTemplate")
    public RedisTemplate redisAcceptableUsagePolicyTemplate(
        @Qualifier("redisAcceptableUsagePolicyConnectionFactory")
        final RedisConnectionFactory redisAcceptableUsagePolicyConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisAcceptableUsagePolicyConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAcceptableUsagePolicyConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory redisAcceptableUsagePolicyConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAcceptableUsagePolicy().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(final CasConfigurationProperties casProperties,
                                                                           @Qualifier("redisAcceptableUsagePolicyTemplate")
                                                                           final RedisTemplate redisAcceptableUsagePolicyTemplate,
                                                                           @Qualifier(TicketRegistrySupport.BEAN_NAME)
                                                                           final TicketRegistrySupport ticketRegistrySupport) {
        return new RedisAcceptableUsagePolicyRepository(ticketRegistrySupport,
            casProperties.getAcceptableUsagePolicy(), redisAcceptableUsagePolicyTemplate);
    }
}
