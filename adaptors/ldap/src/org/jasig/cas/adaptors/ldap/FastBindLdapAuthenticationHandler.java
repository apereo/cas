/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import javax.naming.directory.DirContext;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;

/**
 * Implementation of an LDAP handler to do a "fast bind." A fast bind skips the normal two step binding process to determine validity by providing
 * before hand the path to the uid.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class FastBindLdapAuthenticationHandler extends AbstractLdapAuthenticationHandler {

    private String filter;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticateInternal(final Credentials request) throws AuthenticationException {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;

        DirContext dirContext = this.getContextSource().getDirContext(LdapUtils.getFilterWithValues(this.filter, uRequest.getUserName()),
            uRequest.getPassword());

        if (dirContext == null) {
            return false;
        }

        org.springframework.ldap.support.LdapUtils.closeContext(dirContext);
        return true;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#supports(org.jasig.cas.authentication.principal.Credentials)
     */
    protected boolean supports(Credentials credentials) {
        return credentials == null && credentials.getClass().equals(UsernamePasswordCredentials.class);
    }

    /**
     * @see org.springframework.ldap.core.support.LdapDaoSupport#initDao()
     */
    protected void initDao() throws Exception {
        super.initDao();

        if (this.filter == null) {
            throw new IllegalStateException("filter must be set on " + this.getClass().getName());
        }
    }
}
