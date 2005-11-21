/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import javax.naming.directory.DirContext;

import org.jasig.cas.adaptors.ldap.util.LdapUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * Implementation of an LDAP handler to do a "fast bind." A fast bind skips the
 * normal two step binding process to determine validity by providing before
 * hand the path to the uid.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class FastBindLdapAuthenticationHandler extends
    AbstractLdapUsernamePasswordAuthenticationHandler {

    /** The filter path to the uid of the user. */
    private String filter;

    protected boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials)
        throws AuthenticationException {
        DirContext dirContext = null;
        try {
            dirContext = this.getContextSource().getDirContext(
                LdapUtils.getFilterWithValues(this.filter, credentials
                    .getUsername()), credentials.getPassword());
            return true;
        } catch (DataAccessResourceFailureException e) {
            return false;
        } finally {
            if (dirContext != null) {
                org.springframework.ldap.support.LdapUtils
                    .closeContext(dirContext);
            }
        }
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    protected void initDao() throws Exception {
        Assert.notNull(this.filter);
    }
}