package org.jasig.cas.services;

import java.io.Serializable;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;

/**
 * The release policy that decides how attributes are to be released for a given service.
 * Each policy has the ability to apply an optional filter.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface RegisteredServiceAttributeReleasePolicy extends Serializable {

    /**
     * Is authorized to release credential password?
     *
     * @return the boolean
     */
    boolean isAuthorizedToReleaseCredentialPassword();

    /**
     * Is authorized to release proxy granting ticket?
     *
     * @return the boolean
     */
    boolean isAuthorizedToReleaseProxyGrantingTicket();

    /**
     * Sets the attribute filter.
     *
     * @param filter the new attribute filter
     */
    void setAttributeFilter(RegisteredServiceAttributeFilter filter);
    
    /**
     * Gets the attributes, having applied the filter.
     *
     * @param p the principal that contains the resolved attributes
     * @return the attributes
     */
    Map<String, Object> getAttributes(Principal p);
}
