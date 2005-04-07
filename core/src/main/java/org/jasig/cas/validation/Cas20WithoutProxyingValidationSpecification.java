/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * A mapping of the CAS 2.0 protocol for authentication without the ability to
 * proxy.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20WithoutProxyingValidationSpecification extends
    Cas20ProtocolValidationSpecification {

    public Cas20WithoutProxyingValidationSpecification() {
        super();
    }

    public Cas20WithoutProxyingValidationSpecification(final boolean renew) {
        super(renew);
    }

    public boolean isSatisfiedBy(final Assertion assertion) {
        return super.isSatisfiedBy(assertion)
            && (assertion.getChainedPrincipals().size() == 1);
    }
}
