/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Principal;

/**
 * A Service providing service to an authenticated principal. Typically a web
 * application or web service.
 * 
 * @author William G. Thompson, Jr.
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface Service extends Principal {
    // marker interface for service. No methods
}
