/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * Strategy to populate the Authentication object with additional 
 * attributes.  This is not guaranteed to return the same Authentication
 * object each time.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 *
 */
public interface AuthenticationAttributesPopulator {

    /**
     * Method to add additional attributes to an Authentication object.
     * 
     * @param authentication The Authentication object which we want to add attributes to.
     * @return the Authentication object with the new attributes.
     */
    Authentication populateAttributes(Authentication authentication);
}
