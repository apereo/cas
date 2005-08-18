/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import javax.naming.directory.DirContext;

import org.jasig.cas.adaptors.ldap.util.LdapUtils;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.ldap.core.support.LdapDaoSupport;

/**
 * Implementation of an LDAP handler to do a "fast bind." A fast bind skips the
 * normal two step binding process to determine validity by providing before
 * hand the path to the uid.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class FastBindLdapAuthenticationHandler extends LdapDaoSupport
    implements AuthenticationHandler {

    /** The filter path to the uid of the user. */
    private String filter;

    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials) request;
        DirContext dirContext = null;
        try {
            dirContext = this.getContextSource().getDirContext(
            LdapUtils.getFilterWithValues(this.filter, uRequest.getUsername()),
            uRequest.getPassword());
            return true;
        } catch (DataAccessResourceFailureException e) {
            return false;
        } finally {
            if (dirContext != null) {
                org.springframework.ldap.support.LdapUtils.closeContext(dirContext);
            }
        }
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @return true if the credentials is not null and the class equals
     * UsernamePasswordCredentials.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && credentials.getClass().equals(UsernamePasswordCredentials.class);
    }

    protected void initDao() throws Exception {
        super.initDao();

        if (this.filter == null) {
            throw new IllegalStateException("filter must be set on "
                + this.getClass().getName());
        }
    }
}