/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * A mapping of the CAS 2.0 protocol for authentication
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class Cas20ProtocolAuthenticationSpecification extends Cas10ProtocolAuthenticationSpecification implements AuthenticationSpecification {

    public Cas20ProtocolAuthenticationSpecification() {
        super();
    }

    public Cas20ProtocolAuthenticationSpecification(boolean renew) {
        super(renew);
    }
}