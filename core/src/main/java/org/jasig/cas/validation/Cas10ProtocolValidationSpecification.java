/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * A mapping of the Cas 1.0 protocol for authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas10ProtocolValidationSpecification implements
    ValidationSpecification {

    /** The default value for the renew attribute is false. */
    private static final boolean DEFAULT_RENEW = false;

    /** Denotes whether we should always authenticate or not. */
    private boolean renew;

    public Cas10ProtocolValidationSpecification() {
        this.renew = DEFAULT_RENEW;
    }

    public Cas10ProtocolValidationSpecification(final boolean renew) {
        this.renew = renew;
    }

    /**
     * Method to set the renew requirement.
     * 
     * @param renew The renew value we want.
     */
    public final void setRenew(final boolean renew) {
        this.renew = renew;
    }

    /**
     * Method to determine if we require renew to be true.
     * 
     * @return true if renew is required, false otherwise.
     */
    public final boolean isRenew() {
        return this.renew;
    }

    public boolean isSatisfiedBy(final Assertion assertion) {
        return (!this.renew) || (assertion.isFromNewLogin() && this.renew);
    }
}
