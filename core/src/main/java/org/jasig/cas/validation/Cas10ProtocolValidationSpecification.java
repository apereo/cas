/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * A mapping of the Cas 1.0 protocol for authentication.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class Cas10ProtocolValidationSpecification implements
    ValidationSpecification {

    private final static boolean DEFAULT_RENEW = false;

    private boolean renew;

    public Cas10ProtocolValidationSpecification() {
        this.renew = DEFAULT_RENEW;
    }

    public Cas10ProtocolValidationSpecification(boolean renew) {
        this.renew = renew;
    }

    public void setRenew(boolean renew) {
        this.renew = renew;
    }

    public boolean isRenew() {
        return this.renew;
    }

    public boolean isSatisfiedBy(Assertion assertion) {
        if (!this.renew)
            return true;

        return assertion.isFromNewLogin() && this.renew;
    }

}