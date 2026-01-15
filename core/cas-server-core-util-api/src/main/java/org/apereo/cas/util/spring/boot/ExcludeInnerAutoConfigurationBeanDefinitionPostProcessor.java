package org.apereo.cas.util.spring.boot;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;


/**
 * This is {@link ExcludeInnerAutoConfigurationBeanDefinitionPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class ExcludeInnerAutoConfigurationBeanDefinitionPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final String targetClass;

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        val toRemove = new ArrayList<String>();
        for (val name : registry.getBeanDefinitionNames()) {
            val beanDefinition = registry.getBeanDefinition(name);
            val source = beanDefinition.getSource();
            if (source != null && source.toString().contains(targetClass)) {
                toRemove.add(name);
            }
        }
        toRemove.forEach(registry::removeBeanDefinition);
    }
}
