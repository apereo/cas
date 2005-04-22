/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * After Credentials have been validated, routines implementing this
 * interface are called to extract the ID information and create a 
 * Principal object.
 * 
 * <p>If the Credentials are a userid and password, then this
 * interface just gets the userid. Things are more complicated
 * after Certificate validation because there are many different 
 * ways that the meaningful ID value may be stored in a valid 
 * certificate.<p>
 * 
 * <p>A minimal Principal object just has one ID value. This can
 * be extended with richer objects containing more properties.</p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.authentication.principal.Principal
 * @see org.jasig.cas.authentication.principal.Credentials
 */
public interface CredentialsToPrincipalResolver {

    /**
     * Turn Credentials into a Principal object by extracting a
     * primary ID value and, optionally, getting other extended
     * information.
     * 
     * @param credentials from which to resolve Principal
     * @return resolved Principal
     */
    Principal resolvePrincipal(Credentials credentials);

    /**
     * Determine if a credentials type is supported by this resolver. This is
     * checked before calling resolve principal.
     * 
     * @param credentials
     * @return true if we support these credentials, false otherwise.
     */
    boolean supports(Credentials credentials);
}
