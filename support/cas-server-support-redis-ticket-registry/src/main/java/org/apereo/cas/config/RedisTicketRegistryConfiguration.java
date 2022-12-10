package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.CachedTicketExpirationPolicy;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.RedisCompositeKey;
import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.pub.DefaultRedisTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.registry.pub.RedisTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.registry.sub.DefaultRedisTicketRegistryMessageListener;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.lock.DefaultLockRepository;
import org.apereo.cas.util.lock.LockRepository;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "redis")
@AutoConfiguration
public class RedisTicketRegistryConfiguration {

    private static final BeanCondition CONDITION = BeanCondition.on("cas.ticket.registry.redis.enabled").isTrue().evenIfMissing();

    @Configuration(value = "RedisTicketRegistryCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RedisTicketRegistryCoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "redisTicketRegistryMessageTopic")
        public Topic redisTicketRegistryMessageTopic() {
            return new ChannelTopic(RedisCompositeKey.REDIS_TICKET_REGISTRY_MESSAGE_TOPIC);
        }

        @Bean
        @ConditionalOnMissingBean(name = "redisTicketRegistryMessageListenerContainer")
        public RedisMessageListenerContainer redisTicketRegistryMessageListenerContainer(
            @Qualifier("redisTicketRegistryMessageTopic")
            final ChannelTopic redisTicketRegistryMessageTopic,
            @Qualifier("redisTicketRegistryMessageListener")
            final MessageListener redisTicketRegistryMessageListener,
            @Qualifier("redisTicketConnectionFactory")
            final RedisConnectionFactory redisTicketConnectionFactory) {
            val container = new RedisMessageListenerContainer();
            container.setConnectionFactory(redisTicketConnectionFactory);
            container.addMessageListener(redisTicketRegistryMessageListener, redisTicketRegistryMessageTopic);
            return container;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "redisTicketRegistryMessageIdentifier")
        public PublisherIdentifier redisTicketRegistryMessageIdentifier(
            final CasConfigurationProperties casProperties) {
            val bean = new PublisherIdentifier();
            val redis = casProperties.getTicket().getRegistry().getRedis();

            FunctionUtils.doIfNotBlank(redis.getQueueIdentifier(), __ -> bean.setId(redis.getQueueIdentifier()));
            return bean;
        }

        @Bean
        @ConditionalOnMissingBean(name = "redisTicketRegistryMessageListener")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MessageListener redisTicketRegistryMessageListener(
            @Qualifier("redisTicketRegistryMessageIdentifier")
            final PublisherIdentifier redisTicketRegistryMessageIdentifier,
            @Qualifier("redisTicketRegistryCache")
            final Cache<String, Ticket> redisTicketRegistryCache) {
            val adapter = new MessageListenerAdapter(
                new DefaultRedisTicketRegistryMessageListener(redisTicketRegistryMessageIdentifier, redisTicketRegistryCache));
            adapter.setSerializer(new JdkSerializationRedisSerializer());
            return adapter;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "redisTicketRegistryMessagePublisher")
        public RedisTicketRegistryMessagePublisher redisTicketRegistryMessagePublisher(
            @Qualifier("redisTicketRegistryMessageIdentifier")
            final PublisherIdentifier redisTicketRegistryMessageIdentifier,
            @Qualifier("ticketRedisTemplate")
            final CasRedisTemplate<String, Ticket> ticketRedisTemplate) {
            return new DefaultRedisTicketRegistryMessagePublisher(ticketRedisTemplate, redisTicketRegistryMessageIdentifier);
        }

        @ConditionalOnMissingBean(name = "redisTicketConnectionFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RedisConnectionFactory redisTicketConnectionFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            return BeanSupplier.of(RedisConnectionFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val redis = casProperties.getTicket().getRegistry().getRedis();
                    return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean(name = {"redisTemplate", "ticketRedisTemplate"})
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "ticketRedisTemplate")
        public CasRedisTemplate<String, Ticket> ticketRedisTemplate(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("redisTicketConnectionFactory")
            final RedisConnectionFactory redisTicketConnectionFactory) {
            return BeanSupplier.of(CasRedisTemplate.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> RedisObjectFactory.newRedisTemplate(redisTicketConnectionFactory))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "redisTicketRegistryCache")
        public Cache<String, Ticket> redisTicketRegistryCache(final CasConfigurationProperties casProperties) {
            val redis = casProperties.getTicket().getRegistry().getRedis();
            return Beans.newCache(redis.getCache(), new CachedTicketExpirationPolicy());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketRegistry ticketRegistry(
            @Qualifier("redisTicketRegistryCache")
            final Cache<String, Ticket> redisTicketRegistryCache,
            @Qualifier("redisTicketRegistryMessagePublisher")
            final RedisTicketRegistryMessagePublisher redisTicketRegistryMessagePublisher,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("ticketRedisTemplate")
            final CasRedisTemplate<String, Ticket> ticketRedisTemplate) {
            return BeanSupplier.of(TicketRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val redis = casProperties.getTicket().getRegistry().getRedis();
                    val registry = new RedisTicketRegistry(ticketRedisTemplate, redisTicketRegistryCache, redisTicketRegistryMessagePublisher);
                    registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(redis.getCrypto(), "redis"));
                    return registry;
                })
                .otherwise(DefaultTicketRegistry::new)
                .get();
        }
    }

    @Configuration(value = "RedisTicketRegistryLockingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistryLocking, module = "redis")
    public static class RedisTicketRegistryLockingConfiguration {
        private static final BeanCondition CONDITION_LOCKING =
            BeanCondition.on("cas.ticket.registry.core.enable-locking").isTrue().evenIfMissing();

        @Bean(destroyMethod = "destroy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRegistry casTicketRegistryRedisLockRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("redisTicketConnectionFactory")
            final RedisConnectionFactory redisTicketConnectionFactory) {
            return BeanSupplier.of(LockRegistry.class)
                .when(CONDITION_LOCKING.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val registryKey = "cas-" + RedisLockRegistry.class.getSimpleName();
                    return new RedisLockRegistry(redisTicketConnectionFactory, registryKey);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRepository casTicketRegistryLockRepository(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casTicketRegistryRedisLockRegistry")
            final LockRegistry casTicketRegistryRedisLockRegistry) {
            return BeanSupplier.of(LockRepository.class)
                .when(CONDITION_LOCKING.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultLockRepository(casTicketRegistryRedisLockRegistry))
                .otherwise(LockRepository::noOp)
                .get();
        }
    }
}
