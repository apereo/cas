package org.apereo.cas.configuration.model.core.services;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for service.registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "service.registry", ignoreUnknownFields = false)
public class ServiceRegistryProperties {

    private boolean initFromJson = true;

    private Config config = new Config();

    private QuartzReloader quartzReloader = new QuartzReloader();

    private boolean watcherEnabled = true;

    public boolean isInitFromJson() {
        return initFromJson;
    }

    public void setInitFromJson(final boolean initFromJson) {
        this.initFromJson = initFromJson;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    public QuartzReloader getQuartzReloader() {
        return quartzReloader;
    }

    public void setQuartzReloader(final QuartzReloader quartzReloader) {
        this.quartzReloader = quartzReloader;
    }

    public boolean isWatcherEnabled() {
        return watcherEnabled;
    }

    public void setWatcherEnabled(final boolean watcherEnabled) {
        this.watcherEnabled = watcherEnabled;
    }

    /**
     * Config.
     */
    public static class Config {
        private Resource location = new ClassPathResource("services");

        public Resource getLocation() {
            return location;
        }

        public void setLocation(final Resource location) {
            this.location = location;
        }
    }

    /**
     * QuartzReloader.
     */
    public static class QuartzReloader {
        private int startDelay = 120000;
        private int repeatInterval = 120000;

        public int getStartDelay() {
            return startDelay;
        }

        public void setStartDelay(final int startDelay) {
            this.startDelay = startDelay;
        }

        public int getRepeatInterval() {
            return repeatInterval;
        }

        public void setRepeatInterval(final int repeatInterval) {
            this.repeatInterval = repeatInterval;
        }
    }
}
