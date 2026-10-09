package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasNativeApplicationContextInitializer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.support.GenericApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ConsulNativeApplicationContextInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class ConsulNativeApplicationContextInitializerTests {
    @Test
    void verifyPromotedConfigDataClient() {
        try (val context = new GenericApplicationContext()) {
            val promotedClient = mock(Runnable.class);
            context.registerBean("coreConsulClient", Runnable.class, () -> {
                fail("The AOT client must yield to the promoted Config Data client");
                return mock(Runnable.class);
            });
            context.getBeanFactory().getBeanDefinition("coreConsulClient").setFactoryBeanName(ConsulAutoConfiguration.class.getName());
            context.getBeanFactory().registerSingleton("configDataConsulClient", promotedClient);
            val initializer = ServiceLoader.load(CasNativeApplicationContextInitializer.class).stream()
                .filter(provider -> provider.type().equals(ConsulNativeApplicationContextInitializer.class))
                .findFirst().orElseThrow().get();
            initializer.initialize(context);
            context.refresh();
            assertSame(promotedClient, context.getBean("coreConsulClient"));
            assertSame(promotedClient, context.getBean(Runnable.class));
        }
    }

    @Test
    void verifyClientWithoutConfigDataImport() {
        try (val context = new GenericApplicationContext()) {
            val client = mock(Runnable.class);
            context.registerBean("coreConsulClient", Runnable.class, () -> client);
            new ConsulNativeApplicationContextInitializer().initialize(context);
            context.refresh();
            assertSame(client, context.getBean(Runnable.class));
        }
    }

    @Test
    void verifyCustomClientIsPreserved() {
        try (val context = new GenericApplicationContext()) {
            val client = mock(Runnable.class);
            context.registerBean("coreConsulClient", Runnable.class, () -> client);
            context.getBeanFactory().getBeanDefinition("coreConsulClient").setPrimary(true);
            context.getBeanFactory().registerSingleton("configDataConsulClient", mock(Runnable.class));
            new ConsulNativeApplicationContextInitializer().initialize(context);
            context.refresh();
            assertSame(client, context.getBean("coreConsulClient"));
            assertSame(client, context.getBean(Runnable.class));
        }
    }
}
