package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * Configuration properties class for cas.principal.resolver.persondir.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
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

    public boolean isPrincipalResolutionFailureFatal() {
        return principalResolutionFailureFatal;
    }

    public void setPrincipalResolutionFailureFatal(final boolean principalResolutionFailureFatal) {
        this.principalResolutionFailureFatal = principalResolutionFailureFatal;
    }

    public String getPrincipalAttribute() {
        return principalAttribute;
    }

    public void setPrincipalAttribute(final String principalAttribute) {
        this.principalAttribute = principalAttribute;
    }

    public boolean isReturnNull() {
        return returnNull;
    }

    public void setReturnNull(final boolean returnNull) {
        this.returnNull = returnNull;
    }
}
