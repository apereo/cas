/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * Marker Interface for authentication specifications.
 * 
 * @author William G. Thompson, Jr.
 * @version $Id$
 */
public interface AuthenticationSpecification {
	
	/**
	 * 
	 * @param ticket The ticket we want to check if it satisfies this specification.
	 * @return true if it satisfies it, false otherwise.
	 */
	boolean isSatisfiedBy(Assertion assertion);
}
