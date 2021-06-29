package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Configuration properties class for Person Directory.
 *
 * @author Dmitriy Kopylenko
 * @author Hal Deadman
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class PersonDirectoryPrincipalProperties implements Serializable {

    private static final long serialVersionUID = 8929912041234879300L;

    /**
     * Attribute name to use to indicate the identifier of the principal constructed.
     * If the attribute is blank or has no values, the default principal id will be used
     * determined by the underlying authentication engine. The principal id attribute
     * usually is removed from the collection of attributes collected, though this behavior
     * depends on the schematics of the underlying authentication strategy.
     */
    private String principalAttribute;

    /**
     * Activated attribute repository identifiers
     * that should be used for fetching attributes
     * if attribute resolution is enabled.
     * The list here may include identifiers separated by comma.
     */
    private String activeAttributeRepositoryIds;

}
