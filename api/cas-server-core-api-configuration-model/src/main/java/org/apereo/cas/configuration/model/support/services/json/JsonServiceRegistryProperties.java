package org.apereo.cas.configuration.model.support.services.json;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link JsonServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-json-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class JsonServiceRegistryProperties extends SpringResourceProperties {
    /**
     * Default location directory name where services may be found.
     */
    public static final String DEFAULT_LOCATION_DIRECTORY = "services";

    /**
     * Default location where services may be found on the classpath.
     */
    public static final Resource DEFAULT_LOCATION = new ClassPathResource("services");

    @Serial
    private static final long serialVersionUID = -3022199446494732533L;

    /**
     * Flag indicating whether a background watcher thread is enabled
     * for the purposes of live reloading of service registry data changes
     * from persistent data store.
     */
    private boolean watcherEnabled = true;

    public JsonServiceRegistryProperties() {
        setLocation(DEFAULT_LOCATION);
    }

    /**
     * Is using default location and has it changed?
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isUsingDefaultLocation() {
        return DEFAULT_LOCATION.equals(getLocation());
    }
}
