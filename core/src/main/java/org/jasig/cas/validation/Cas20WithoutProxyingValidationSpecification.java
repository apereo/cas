/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * Validation specification for the CAS 2.0 protocol.  This specification
 * extends the Cas10ProtocolValidationSpecification, checking for the 
 * presence of renew=true and if requested, succeeding only if ticket 
 * validation is occurring from a new login.  Additionally, this
 * specification will not accept proxied authentications.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20WithoutProxyingValidationSpecification extends
    CasProtocolValidationSpecification {

    public Cas20WithoutProxyingValidationSpecification() {
        super();
    }

    public Cas20WithoutProxyingValidationSpecification(final boolean renew) {
        super(renew);
    }

    public boolean isSatisfiedBy(final Assertion assertion) {
        return super.isSatisfiedBy(assertion)
            && (assertion.getChainedAuthentications().size() == 1);
    }
}
