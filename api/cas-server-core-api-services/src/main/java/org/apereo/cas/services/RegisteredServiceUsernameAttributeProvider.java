package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;

/**
 * Strategy interface to define what username attribute should
 * be returned for a given registered service.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceUsernameAttributeProvider extends Serializable {
    /**
     * Resolve the username that is to be returned to CAS clients.
     *
     * @param principal         the principal
     * @param service           the service for which attribute should be calculated
     * @param registeredService the registered service owning this user name attribute provider
     * @return the username value configured for this service
     */
    String resolveUsername(Principal principal, Service service, RegisteredService registeredService);
}
