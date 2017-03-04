package org.apereo.cas.configuration;

import com.google.common.base.Throwables;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * This is {@link CasConfigurationPropertiesEnvironmentManager}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasConfigurationPropertiesEnvironmentManager {

    @Autowired
    private Environment environment;

    /**
     * Gets standalone profile configuration directory.
     *
     * @return the standalone profile configuration directory
     */
    public File getStandaloneProfileConfigurationDirectory() {
        return environment.getProperty("cas.standalone.config", File.class, new File("/etc/cas/config"));
    }

    public String getApplicationName() {
        return environment.getRequiredProperty("spring.application.name");
    }
    
    /**
     * Save property for standalone profile.
     *
     * @param pair the pair
     */
    public void savePropertyForStandaloneProfile(final Pair<String, String> pair) {
        try {
            final File file = getStandaloneProfileConfigurationDirectory();
            final Parameters params = new Parameters();
            
            final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties().setFile(new File(file, getApplicationName() + ".properties")));

            final Configuration config = builder.getConfiguration();
            config.setProperty(pair.getKey(), pair.getValue());
            builder.save();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
