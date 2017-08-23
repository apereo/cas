package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Map;

/**
 * The release policy that decides how attributes are to be released for a given service.
 * Each policy has the ability to apply an optional filter.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceAttributeReleasePolicy extends Serializable {

    /**
     * Is authorized to release authentication attributes boolean.
     *
     * @return the boolean
     */
    default boolean isAuthorizedToReleaseAuthenticationAttributes() {
        return true;
    }

    /**
     * Is authorized to release credential password?
     *
     * @return true /false
     */
    default boolean isAuthorizedToReleaseCredentialPassword() {
        return false;
    }

    /**
     * Is authorized to release proxy granting ticket?
     *
     * @return true /false
     */
    default boolean isAuthorizedToReleaseProxyGrantingTicket() {
        return false;
    }

    /**
     * Sets the attribute filter.
     *
     * @param filter the new attribute filter
     */
    default void setAttributeFilter(RegisteredServiceAttributeFilter filter) {
    }

    /**
     * Gets the attributes, having applied the filter.
     *
     * @param p               the principal that contains the resolved attributes
     * @param selectedService the selected service
     * @param service         the service
     * @return the attributes
     */
    Map<String, Object> getAttributes(Principal p, Service selectedService, RegisteredService service);

    /**
     * Gets the attributes that qualify for consent.
     *
     * @param p               the principal that contains the resolved attributes
     * @param selectedService the selected service
     * @param service         the service
     * @return the attributes
     */
    default Map<String, Object> getConsentableAttributes(final Principal p, final Service selectedService, 
                                                         final RegisteredService service) {
        return getAttributes(p, selectedService, service);    
    }
}
