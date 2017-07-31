package org.apereo.cas.configuration.model.support.services.json;

import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link JsonServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonServiceRegistryProperties extends SpringResourceProperties {
    public JsonServiceRegistryProperties() {
        setLocation(new ClassPathResource("services"));
    }
}
