package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.util.lock.DefaultLockRepository;
import org.apereo.cas.util.lock.LockRepository;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

/**
 * This is {@link CasHazelcastZooKeeperAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasHazelcastZooKeeperAutoConfiguration {

    @Configuration(value = "HazelcastTicketRegistryZooKeeperLockingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(HazelcastTicketRegistry.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistryLocking, module = "hazelcast")
    static class HazelcastTicketRegistryZooKeeperLockingConfiguration {
        private static final int CONNECTION_TIMEOUT_MILLIS = 3000;
        private static final BeanCondition CONDITION = BeanCondition.on("cas.ticket.registry.core.enable-locking").isTrue().evenIfMissing();

        @Bean
        @ConditionalOnMissingBean(name = "casTicketRegistryZooKeeperLockRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRegistry casTicketRegistryZooKeeperLockRepository(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(LockRegistry.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val zk = casProperties.getTicket().getRegistry().getHazelcast().getCluster().getDiscovery().getZookeeper();
                    val curatorClient = CuratorFrameworkFactory.newClient(zk.getUrl(),
                        CONNECTION_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS,
                        new RetryNTimes(2, 100));
                    curatorClient.start();
                    return new ZookeeperLockRegistry(curatorClient);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRepository casTicketRegistryLockRepository(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casTicketRegistryZooKeeperLockRepository")
            final LockRegistry casTicketRegistryZooKeeperLockRepository) {
            return BeanSupplier.of(LockRepository.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultLockRepository(casTicketRegistryZooKeeperLockRepository))
                .otherwise(LockRepository::noOp)
                .get();
        }
    }
}
