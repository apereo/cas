/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Principal;


/**
 * Interface for returning the results of a validation attempt
 * on a ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface Assertion {
	
    Principal getPrincipal();

}
