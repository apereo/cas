package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents a security assertion obtained from a successfully validated ticket.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public interface Assertion extends Serializable {

    /**
     * Gets the authentication event that is basis of this assertion.
     *
     * @return Non-null primary authentication event.
     */
    Authentication getPrimaryAuthentication();

    /**
     * Original authentication tied to this assertion
     * prior to any transformations. This is the authentication
     * attempt on top of which this assertion was built and is
     * used for tracking purposes particularly when the assertion
     * is built manually where necessary.
     *
     * @return the authentication
     */
    Authentication getOriginalAuthentication();

    /**
     * Gets a list of all authentications that have occurred during a CAS SSO session.
     *
     * @return Non-null, non-empty list of authentications in leaf-first order (i.e. authentications on the root ticket
     * occur at the end).
     */
    List<Authentication> getChainedAuthentications();

    /**
     * True if the validated ticket was granted in the same transaction as that
     * in which its grantor GrantingTicket was originally issued.
     *
     * @return true if validated ticket was granted simultaneous with its
     * grantor's issuance
     */
    boolean isFromNewLogin();

    /**
     * True if the validated ticket was self-contained and stateless.
     * 
     * @return true if validated ticket was stateless
     */
    boolean isStateless();

    /**
     * Method to obtain the service for which we are asserting this ticket is
     * valid for.
     *
     * @return the service for which we are asserting this ticket is valid for.
     */
    Service getService();

    /**
     * Gets registered service.
     *
     * @return the registered service
     */
    RegisteredService getRegisteredService();

    /**
     * Context map.
     *
     * @return the map
     */
    Map<String, Serializable> getContext();
}
