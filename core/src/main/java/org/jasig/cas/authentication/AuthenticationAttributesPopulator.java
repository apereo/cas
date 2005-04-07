/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Strategy to populate the Authentication object with additional attributes.
 * This is not guaranteed to return the same Authentication object each time.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface AuthenticationAttributesPopulator {

    /**
     * Method to add additional attributes to an Authentication object.
     * 
     * @param authentication The Authentication object which we want to add
     * attributes to.
     * @return the Authentication object with the new attributes.
     */
    Authentication populateAttributes(Authentication authentication,
        Credentials credentials);
}
