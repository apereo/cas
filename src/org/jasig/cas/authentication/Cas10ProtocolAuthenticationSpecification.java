/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * A mapping of the Cas 1.0 protocol for authentication.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class Cas10ProtocolAuthenticationSpecification implements AuthenticationSpecification {

    private final static boolean DEFAULT_RENEW = false;

    private boolean renew;

    public Cas10ProtocolAuthenticationSpecification() {
        this.renew = DEFAULT_RENEW;
    }

    public Cas10ProtocolAuthenticationSpecification(boolean renew) {
        this.renew = renew;
    }

    public void setRenew(boolean renew) {
        this.renew = renew;
    }

    public boolean isRenew() {
        return this.renew;
    }

    /**
     * @see org.jasig.cas.authentication.AuthenticationSpecification#isSatisfiedBy(org.jasig.cas.authentication.Assertion)
     */
    public boolean isSatisfiedBy(Assertion assertion) {
        if (!this.renew)
            return true;

        return assertion.isFromNewLogin() && this.renew;
    }

}