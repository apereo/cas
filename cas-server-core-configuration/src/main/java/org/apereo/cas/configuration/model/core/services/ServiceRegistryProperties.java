package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.support.AbstractConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;

/**
 * Configuration properties class for service.registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class ServiceRegistryProperties extends AbstractConfigProperties {

    private boolean initFromJson = true;

    private int startDelay;
    
    private int repeatInterval;

    private boolean watcherEnabled = true;

    /**
     * Instantiates a new Service registry properties.
     */
    public ServiceRegistryProperties() {
        super.getConfig().setLocation(new ClassPathResource("services"));
    }
    
    public boolean isInitFromJson() {
        return initFromJson;
    }

    public void setInitFromJson(final boolean initFromJson) {
        this.initFromJson = initFromJson;
    }

    public boolean isWatcherEnabled() {
        return watcherEnabled;
    }

    public void setWatcherEnabled(final boolean watcherEnabled) {
        this.watcherEnabled = watcherEnabled;
    }

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
