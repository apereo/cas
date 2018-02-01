package org.apereo.cas.configuration.model.support.services.json;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.core.io.ClassPathResource;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link JsonServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-json-service-registry")
@Slf4j
@Getter
@Setter
public class JsonServiceRegistryProperties extends SpringResourceProperties {

    private static final long serialVersionUID = -3022199446494732533L;

    public JsonServiceRegistryProperties() {
        setLocation(new ClassPathResource("services"));
    }
}
