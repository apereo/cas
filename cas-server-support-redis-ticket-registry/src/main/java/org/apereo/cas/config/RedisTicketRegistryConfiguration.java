package org.apereo.cas.config;

import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRedisTemplate;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author serv
 * @since 5.0.0
 */
@Configuration("redisTicketRegistryConfiguration")
public class RedisTicketRegistryConfiguration {


    @RefreshScope
    @Bean
    public TicketRedisTemplate ticketRedisTemplate(RedisConnectionFactory connectionFactory){
        return new TicketRedisTemplate(connectionFactory);
    }


    @RefreshScope
    @Bean(name = {"redisTicketRegistry", "ticketRegistry"})
    public TicketRegistry redisTicketRegistry(TicketRedisTemplate ticketRedisTemplate) {
        return new RedisTicketRegistry(ticketRedisTemplate);
    }
}
