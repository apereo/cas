/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */ 
package org.jasig.cas.adaptors.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;

/**
 * Authentication strategy for LDAP DIGEST-MD5.
 * 
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 4.0
 */
public class DigestMd5DirContextAuthenticationStrategy
    implements DirContextAuthenticationStrategy
{
    /** Authentication type for DIGEST-MD5 auth */
    private static final String DIGEST_MD5_AUTHENTICATION = "DIGEST-MD5";


    /** {@inheritDoc} */
    public DirContext processContextAfterCreation(
        final DirContext ctx,
        final String userDn,
        final String password)
        throws NamingException {

        return ctx;
    }


    /** {@inheritDoc} */
    @SuppressWarnings(value = "unchecked")
    public void setupEnvironment(
        final Hashtable env,
        final String userDn,
        final String password)
        throws NamingException {

        env.put(Context.SECURITY_AUTHENTICATION, DIGEST_MD5_AUTHENTICATION);
        // userDn should be a bare username for DIGEST-MD5
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

    }

}
