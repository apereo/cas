/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * A mapping of the CAS 2.0 protocol for authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas20ProtocolValidationSpecification implements ValidationSpecification {
    
    private final Cas10ProtocolValidationSpecification cas10protocolValidationSpecification;
    
    public Cas20ProtocolValidationSpecification() {
        this.cas10protocolValidationSpecification = new Cas10ProtocolValidationSpecification();
    }

    public Cas20ProtocolValidationSpecification(final boolean renew) {
        this.cas10protocolValidationSpecification = new Cas10ProtocolValidationSpecification(renew);
    }

    public boolean isSatisfiedBy(final Assertion assertion) {
        return this.cas10protocolValidationSpecification.isSatisfiedBy(assertion);
    }
    
    public void setRenew(final boolean renew) {
        this.cas10protocolValidationSpecification.setRenew(renew);
    }
    
    public boolean isRenew() {
        return this.cas10protocolValidationSpecification.isRenew();
    }
}
