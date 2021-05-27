package org.apereo.cas.util.spring;

import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An implementation of {@link ApplicationContextAware} that statically
 * holds the application context.
 *
 * @author Misagh Moayyed
 * @since 3.0.0.
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext CONTEXT;

    public static ApplicationContext getApplicationContext() {
        return CONTEXT;
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        CONTEXT = context;
    }

    /**
     * Gets multifactor authentication principal resolvers.
     *
     * @return the multifactor authentication principal resolvers
     */
    public static List<MultifactorAuthenticationPrincipalResolver> getMultifactorAuthenticationPrincipalResolvers() {
        val resolvers = new ArrayList<>(CONTEXT.getBeansOfType(MultifactorAuthenticationPrincipalResolver.class).values());
        AnnotationAwareOrderComparator.sort(resolvers);
        return resolvers;
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
            return Optional.of(CONTEXT.getBean(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY, IPersonAttributeDao.class));
        }
        return Optional.empty();
    }

    /**
     * Gets configurable application context.
     *
     * @return the configurable application context
     */
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return (ConfigurableApplicationContext) CONTEXT;
    }

    /**
     * Gets script resource cache manager.
     *
     * @return the script resource cache manager
     */
    public static Optional<ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript>> getScriptResourceCacheManager() {
        if (CONTEXT != null && CONTEXT.containsBean(ScriptResourceCacheManager.BEAN_NAME)) {
            return Optional.of(CONTEXT.getBean(ScriptResourceCacheManager.BEAN_NAME, ScriptResourceCacheManager.class));
        }
        return Optional.empty();
    }

    /**
     * Gets attribute definition store.
     *
     * @return the attribute definition store
     */
    public static Optional<AttributeDefinitionStore> getAttributeDefinitionStore() {
        if (CONTEXT != null && CONTEXT.containsBean(AttributeDefinitionStore.BEAN_NAME)) {
            return Optional.of(CONTEXT.getBean(AttributeDefinitionStore.BEAN_NAME, AttributeDefinitionStore.class));
        }
        return Optional.empty();
    }

    /**
     * Gets principal attributes repository.
     *
     * @return the principal attributes repository
     */
    public static Optional<RegisteredServicePrincipalAttributesRepository> getPrincipalAttributesRepository() {
        if (CONTEXT != null && CONTEXT.containsBean(PrincipalResolver.BEAN_NAME_GLOBAL_PRINCIPAL_ATTRIBUTE_REPOSITORY)) {
            return Optional.of(CONTEXT.getBean(PrincipalResolver.BEAN_NAME_GLOBAL_PRINCIPAL_ATTRIBUTE_REPOSITORY,
                RegisteredServicePrincipalAttributesRepository.class));
        }
        return Optional.empty();
    }

    /**
     * Gets principal attributes repository cache.
     *
     * @return the principal attributes repository cache
     */
    public static Optional<PrincipalAttributesRepositoryCache> getPrincipalAttributesRepositoryCache() {
        if (CONTEXT != null && CONTEXT.containsBean(PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME)) {
            return Optional.of(CONTEXT.getBean(PrincipalAttributesRepositoryCache.DEFAULT_BEAN_NAME,
                PrincipalAttributesRepositoryCache.class));
        }
        return Optional.empty();
    }
}
