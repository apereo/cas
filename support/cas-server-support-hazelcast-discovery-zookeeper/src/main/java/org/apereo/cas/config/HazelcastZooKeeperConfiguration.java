package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.util.lock.DefaultLockRepository;
import org.apereo.cas.util.lock.LockRepository;

import lombok.val;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

/**
 * This is {@link HazelcastZooKeeperConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "HazelcastZooKeeperConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastZooKeeperConfiguration {

    @Configuration(value = "HazelcastTicketRegistryZooKeeperLockingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(HazelcastTicketRegistry.class)
    @ConditionalOnProperty(prefix = "cas.ticket.registry.core", name = "enable-locking", havingValue = "true", matchIfMissing = true)
    public static class HazelcastTicketRegistryZooKeeperLockingConfiguration {
        private static final int CONNECTION_TIMEOUT_MILLIS = 3000;

        @Bean
        @ConditionalOnMissingBean(name = "casTicketRegistryZooKeeperLockRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRegistry casTicketRegistryZooKeeperLockRepository(final CasConfigurationProperties casProperties) {
            val zk = casProperties.getTicket().getRegistry().getHazelcast().getCluster().getDiscovery().getZookeeper();
            val curatorClient = CuratorFrameworkFactory.newClient(zk.getUrl(),
                CONNECTION_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS,
                new RetryNTimes(2, 100));
            curatorClient.start();
            return new ZookeeperLockRegistry(curatorClient);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LockRepository casTicketRegistryLockRepository(
            @Qualifier("casTicketRegistryZooKeeperLockRepository")
            final LockRegistry casTicketRegistryZooKeeperLockRepository) {
            return new DefaultLockRepository(casTicketRegistryZooKeeperLockRepository);
        }
    }
}
