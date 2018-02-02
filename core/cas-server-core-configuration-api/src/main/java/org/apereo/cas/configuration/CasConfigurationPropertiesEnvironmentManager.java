package org.apereo.cas.configuration;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Map;

/**
 * This is {@link CasConfigurationPropertiesEnvironmentManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@Slf4j
public class CasConfigurationPropertiesEnvironmentManager {


    @Autowired
    private ConfigurationPropertiesBindingPostProcessor binder;

    @Autowired
    private Environment environment;

    /**
     * Gets standalone profile configuration directory.
     *
     * @return the standalone profile configuration directory
     */
    public File getStandaloneProfileConfigurationDirectory() {
        return environment.getProperty("cas.standalone.configurationDirectory", File.class, new File("/etc/cas/config"));
    }

    /**
     * Gets standalone profile configuration file.
     *
     * @return the standalone profile configuration file
     */
    public File getStandaloneProfileConfigurationFile() {
        return environment.getProperty("cas.standalone.configurationFile", File.class);
    }

    public String getApplicationName() {
        return environment.getRequiredProperty("spring.application.name");
    }

    /**
     * Save property for standalone profile.
     *
     * @param pair the pair
     */
    @SneakyThrows
    public void savePropertyForStandaloneProfile(final Pair<String, String> pair) {
        final File file = getStandaloneProfileConfigurationDirectory();
        final Parameters params = new Parameters();

        final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFile(new File(file, getApplicationName() + ".properties")));

        final Configuration config = builder.getConfiguration();
        config.setProperty(pair.getKey(), pair.getValue());
        builder.save();
    }

    /**
     * Rebind cas configuration properties.
     *
     * @param applicationContext the application context
     */
    public void rebindCasConfigurationProperties(final ApplicationContext applicationContext) {
        rebindCasConfigurationProperties(this.binder, applicationContext);
    }

    /**
     * Rebind cas configuration properties.
     *
     * @param binder             the binder
     * @param applicationContext the application context
     */
    public static void rebindCasConfigurationProperties(@NonNull final ConfigurationPropertiesBindingPostProcessor binder,
                                                        final ApplicationContext applicationContext) {

        final Map<String, CasConfigurationProperties> map = applicationContext.getBeansOfType(CasConfigurationProperties.class);
        final String name = map.keySet().iterator().next();
        LOGGER.debug("Reloading CAS configuration via [{}]", name);
        final Object e = applicationContext.getBean(name);
        binder.postProcessBeforeInitialization(e, name);
        final Object bean = applicationContext.getAutowireCapableBeanFactory().initializeBean(e, name);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        LOGGER.debug("Reloaded CAS configuration [{}]", name);
    }
}
