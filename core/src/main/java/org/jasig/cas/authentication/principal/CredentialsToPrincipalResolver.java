/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Create Principal support for AuthenticationManagerImpl.
 * 
 * <p>After Credentials have been validated, routines implementing this
 * interface are called by AuthenticationManagerImpl to extract the 
 * ID information and create a Principal object.</p>
 * 
 * <p>A minimal Principal object just has one ID value. This can
 * be extended with richer objects containing more properties.
 * The SimplePrincipal class implementing this interface just 
 * stores a userid.</p>
 * 
 * <p>The Credentials typically contains a userid typed by the user or a
 * Certificate presented by the browser. In the simplest case the userid
 * is stored as the Principal ID. The Certificate is a more complicated
 * case because the ID may have to be extracted from the Subject DN or
 * from one of the alternate subject names. In a few cases, the institution
 * may prefer the ID to be a student or employee ID number that can only
 * be obtained by database lookup using information supplied in the
 * Credentials.</p>
 * 
 * <p>The Resolver is free to obtain additional information about
 * the user and place it in the fields of a class that extends 
 * Principal. Such extended information will be stored like other
 * Principal objects in the TGT, persisted as needed, and will be
 * available to the View layer, but it is transparent to most CAS 
 * processing.</p>
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
     * information returned in an extended object.
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
