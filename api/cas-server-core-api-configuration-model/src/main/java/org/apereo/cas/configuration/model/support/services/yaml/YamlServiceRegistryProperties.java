package org.apereo.cas.configuration.model.support.services.yaml;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link YamlServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-yaml-service-registry")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("YamlServiceRegistryProperties")
public class YamlServiceRegistryProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 4863603996990314548L;

    /**
     * Flag indicating whether a background watcher thread is enabled
     * for the purposes of live reloading of service registry data changes
     * from persistent data store.
     */
    private boolean watcherEnabled = true;

    public YamlServiceRegistryProperties() {
        setLocation(new ClassPathResource("services"));
    }
}
