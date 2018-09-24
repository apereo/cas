package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Registers Duo Provider beans into the registry.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Configuration("duoProviderRegistrationConfiguration")
@Slf4j
public class DuoProviderRegistrationConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        val proxy = (RootBeanDefinition) beanDefinitionRegistry.getBeanDefinition("duoMfaProviderFactoryBean");
        val scoped = (RootBeanDefinition) beanDefinitionRegistry.getBeanDefinition("scopedTarget.duoMfaProviderFactoryBean");
        val duos = Binder.get(environment).bind("cas.authn.mfa.duo", Bindable.listOf(DuoSecurityMultifactorProperties.class));
        duos.get().stream().forEach(d -> {
            val proxyClone = proxy.cloneBeanDefinition();
            val proxyProps = new MutablePropertyValues();
            proxyProps.add("targetBeanName", "scopedTarget."+d.getId()+"-provider");
            proxyClone.setPropertyValues(proxyProps);
            val scopedClone = scoped.cloneBeanDefinition();
            val scopedProps = new MutablePropertyValues();
            scopedProps.add("duoId", d.getId());
            scopedClone.setPropertyValues(scopedProps);
            beanDefinitionRegistry.registerBeanDefinition(d.getId() + "-provider", proxyClone);
            beanDefinitionRegistry.registerBeanDefinition("scopedTarget."+ d.getId() + "-provider", scopedClone);
        });
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
