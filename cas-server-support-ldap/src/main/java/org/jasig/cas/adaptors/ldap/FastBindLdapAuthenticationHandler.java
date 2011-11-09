/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */                                     
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.NamingException;

import javax.naming.directory.DirContext;

/**
 * Implementation of an LDAP handler to do a "fast bind." A fast bind skips the
 * normal two step binding process to determine validity by providing before
 * hand the path to the uid.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public final class FastBindLdapAuthenticationHandler extends AbstractLdapUsernamePasswordAuthenticationHandler {

    protected boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {
        DirContext dirContext = null;
        try {
            final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());
            final String bindDn = LdapUtils.getFilterWithValues(getFilter(), transformedUsername);
            this.log.debug("Performing LDAP bind with credential: " + bindDn);
            dirContext = this.getContextSource().getContext(bindDn, credentials.getPassword());
            return true;
        } catch (final NamingException e) {
            // see if we can match to a more specific exception and then throw that instead
            // i.e. AccountLockedException, AccountDisabledException, ExpiredPasswordException,...
            final String details = e.getMessage();
            log.debug("LDAP server returned exception message: " + details);
            handleLDAPError(details);
            return false;
        } finally {
            if (dirContext != null) {
                LdapUtils.closeContext(dirContext);
            }
        }
    }
}