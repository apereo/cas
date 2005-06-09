/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * Validation specification for the CAS 1.0 protocol.  This specification
 * checks for the presence of renew=true and if requested, succeeds only if 
 * ticket validation is occurring from a new login.  Additionally,
 * validation will fail if passed a proxy ticket.
 * 
 * @author Scott Battaglia
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas10ProtocolValidationSpecification extends 
	CasProtocolValidationSpecification implements ValidationSpecification {

    public Cas10ProtocolValidationSpecification() {
        super();
    }

    public Cas10ProtocolValidationSpecification(final boolean renew) {
        super(renew);
    }

    public boolean isSatisfiedBy(final Assertion assertion) {
        return super.isSatisfiedBy(assertion)
        	&& (assertion.getChainedAuthentications().size() == 1);
    }
}
