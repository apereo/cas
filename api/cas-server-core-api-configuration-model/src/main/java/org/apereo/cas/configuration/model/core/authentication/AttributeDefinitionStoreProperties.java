package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link AttributeDefinitionStoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AttributeDefinitionStoreProperties implements Serializable {
    private static final long serialVersionUID = 1248812041234879300L;

    /**
     * Load attribute definitions from a JSON resource.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties json = new SpringResourceProperties();
}
