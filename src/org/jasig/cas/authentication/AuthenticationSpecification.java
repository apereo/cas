/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * Interface for authentication specifications.
 * 
 * @author William G. Thompson, Jr.
 * @version $Id$
 */
public interface AuthenticationSpecification {

    /**
     * @param assertion The assertion we want to confirm is satisfied by this spec.
     * @return true if it is, false otherwise.
     */
    boolean isSatisfiedBy(Assertion assertion);
}