package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;

import java.io.Serializable;

/**
 * Strategy interface to define what username attribute should
 * be returned for a given registered service.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface RegisteredServiceUsernameAttributeProvider extends Serializable {
    /**
     * Resolve the username that is to be returned to CAS clients.
     *
     * @param principal the principal
     * @param service the service for which attribute should be calculated
     * @return the username value configured for this service
     */
    String resolveUsername(Principal principal, Service service);
}
