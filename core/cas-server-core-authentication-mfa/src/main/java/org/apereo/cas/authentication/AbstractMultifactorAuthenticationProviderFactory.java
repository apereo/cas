package org.apereo.cas.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Abstract class to provide functionality of creating and registering into the
 * refresh scope instances of MultifactorAuthenticatonProvider implementations.
 *
 * @author Travis schmidt
 * @since 5.3.4
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMultifactorAuthenticationProviderFactory {

    private static final String TARGET_BEAN_NAME_KEY = "targetBeanName";
    private static final String SCOPED_TARGET_PREFIX = "scopedTarget.";
    private static final String PROPERTIES_KEY = "properties";
    private static final String PROVIDER_NAME_SUFFIX = "-provider";

    /**
     * The ApplicationContext.
     */
    private final GenericApplicationContext applicationContext;

    /**
     * This name of the registered AbstractFactoryBean used to create provider instances.
     */
    private final String factoryBeanName;

    /**
     * Returns the provider registered in the application context with the passed id.
     *
     * @param id - the id
     * @param <T> - the provider type
     * @return - the provider or null if not found.
     */
    protected <T extends MultifactorAuthenticationProvider> T getProvider(final String id) {
        final String name = name(id);
        if (applicationContext.containsBean(name)) {
            return (T) applicationContext.getBean(name);
        }
        return null;
    }

    /**
     * Creates and registers bean definitions for provider instances using the factory bean as
     * a template.
     *
     * @param props - the properties
     */
    protected void registerInstanceBean(final BaseMultifactorProviderProperties props) {
        final String providerName = name(props.getId());
        /**
         * Get the factory bean definition to use a template to create the instance bean.
         */
        final RootBeanDefinition bean = ((RootBeanDefinition) applicationContext.getBeanDefinition(factoryBeanName)).cloneBeanDefinition();
        final MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.add(TARGET_BEAN_NAME_KEY, SCOPED_TARGET_PREFIX.concat(providerName));
        bean.setPropertyValues(propertyValues);
        LOGGER.debug("Registering provider bean [{}]", bean);
        applicationContext.registerBeanDefinition(providerName, bean);

        /**
         * Get the scoped factory bean definition to use a template to create the scoped instance bean.
         * The factory is expected to have a "properties" field that accepts BaseMultifactorProvider props.
         */
        final RootBeanDefinition scoped = ((RootBeanDefinition) applicationContext.getBeanDefinition(SCOPED_TARGET_PREFIX.concat(factoryBeanName))).cloneBeanDefinition();
        final MutablePropertyValues scopedProps = new MutablePropertyValues();
        scopedProps.addPropertyValue(PROPERTIES_KEY, props);
        scoped.setPropertyValues(scopedProps);
        LOGGER.debug("Registering scoped provider bean[{}]", scoped);
        applicationContext.registerBeanDefinition(SCOPED_TARGET_PREFIX.concat(providerName), scoped);
    }

    private String name(final String id) {
        return id.concat(PROVIDER_NAME_SUFFIX);
    }
}
