package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Configuration properties class for global Person Directory settings.
 *
 * @author Dmitriy Kopylenko
 * @author Hal Deadman
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class PersonDirectoryPrincipalResolverGlobalProperties implements Serializable {

    private static final long serialVersionUID = 8929912041234879300L;

    /**
     * Return a null principal object if no attributes can be found for the principal.
     */
    private boolean returnNull;

    /**
     * When true, throws an error back indicating that principal resolution
     * has failed and no principal can be found based on the authentication requirements.
     * Otherwise, logs the condition as an error without raising a catastrophic error.
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

    /**
     * Whether attribute repositories should be contacted
     * to fetch person attributes.
     */
    private boolean attributeResolutionEnabled = true;

    /**
     * In the event that the principal resolution engine resolves
     * more than one principal, (specially if such principals in the chain
     * have different identifiers), this setting determines strategy by which
     * the principal id would be chosen from the chain.
     * Accepted values are: {@code last}, {@code first}.
     */
    private String principalResolutionConflictStrategy = "last";

}
