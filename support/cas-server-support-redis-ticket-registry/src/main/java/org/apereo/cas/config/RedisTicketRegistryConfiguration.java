package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.lock.DefaultLockRepository;
import org.apereo.cas.util.lock.LockRepository;

import lombok.val;
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
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author serv
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.ticket.registry.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "RedisTicketRegistryConfiguration", proxyBeanMethods = false)
public class RedisTicketRegistryConfiguration {

    @Configuration(value = "RedisTicketRegistryCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RedisTicketRegistryCoreConfiguration {
        @ConditionalOnMissingBean(name = "redisTicketConnectionFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RedisConnectionFactory redisTicketConnectionFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            val redis = casProperties.getTicket().getRegistry().getRedis();
            return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
        }

        @Bean(name = {"redisTemplate", "ticketRedisTemplate"})
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "ticketRedisTemplate")
        public RedisTemplate<String, Ticket> ticketRedisTemplate(
            @Qualifier("redisTicketConnectionFactory")
            final RedisConnectionFactory redisTicketConnectionFactory) {
            return RedisObjectFactory.newRedisTemplate(redisTicketConnectionFactory);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketRegistry ticketRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier("ticketRedisTemplate")
            final RedisTemplate<String, Ticket> ticketRedisTemplate) {
            val redis = casProperties.getTicket().getRegistry().getRedis();
            val r = new RedisTicketRegistry(ticketRedisTemplate, redis.getScanCount());
            r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(redis.getCrypto(), "redis"));
            return r;
        }
    }

    @Configuration(value = "RedisTicketRegistryLockingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(prefix = "cas.ticket.registry.core", name = "enable-locking", havingValue = "true", matchIfMissing = true)
    public static class RedisTicketRegistryLockingConfiguration {

        @Bean(destroyMethod = "destroy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRegistry casTicketRegistryRedisLockRegistry(
            @Qualifier("redisTicketConnectionFactory")
            final RedisConnectionFactory redisTicketConnectionFactory) {
            val registryKey = "cas-" + RedisLockRegistry.class.getSimpleName();
            return new RedisLockRegistry(redisTicketConnectionFactory, registryKey);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRepository casTicketRegistryLockRepository(
            @Qualifier("casTicketRegistryRedisLockRegistry")
            final LockRegistry casTicketRegistryRedisLockRegistry) {
            return new DefaultLockRepository(casTicketRegistryRedisLockRegistry);
        }
    }
}
