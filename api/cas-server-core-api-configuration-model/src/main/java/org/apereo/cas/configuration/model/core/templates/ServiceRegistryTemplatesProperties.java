package org.apereo.cas.configuration.model.core.templates;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ServiceRegistryTemplatesProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-services", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ServiceRegistryTemplatesProperties")
public class ServiceRegistryTemplatesProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -168826011744304210L;

    /**
     * The directory location that holds the template service definitions.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties directory = new SpringResourceProperties()
        .setLocation(new ClassPathResource("registeredServiceTemplates"));
}
