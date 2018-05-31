package org.apereo.cas.configuration;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.Configuration;
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
    @NonNull
    private final ConfigurationPropertiesBindingPostProcessor binder;

    private final Environment environment;

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
        final var file = getStandaloneProfileConfigurationDirectory();
        final var params = new Parameters();

        final var builder =
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
    public static void rebindCasConfigurationProperties(final ConfigurationPropertiesBindingPostProcessor binder,
                                                        final ApplicationContext applicationContext) {

        final var map = applicationContext.getBeansOfType(CasConfigurationProperties.class);
        final var name = map.keySet().iterator().next();
        LOGGER.debug("Reloading CAS configuration via [{}]", name);
        final var e = applicationContext.getBean(name);
        binder.postProcessBeforeInitialization(e, name);
        final var bean = applicationContext.getAutowireCapableBeanFactory().initializeBean(e, name);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        LOGGER.debug("Reloaded CAS configuration [{}]", name);
    }
}
