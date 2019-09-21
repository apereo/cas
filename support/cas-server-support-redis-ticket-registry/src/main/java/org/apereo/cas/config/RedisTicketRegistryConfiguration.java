package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author serv
 * @since 5.0.0
 */
@Configuration("redisTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "redisTicketConnectionFactory")
    @Bean
    public RedisConnectionFactory redisTicketConnectionFactory() {
        val redis = casProperties.getTicket().getRegistry().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketRedisTemplate")
    public RedisTemplate<String, Ticket> ticketRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisTicketConnectionFactory());
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val redis = casProperties.getTicket().getRegistry().getRedis();
        val r = new RedisTicketRegistry(ticketRedisTemplate());
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(redis.getCrypto(), "redis"));
        return r;
    }
}
