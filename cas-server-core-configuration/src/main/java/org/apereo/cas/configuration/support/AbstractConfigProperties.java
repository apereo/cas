package org.apereo.cas.configuration.support;

import org.springframework.core.io.Resource;

/**
 * Common properties for configuration models requiring 'config' namespace abstraction.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class AbstractConfigProperties {

    private Config config = new Config();

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    /**
     * Encapsulates re-usable properties for *.config.* namespace.
     */
    public static class Config {
        private Resource location;

        public Resource getLocation() {
            return location;
        }

        public void setLocation(final Resource location) {
            this.location = location;
        }
    }
}
