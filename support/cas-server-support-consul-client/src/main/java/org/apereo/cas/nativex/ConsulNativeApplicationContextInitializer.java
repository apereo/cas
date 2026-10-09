package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasNativeApplicationContextInitializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ConsulNativeApplicationContextInitializer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class ConsulNativeApplicationContextInitializer implements CasNativeApplicationContextInitializer {
    @Override
    public void initialize(final @NonNull ConfigurableApplicationContext context) {
        context.addBeanFactoryPostProcessor(beanFactory -> {
            if (beanFactory instanceof final BeanDefinitionRegistry registry
                && beanFactory.containsSingleton("configDataConsulClient") && registry.containsBeanDefinition("coreConsulClient")
                && ConsulAutoConfiguration.class.getName().equals(registry.getBeanDefinition("coreConsulClient").getFactoryBeanName())) {
                registry.removeBeanDefinition("coreConsulClient");
                registry.registerAlias("configDataConsulClient", "coreConsulClient");
            }
        });
    }
}
