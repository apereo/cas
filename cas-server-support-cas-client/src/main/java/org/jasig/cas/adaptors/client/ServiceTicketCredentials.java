/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.util.Assert;

/**
 * Credentials holder for CAS Service Tickets (to validate other CAS servers).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ServiceTicketCredentials implements Credentials {

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = 1025477259567939792L;

    private final String serviceTicketId;
    
    private Assertion assertion;

    public ServiceTicketCredentials(final String serviceTicketId) {
        Assert.notNull(serviceTicketId, "serviceTicketId cannot be null.");
        this.serviceTicketId = serviceTicketId;
    }

    public String getServiceTicketId() {
        return this.serviceTicketId;
    }
    
    public void setAssertion(final Assertion assertion) {
        this.assertion = assertion;
    }
    
    public Assertion getAssertion() {
        return this.assertion;
    }
}
