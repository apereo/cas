package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasNativeApplicationContextInitializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ZooKeeperNativeApplicationContextInitializer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class ZooKeeperNativeApplicationContextInitializer implements CasNativeApplicationContextInitializer {
    @Override
    public void initialize(final @NonNull ConfigurableApplicationContext context) {
        context.addBeanFactoryPostProcessor(beanFactory -> {
            if (beanFactory instanceof final BeanDefinitionRegistry registry
                && beanFactory.containsSingleton("configDataCuratorFramework") && registry.containsBeanDefinition("curatorFramework")
                && ZookeeperAutoConfiguration.class.getName().equals(registry.getBeanDefinition("curatorFramework").getFactoryBeanName())) {
                registry.removeBeanDefinition("curatorFramework");
                registry.registerAlias("configDataCuratorFramework", "curatorFramework");
            }
        });
    }
}
