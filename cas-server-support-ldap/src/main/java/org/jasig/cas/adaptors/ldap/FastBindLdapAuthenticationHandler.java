/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap;

import javax.naming.directory.DirContext;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Implementation of an LDAP handler to do a "fast bind." A fast bind skips the
 * normal two step binding process to determine validity by providing before
 * hand the path to the uid.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public final class FastBindLdapAuthenticationHandler extends
    AbstractLdapUsernamePasswordAuthenticationHandler {

    protected boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials)
        throws AuthenticationException {
        DirContext dirContext = null;
        try {
            dirContext = this.getContextSource().getDirContext(
                LdapUtils.getFilterWithValues(getFilter(), credentials
                    .getUsername()), credentials.getPassword());
            return true;
        } catch (DataAccessResourceFailureException e) {
            return false;
        } finally {
            if (dirContext != null) {
                LdapUtils
                    .closeContext(dirContext);
            }
        }
    }
}