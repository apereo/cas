package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for service.registry.*
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "service.registry", ignoreUnknownFields = false)
public class ServiceRegistryProperties extends AbstractConfigProperties {

    private boolean initFromJson = true;

    private QuartzReloader quartzReloader = new QuartzReloader();

    private boolean watcherEnabled = true;

    public boolean isInitFromJson() {
        return initFromJson;
    }

    public void setInitFromJson(final boolean initFromJson) {
        this.initFromJson = initFromJson;
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

    public ServiceRegistryProperties() {
        super.getConfig().setLocation(new ClassPathResource("services"));
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
