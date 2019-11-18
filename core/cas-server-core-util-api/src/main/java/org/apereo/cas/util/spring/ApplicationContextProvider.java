package org.apereo.cas.util.spring;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;

/**
 * @author Misagh Moayyed
 * An implementation of {@link ApplicationContextAware} that statically
 * holds the application context
 * @since 3.0.0.
 */
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    public static ApplicationContext getApplicationContext() {
        return CONTEXT;
    }

    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        CONTEXT = ctx;
    }

    /**
     * Hold application context statically.
     *
     * @param ctx the ctx
     */
    public static void holdApplicationContext(final ApplicationContext ctx) {
        CONTEXT = ctx;
    }

    /**
     * Register bean into application context.
     *
     * @param <T>                the type parameter
     * @param applicationContext the application context
     * @param beanClazz          the bean clazz
     * @param beanId             the bean id
     * @return the type registered
     */
    public static <T> T registerBeanIntoApplicationContext(final ConfigurableApplicationContext applicationContext,
                                                           final Class<T> beanClazz, final String beanId) {
        val beanFactory = applicationContext.getBeanFactory();
        val provider = beanFactory.createBean(beanClazz);
        beanFactory.initializeBean(provider, beanId);
        beanFactory.autowireBean(provider);
        beanFactory.registerSingleton(beanId, provider);
        return provider;
    }

    /**
     * Register bean into application context t.
     *
     * @param <T>                the type parameter
     * @param applicationContext the application context
     * @param beanInstance       the bean instance
     * @param beanId             the bean id
     * @return the t
     */
    public static <T> T registerBeanIntoApplicationContext(final ConfigurableApplicationContext applicationContext,
                                                           final T beanInstance, final String beanId) {
        val beanFactory = applicationContext.getBeanFactory();
        if (beanFactory.containsBean(beanId)) {
            return (T) applicationContext.getBean(beanId, beanInstance.getClass());
        }
        beanFactory.initializeBean(beanInstance, beanId);
        beanFactory.autowireBean(beanInstance);
        beanFactory.registerSingleton(beanId, beanInstance);
        return beanInstance;
    }

    /**
     * Gets cas properties.
     *
     * @return the cas properties
     */
    public static Optional<CasConfigurationProperties> getCasConfigurationProperties() {
        if (CONTEXT != null) {
            return Optional.of(CONTEXT.getBean(CasConfigurationProperties.class));
        }
        return Optional.empty();
    }

    /**
     * Gets attribute repository.
     *
     * @return the attribute repository
     */
    public static Optional<IPersonAttributeDao> getAttributeRepository() {
        if (CONTEXT != null) {
            return Optional.of(CONTEXT.getBean("attributeRepository", IPersonAttributeDao.class));
        }
        return Optional.empty();
    }

    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return (ConfigurableApplicationContext) CONTEXT;
    }
}
