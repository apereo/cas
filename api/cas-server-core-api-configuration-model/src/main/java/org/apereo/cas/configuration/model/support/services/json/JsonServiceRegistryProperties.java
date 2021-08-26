package org.apereo.cas.configuration.model.support.services.json;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.ClassPathResource;

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
@JsonFilter("JsonServiceRegistryProperties")
public class JsonServiceRegistryProperties extends SpringResourceProperties {

    private static final long serialVersionUID = -3022199446494732533L;

    /**
     * Flag indicating whether a background watcher thread is enabled
     * for the purposes of live reloading of service registry data changes
     * from persistent data store.
     */
    private boolean watcherEnabled = true;

    public JsonServiceRegistryProperties() {
        setLocation(new ClassPathResource("services"));
    }
}
