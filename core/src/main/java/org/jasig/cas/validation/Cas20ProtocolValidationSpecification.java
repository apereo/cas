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
public class Cas20ProtocolValidationSpecification extends
    Cas10ProtocolValidationSpecification implements ValidationSpecification {

    public Cas20ProtocolValidationSpecification() {
        super();
    }

    public Cas20ProtocolValidationSpecification(final boolean renew) {
        super(renew);
    }
}
