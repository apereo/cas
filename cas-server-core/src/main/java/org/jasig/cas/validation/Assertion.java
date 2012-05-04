/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.validation;

import java.io.Serializable;
import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;

/**
 * Return from CentralAuthenticationService.validateServiceTicket(String,
 * Service), the Assertion contains a chain of Principal objects. The first is
 * the User's login Principal, while any others are Proxy Principals.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface Assertion extends Serializable {

    /**
     * Get a List of Authentications which represent the owners of the
     * GrantingTickets which granted the ticket that was validated. The first
     * Authentication of this list is the Authentication which originally
     * authenticated to CAS to obtain the first Granting Ticket. Subsequent
     * Authentication are those associated with GrantingTickets that were
     * granted from that original granting ticket. The last Authentication in
     * this List is that associated with the GrantingTicket that was the
     * immediate grantor of the ticket that was validated. The List returned by
     * this method will contain at least one Authentication.
     * 
     * @return a List of Authentication
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
     * Method to obtain the service for which we are asserting this ticket is
     * valid for.
     * 
     * @return the service for which we are asserting this ticket is valid for.
     */
    Service getService();

}
