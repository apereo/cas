package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasNativeApplicationContextInitializer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.context.support.GenericApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ZooKeeperNativeApplicationContextInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class ZooKeeperNativeApplicationContextInitializerTests {
    @Test
    void verifyPromotedConfigDataClient() {
        try (val context = new GenericApplicationContext()) {
            val promotedClient = mock(Runnable.class);
            context.registerBean("curatorFramework", Runnable.class, () -> {
                fail("The AOT client must yield to the promoted Config Data client");
                return mock(Runnable.class);
            });
            context.getBeanFactory().getBeanDefinition("curatorFramework").setFactoryBeanName(ZookeeperAutoConfiguration.class.getName());
            context.getBeanFactory().registerSingleton("configDataCuratorFramework", promotedClient);
            val initializer = ServiceLoader.load(CasNativeApplicationContextInitializer.class).stream()
                .filter(provider -> provider.type().equals(ZooKeeperNativeApplicationContextInitializer.class))
                .findFirst().orElseThrow().get();
            initializer.initialize(context);
            context.refresh();
            assertSame(promotedClient, context.getBean("curatorFramework"));
            assertSame(promotedClient, context.getBean(Runnable.class));
        }
    }

    @Test
    void verifyClientWithoutConfigDataImport() {
        try (val context = new GenericApplicationContext()) {
            val client = mock(Runnable.class);
            context.registerBean("curatorFramework", Runnable.class, () -> client);
            new ZooKeeperNativeApplicationContextInitializer().initialize(context);
            context.refresh();
            assertSame(client, context.getBean(Runnable.class));
        }
    }

    @Test
    void verifyCustomClientIsPreserved() {
        try (val context = new GenericApplicationContext()) {
            val client = mock(Runnable.class);
            context.registerBean("curatorFramework", Runnable.class, () -> client);
            context.getBeanFactory().getBeanDefinition("curatorFramework").setPrimary(true);
            context.getBeanFactory().registerSingleton("configDataCuratorFramework", mock(Runnable.class));
            new ZooKeeperNativeApplicationContextInitializer().initialize(context);
            context.refresh();
            assertSame(client, context.getBean("curatorFramework"));
            assertSame(client, context.getBean(Runnable.class));
        }
    }
}
