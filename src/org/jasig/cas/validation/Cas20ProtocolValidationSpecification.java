/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

/**
 * A mapping of the CAS 2.0 protocol for authentication
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class Cas20ProtocolValidationSpecification extends Cas10ProtocolValidationSpecification implements ValidationSpecification {

    public Cas20ProtocolValidationSpecification() {
        super();
    }

    public Cas20ProtocolValidationSpecification(boolean renew) {
        super(renew);
    }
}