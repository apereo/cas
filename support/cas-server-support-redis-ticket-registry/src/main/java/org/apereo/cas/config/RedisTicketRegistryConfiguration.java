package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

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
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author serv
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.ticket.registry.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "redisTicketRegistryConfiguration", proxyBeanMethods = false)
public class RedisTicketRegistryConfiguration {

    @ConditionalOnMissingBean(name = "redisTicketConnectionFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory redisTicketConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getTicket().getRegistry().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ticketRedisTemplate")
    public RedisTemplate<String, Ticket> ticketRedisTemplate(
        @Qualifier("redisTicketConnectionFactory")
        final RedisConnectionFactory redisTicketConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisTicketConnectionFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public TicketRegistry ticketRegistry(final CasConfigurationProperties casProperties,
                                         @Qualifier("ticketRedisTemplate")
                                         final RedisTemplate<String, Ticket> ticketRedisTemplate) {
        val redis = casProperties.getTicket().getRegistry().getRedis();
        val r = new RedisTicketRegistry(ticketRedisTemplate);
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(redis.getCrypto(), "redis"));
        return r;
    }
}
