package org.apereo.cas.configuration;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * This is {@link CasConfigurationPropertiesEnvironmentManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class CasConfigurationPropertiesEnvironmentManager {

    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "configurationPropertiesEnvironmentManager";

    private final ConfigurationPropertiesBindingPostProcessor binder;

    /**
     * Rebind cas configuration properties.
     *
     * @param binder             the binder
     * @param applicationContext the application context
     * @return the application context
     */
    public static ApplicationContext rebindCasConfigurationProperties(
        final ConfigurationPropertiesBindingPostProcessor binder,
        final ApplicationContext applicationContext) {
        val config = applicationContext.getBean(CasConfigurationProperties.class);
        val name = String.format("%s-%s", CasConfigurationProperties.PREFIX, config.getClass().getName());
        binder.postProcessBeforeInitialization(config, name);
        val bean = applicationContext.getAutowireCapableBeanFactory().initializeBean(config, name);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        LOGGER.debug("Reloaded CAS configuration [{}]", name);
        return applicationContext;
    }


    /**
     * Rebind cas configuration properties.
     *
     * @param applicationContext the application context
     * @return the application context
     */
    public ApplicationContext rebindCasConfigurationProperties(final ApplicationContext applicationContext) {
        return rebindCasConfigurationProperties(this.binder, applicationContext);
    }

    /**
     * Configure environment property sources property source.
     *
     * @param environment the environment
     * @return the property source
     */
    public static CompositePropertySource configureEnvironmentPropertySources(final ConfigurableEnvironment environment) {
        val nativePropertySources = new CompositePropertySource("casNativeCompositeSource");
        val propertySources = environment.getPropertySources();
        FunctionUtils.doIfNotNull(propertySources.get("commandLineArgs"), nativePropertySources::addFirstPropertySource);
        FunctionUtils.doIfNotNull(propertySources.get("systemProperties"), nativePropertySources::addPropertySource);
        FunctionUtils.doIfNotNull(propertySources.get("systemEnvironment"), nativePropertySources::addPropertySource);
        return nativePropertySources;
    }

}
