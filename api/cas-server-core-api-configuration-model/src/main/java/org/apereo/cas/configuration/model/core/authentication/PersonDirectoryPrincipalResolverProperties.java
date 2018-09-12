package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Configuration properties class for Person Directory.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class PersonDirectoryPrincipalResolverProperties implements Serializable {

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
     * Return a null principal object if no attributes can be found for the principal.
     */
    private boolean returnNull;

    /**
     * When true, throws an error back indicating that principal resolution
     * has failed and no principal can be found based on the authentication requirements.
     * Otherwise, simply logs the condition as an error without raising a catastrophic error.
     */
    private boolean principalResolutionFailureFatal;

    /**
     * Uses an existing principal id that may have already
     * been established in order to run person directory queries.
     * This is generally useful in situations where
     * authentication is delegated to an external identity provider
     * and a principal is first established to then query an attribute source.
     */
    private boolean useExistingPrincipalId;
}
