package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.RedisAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

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
 * This is {@link CasAcceptableUsagePolicyRedisConfiguration} that stores AUP decisions in a mongo database.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casAcceptableUsagePolicyRedisConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyRedisConfiguration {

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisAcceptableUsagePolicyTemplate")
    public RedisTemplate redisAcceptableUsagePolicyTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisAcceptableUsagePolicyConnectionFactory());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAcceptableUsagePolicyConnectionFactory")
    public RedisConnectionFactory redisAcceptableUsagePolicyConnectionFactory() {
        val redis = casProperties.getAcceptableUsagePolicy().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        return new RedisAcceptableUsagePolicyRepository(ticketRegistrySupport.getObject(),
            casProperties.getAcceptableUsagePolicy(),
            redisAcceptableUsagePolicyTemplate());
    }
}
