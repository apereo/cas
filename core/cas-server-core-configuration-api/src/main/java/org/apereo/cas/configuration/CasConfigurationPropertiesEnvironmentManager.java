package org.apereo.cas.configuration;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * This is {@link CasConfigurationPropertiesEnvironmentManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@Slf4j
@RequiredArgsConstructor
@Getter
public class CasConfigurationPropertiesEnvironmentManager {
    private static final File DEFAULT_CAS_CONFIG_DIRECTORY = new File("/etc/cas/config");

    @NonNull
    private final ConfigurationPropertiesBindingPostProcessor binder;

    private final Environment environment;

    /**
     * Rebind cas configuration properties.
     *
     * @param binder             the binder
     * @param applicationContext the application context
     */
    public static void rebindCasConfigurationProperties(final ConfigurationPropertiesBindingPostProcessor binder,
                                                        final ApplicationContext applicationContext) {

        val map = applicationContext.getBeansOfType(CasConfigurationProperties.class);
        val name = map.keySet().iterator().next();
        LOGGER.debug("Reloading CAS configuration via [{}]", name);
        val e = applicationContext.getBean(name);
        binder.postProcessBeforeInitialization(e, name);
        val bean = applicationContext.getAutowireCapableBeanFactory().initializeBean(e, name);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        LOGGER.debug("Reloaded CAS configuration [{}]", name);
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
     * Gets standalone profile configuration directory.
     *
     * @return the standalone profile configuration directory
     */
    public File getStandaloneProfileConfigurationDirectory() {
        return environment.getProperty("cas.standalone.configurationDirectory", File.class, DEFAULT_CAS_CONFIG_DIRECTORY);
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
        val file = getStandaloneProfileConfigurationDirectory();
        val params = new Parameters();

        val builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFile(new File(file, getApplicationName() + ".properties")));

        val config = builder.getConfiguration();
        config.setProperty(pair.getKey(), pair.getValue());
        builder.save();
    }

}
