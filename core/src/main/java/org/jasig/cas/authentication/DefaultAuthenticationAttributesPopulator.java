/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

/**
 * Default AuthenticationAttributesPopulator which adds no additional attributes
 * to the Authentication object.
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class DefaultAuthenticationAttributesPopulator implements
    AuthenticationAttributesPopulator {

    public Authentication populateAttributes(final Authentication authentication) {
        return authentication;
    }

}
