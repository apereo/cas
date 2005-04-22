/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * An extension point that allows the installation to add attributes
 * to the Authentication object. It has no defined function in the 
 * basic CAS implmentation and it typically represented by a 
 * DefaultAuthenticationAttributesPopulator that does nothing.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface AuthenticationAttributesPopulator {

    /**
     * Given an existing Authentication object, either return it unmodified,
     * replace it, or if it is mutable, change it.
     * 
     * @param Authentication object so far
     * @return the argument or a new Authentication object.
     */
    Authentication populateAttributes(Authentication authentication,
        Credentials credentials);
}
