package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Provides a template for redis operations.
 *
 * @author serv
 * @since 5.1.0
 */
public class TicketRedisTemplate extends RedisTemplate<String, Ticket> {

    public TicketRedisTemplate() {
        final RedisSerializer<String> string = new StringRedisSerializer();
        final JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
        setKeySerializer(string);
        setValueSerializer(jdk);
        setHashKeySerializer(string);
        setHashValueSerializer(jdk);
    }

    public TicketRedisTemplate(final RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }
}
